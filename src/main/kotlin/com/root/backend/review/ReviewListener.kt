package com.root.backend.review

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.root.backend.Review
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class ReviewListener(private val reviewService: ReviewService) {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val objectMapper = jacksonObjectMapper()

    @RabbitListener(queues = ["review-request"])
    fun processReview(message: String) {
        try {
            val review: Review = objectMapper.readValue(message)
            reviewService.saveReceivedReview(review)
            println("Received Review: $review")
        } catch (e: Exception) {
            println("Error processing review: ${e.message}")
            logger.error("Error processing review", e)
        }
    }

//    @RabbitListener(queues = ["review-response"])
//    fun processReviewAnswer(review: Review) {
//        if (review.id != null && review.reviewAnswer != null) {
//            reviewService.updateReviewAnswer(review.id, review.reviewAnswer)
//            println("Review answer updated: $review")
//        } else {
//            println("No action required: Review answer is null or review ID is missing.")
//        }
//    }

}