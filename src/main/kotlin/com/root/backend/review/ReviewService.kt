package com.root.backend.review

import com.root.backend.PagedReviews
import com.root.backend.Review
import com.root.backend.Reviews
import com.root.backend.toReviewDto
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

@Service
class ReviewService(// private val rabbitTemplate: RabbitTemplate,
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
                scope = rs.getInt("scope")
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
}
