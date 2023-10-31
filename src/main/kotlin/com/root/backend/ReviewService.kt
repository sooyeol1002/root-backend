package com.root.backend

import com.root.backend.auth.Review
import com.root.backend.auth.Reviews
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class ReviewService(private val rabbitTemplate: RabbitTemplate) {
    fun saveReceivedReview(review: Review) {
        transaction {
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
