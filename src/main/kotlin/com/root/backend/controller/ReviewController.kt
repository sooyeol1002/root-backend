package com.root.backend.controller

import com.root.backend.auth.AuthService
import com.root.backend.Review
import com.root.backend.ReviewDto
import com.root.backend.review.ReviewService
import com.root.backend.toReviewDto
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/reviews")
class ReviewController(private val rabbitTemplate: RabbitTemplate,
                       private val reviewService: ReviewService,
                       private val authService: AuthService) {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    @PostMapping
    fun createReview(@RequestBody reviewData: Review): ResponseEntity<String> {

        // RabbitMQ로 메시지 전송
        rabbitTemplate.convertAndSend("reviewExchange", "routingKey", reviewData)

//        reviewService.saveReceivedReview(reviewData)

        return ResponseEntity.ok("RabbitMQ로 전송완료")
    }

    @GetMapping("/get")
    fun getReviewsByBrandName(@RequestHeader("Authorization") token: String): ResponseEntity<List<ReviewDto>> {
        val profile = authService.getUserProfileFromToken(token) ?: return ResponseEntity.badRequest().build()
        val reviews = authService.findReviewsByBrandName(profile.brandName).map { it.toReviewDto() }
        return ResponseEntity.ok(reviews)
    }

}