package com.root.backend.review

import com.root.backend.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

@Service
class ReviewService(private val rabbitTemplate: RabbitTemplate,
                    private val messagingTemplate: SimpMessagingTemplate,
                    private val jdbcTemplate: JdbcTemplate) {

    private val reviewRowMapper = RowMapper { rs: ResultSet, _: Int ->
        Review(
                id = rs.getLong("id"),
                brandName = rs.getString("brand_name"),
                productNumber = rs.getInt("product_number"),
                birthDate = rs.getString("birth_date"),
                gender = rs.getString("gender"),
                content = rs.getString("content"),
                scope = rs.getInt("scope"),
                userId = rs.getInt("user_id")
        )
    }

    fun saveReceivedReview(review: Review) {
        transaction {
            Reviews.insert {
                it[brandName] = review.brandName
                it[productNumber] = review.productNumber
                it[birthDate] = review.birthDate
                it[gender] = review.gender
                it[content] = review.content
                it[scope] = review.scope
                it[userLoginId] = review.userId
            }
        }

        messagingTemplate.convertAndSend("/topic/review", review.toReviewDto())
    }

    fun findReviewsByBrandNameWithPaging(brandName: String, page: Int, size: Int): PagedReviews {
        val offset = page * size
        val sql = "SELECT * FROM review WHERE brand_name = ? ORDER BY id DESC LIMIT ? OFFSET ?"
        val reviews = jdbcTemplate.query(sql, reviewRowMapper, brandName, size, offset)

        val totalElementsSql = "SELECT COUNT(*) FROM review WHERE brand_name = ?"
        val totalElements = jdbcTemplate.queryForObject(totalElementsSql, Int::class.java, brandName)

        val totalPages = (totalElements + size - 1) / size

        return PagedReviews(reviews, totalPages, totalElements)
    }

    fun updateReviewAnswer(reviewId: Long, answer: String) {
        var updatedReview: ReviewDto? = null
        transaction {
            Reviews.update({ Reviews.id eq reviewId }) {
                it[reviewAnswer] = answer
            }
            updatedReview = selectReviewById(reviewId)?.toReviewDto()
        }

        updatedReview?.let {
            rabbitTemplate.convertAndSend(it)
            println("Review answer updated and message sent to RabbitMQ: $it")
        }
    }

    private fun selectReviewById(reviewId: Long): Review? {
        return Reviews.select { Reviews.id eq reviewId }
                .mapNotNull { toReview(it) }
                .singleOrNull()
    }

    private fun toReview(row: ResultRow): Review =
            Review(
                    id = row[Reviews.id].value,
                    brandName = row[Reviews.brandName],
                    productNumber = row[Reviews.productNumber],
                    birthDate = row[Reviews.birthDate],
                    gender = row[Reviews.gender],
                    content = row[Reviews.content],
                    scope = row[Reviews.scope],
                    userId = row[Reviews.userLoginId],
                    reviewAnswer = row[Reviews.reviewAnswer]
            )
}
