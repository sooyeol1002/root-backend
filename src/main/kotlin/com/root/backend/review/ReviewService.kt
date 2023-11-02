package com.root.backend.review

import com.root.backend.Review
import com.root.backend.Reviews
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class ReviewService(// private val rabbitTemplate: RabbitTemplate,
                    private val messagingTemplate: SimpMessagingTemplate) {
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

        messagingTemplate.convertAndSend("/topic/review", review)
    }
}
