package com.root.backend.review

import com.root.backend.Reviews
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ReviewStatisticsService {

    fun getAgeGroupStatistics(brandName: String): Map<String, Int> {
        val currentYear = LocalDate.now().year
        return transaction {
            Reviews.select { Reviews.brandName eq brandName}.map { row ->
                val birthYear = row[Reviews.birthDate].split("-")[0].toInt()
                val age = currentYear - birthYear
                age / 10 * 10
            }.groupingBy { it.toString() + "대" }.eachCount()
        }
    }

    fun getGenderStatistics(brandName: String): Map<String, Int> {
        return transaction {
            Reviews.select { Reviews.brandName eq brandName}.map { row ->
                row[Reviews.gender]
            }.groupingBy { it }
                    .eachCount()
        }
    }
}