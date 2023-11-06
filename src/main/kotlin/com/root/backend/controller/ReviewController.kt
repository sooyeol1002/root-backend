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

        rabbitTemplate.convertAndSend(reviewData)

        return ResponseEntity.ok("RabbitMQ로 전송완료")
    }

    @GetMapping("/get")
    fun getReviewsByBrandName(
            @RequestHeader("Authorization") token: String,
            @RequestParam(defaultValue = "0") page: Int,
            @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val profile = authService.getUserProfileFromToken(token) ?: return ResponseEntity.badRequest().build()
        val reviewsPage = reviewService.findReviewsByBrandNameWithPaging(profile.brandName, page, size)
        val response: Map<String, Any> = mapOf(
                "content" to reviewsPage.reviews.map { it.toReviewDto() },
                "totalPages" to reviewsPage.totalPages,
                "totalElements" to reviewsPage.totalElements,
                "currentPage" to page
        )
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{reviewId}/answer")
    fun updateReviewAnswer(
            @PathVariable reviewId: Long,
            @RequestBody answer: String
    ): ResponseEntity<String> {
        reviewService.updateReviewAnswer(reviewId, answer)
        return ResponseEntity.ok("리뷰 답변이 업데이트 되었습니다.")
    }

}