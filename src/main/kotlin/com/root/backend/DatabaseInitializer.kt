package com.root.backend

import com.root.backend.auth.Identities
import com.root.backend.auth.Review
import com.root.backend.auth.Reviews
import com.root.backend.auth.User
import com.root.backend.auth.util.HashUtil
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DatabaseInitializer(private val database: Database) {

    @PostConstruct
    fun init() {
        insertDummyData()
        insertDummyReviewData()
    }

    private fun insertDummyData() {
        transaction {
            // 데이터가 이미 존재하는지 확인
            val existingUsernames = Identities.select { Identities.username inList listOf("user1", "user2", "user3") }
                .map { it[Identities.username] }

            val usersToInsert = listOf(
                User(1L, "user1", "pass1"),
                User(2L, "user2", "pass2"),
                User(3L, "user3", "pass3")
            ).filter { it.username !in existingUsernames }

            for (user in usersToInsert) {
                Identities.insert {
                    it[id] = EntityID(user.id, Identities)
                    it[username] = user.username
                    it[secret] = HashUtil.createHash(user.password)  // 비밀번호를 해싱하여 저장
                }
            }
        }
    }

    private fun insertDummyReviewData() {
        transaction {
            val existingReviews = Reviews.selectAll().count()

            // 이미 리뷰 데이터가 있다면, 중복 데이터를 피하기 위해 함수 종료
            if (existingReviews > 0) return@transaction

            val reviewsToInsert = listOf(
                    Review(1 ,"BrandA", 1, "1990-01-01", "남", "리뷰1"),
                    Review(2 ,"BrandB", 2, "1985-10-02", "여", "리뷰2"),
                    Review(3 ,"BrandC", 3, "2000-10-27", "남", "리뷰3"),
            )

            for (review in reviewsToInsert) {
                Reviews.insert {
                    it[brandName] = review.brandName
                    it[productNumber] = review.productNumber
                    it[birthDate] = review.birthDate
                    it[gender] = review.gender
                    it[content] = review.content
                }
            }
        }
    }
}