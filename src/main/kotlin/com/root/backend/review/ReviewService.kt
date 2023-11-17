package com.root.backend.review

import com.root.backend.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ReviewService(private val rabbitTemplate: RabbitTemplate,
                    private val messagingTemplate: SimpMessagingTemplate,
                    private val jdbcTemplate: JdbcTemplate) {

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val currentDateTime = LocalDateTime.now().format(formatter)
    private val reviewRowMapper = RowMapper { rs: ResultSet, _: Int ->
        Review(
                id = rs.getLong("id"),
                brandName = rs.getString("brand_name"),
                productId = rs.getLong("product_number"),
                birth = rs.getString("birth_date"),
                gender = rs.getString("gender"),
                reviewContent = rs.getString("content"),
                scope = rs.getInt("scope"),
                userId = rs.getLong("user_id"),
                receivedId = rs.getLong("received_id"),
                reviewAnswer = rs.getString("review_answer"),
                currentTime = rs.getString("review_date")
        )
    }

    fun saveReceivedReview(review: Review): EntityID<Long> {
        val insertedId = transaction {
            Reviews.insert {
                it[brandName] = review.brandName
                it[productId] = review.productId
                it[birth] = review.birth
                it[gender] = review.gender
                it[reviewContent] = review.reviewContent
                it[scope] = review.scope
                it[userId] = review.userId
                it[receivedId] = review.id
                it[currentTime] = currentDateTime
                println("Saving received review with ID: ${review.id}")
            } get Reviews.id
        }

        messagingTemplate.convertAndSend("/topic/review", review.toReviewDto())

        return insertedId
    }

//    fun sendReviewResponse(reviewResponse: ReviewResponse) {
//        rabbitTemplate.convertAndSend("review-response", reviewResponse)
//        println("Review response sent to RabbitMQ: $reviewResponse")
//    }

    fun selectReviewById(reviewId: Long): Review? {
        val sql = "SELECT * FROM review WHERE id = ?"
        return jdbcTemplate.query(sql, reviewRowMapper, reviewId).firstOrNull()
    }

    fun findReviewsByBrandNameWithPaging(brandName: String, page: Int, size: Int): PagedReviews {
        val offset = page * size
        val sql = "SELECT * FROM review WHERE brand_name = ? AND (review_answer IS NULL OR review_answer = '') ORDER BY id DESC LIMIT ? OFFSET ?"
        val reviews = jdbcTemplate.query(sql, reviewRowMapper, brandName, size, offset)

        val totalElementsSql = "SELECT COUNT(*) FROM review WHERE brand_name = ? AND (review_answer IS NULL OR review_answer = '')"
        val totalElements = jdbcTemplate.queryForObject(totalElementsSql, Int::class.java, brandName)

        val totalPages = (totalElements + size - 1) / size

        return PagedReviews(reviews, totalPages, totalElements)
    }

    fun findAnsweredReviewsWithPaging(brandName: String, page: Int, size: Int): PagedReviews {
        val offset = page * size
        val sql = "SELECT * FROM review WHERE review_answer IS NOT NULL AND review_answer <> '' AND brand_name = ? ORDER BY id DESC LIMIT ? OFFSET ?"
        val reviews = jdbcTemplate.query(sql, reviewRowMapper, brandName, size, offset)

        val countSql = "SELECT COUNT(*) FROM review WHERE review_answer IS NOT NULL AND review_answer <> '' AND brand_name = ?"
        val totalReviews = jdbcTemplate.queryForObject(countSql, Long::class.java, brandName) ?: 0L

        val totalPages = (totalReviews + size - 1) / size

        return PagedReviews(reviews, totalPages.toInt(), totalReviews.toInt())
    }

}
