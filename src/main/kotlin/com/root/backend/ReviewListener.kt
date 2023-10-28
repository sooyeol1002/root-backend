package com.root.backend

import com.root.backend.auth.Review
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ReviewListener(private val reviewService: ReviewService) {

    @RabbitListener(queues = ["review-queue"])
    fun processReview(review: Review) {
        try {
            reviewService.saveReceivedReview(review)
            println("Received Review: $review")
        } catch (e: Exception) {
            println("Error processing review: ${e.message}")
        }
    }
}