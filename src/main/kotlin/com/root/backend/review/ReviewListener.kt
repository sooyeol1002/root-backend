package com.root.backend.review

import com.root.backend.Review
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ReviewListener(private val reviewService: ReviewService) {

    @RabbitListener(queues = ["review-request"])
    fun processReview(review: Review) {
        try {
            reviewService.saveReceivedReview(review)
            println("Received Review: $review")
        } catch (e: Exception) {
            println("Error processing review: ${e.message}")
        }
    }

    @RabbitListener(queues = ["review-response"])
    fun processReviewAnswer(review: Review) {
        if (review.id != null && review.reviewAnswer != null) {
            reviewService.updateReviewAnswer(review.id, review.reviewAnswer)
            println("Review answer updated: $review")
        } else {
            println("No action required: Review answer is null or review ID is missing.")
        }
    }
}