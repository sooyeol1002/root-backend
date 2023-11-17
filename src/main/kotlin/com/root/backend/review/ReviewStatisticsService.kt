package com.root.backend.review

import com.fasterxml.jackson.databind.ObjectMapper
import com.root.backend.Review
import com.root.backend.Reviews
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.LocalDate

@Service
class ReviewStatisticsService {

    fun getAgeGroupStatistics(brandName: String): Map<String, Int> {
        return transaction {
            // brandName에 해당하는 리뷰들을 모두 가져옴
            Reviews.select { Reviews.brandName eq brandName }.mapNotNull { row ->
                // 각 row를 Review 객체로 변환
                rowToReview(row)?.let { review ->
                    // Review 객체로부터 나이 계산
                    val age = review.calculateAge()
                    // 나이를 10년 단위로 그룹화하여 키를 생성 (예: "20대")
                    age / 10 * 10
                }
                // 나이 그룹별로 카운트
            }.groupingBy { it.toString() + "대" }.eachCount()
        }
    }

    // row 데이터를 Review 객체로 변환하는 함수
    fun rowToReview(row: ResultRow): Review? {
        return try {
            Review(
                    id = row[Reviews.id].value,
                    brandName = row[Reviews.brandName],
                    productId = row[Reviews.productId],
                    birth = row[Reviews.birth], // "yyyymmdd" 형태의 문자열이 있어야 함
                    gender = row[Reviews.gender],
                    reviewContent = row[Reviews.reviewContent],
                    scope = row[Reviews.scope],
                    userId = row[Reviews.userId],
                    reviewAnswer = row[Reviews.reviewAnswer],
                    receivedId = row[Reviews.receivedId],
                    currentTime = row[Reviews.currentTime].toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getGenderStatistics(brandName: String): Map<String, Int> {
        return transaction {
            Reviews.select { Reviews.brandName eq brandName}.map { row ->
                when (row[Reviews.gender]) {
                    "남", "남성", "male" -> "남"
                    "여", "여성", "female"-> "여"
                    else -> row[Reviews.gender]
                }
            }.groupingBy { it }
                    .eachCount()
        }
    }

    fun getProductScopeStatistics(brandName: String): String {
        val statisticsMap = transaction {
            Reviews.select { Reviews.brandName eq brandName }
                    .mapNotNull { rowToReview(it) }
                    .groupBy { it.productId }
                    .mapValues { (_, reviews) ->
                        reviews.map { it.scope }.average()
                    }
        }
        return ObjectMapper().writeValueAsString(statisticsMap)
    }

}