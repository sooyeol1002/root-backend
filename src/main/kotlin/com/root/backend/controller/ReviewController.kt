package com.root.backend.controller

import com.root.backend.*
import com.root.backend.auth.AuthService
import com.root.backend.review.ReviewService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/reviews")
class ReviewController(private val rabbitTemplate: RabbitTemplate,
                       private val reviewService: ReviewService,
                       private val authService: AuthService) {

    @PostMapping
    fun createReview(@RequestBody reviewData: Review): ResponseEntity<String> {

//        val savedReviewId = reviewService.saveReceivedReview(reviewData)

        val reviewResponse = ReviewResponse(
                id = reviewData.receivedId,
                productId = reviewData.productId,
                reviewAnswer = null
        )

//        reviewService.sendReviewResponse(reviewResponse)
//        rabbitTemplate.convertAndSend("review-response", reviewResponse)

        // 성공 응답 반환
        return ResponseEntity.ok("리뷰가 처리되었습니다. ID: ${reviewResponse.id}")
    }

    @GetMapping("/unanswered")
    fun getReviewsByBrandName(
            @RequestHeader("Authorization") token: String,
            @RequestParam(defaultValue = "0") page: Int,
            @RequestParam(defaultValue = "5") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val profile = authService.getUserProfileFromToken(token) ?: return ResponseEntity.badRequest().build()
        val reviewsPage = reviewService.findReviewsByBrandNameWithPaging(profile.brandName, page, size)
        val response: Map<String, Any> = mapOf(
                "content" to reviewsPage.reviews.map { it.toReviewDto() },
                "totalPages" to reviewsPage.totalPages,
                "totalElements" to reviewsPage.totalElements,
                "currentPage" to page
        )
        println("----------------------------------------unanswered$response")
        println(reviewsPage.reviews.map { it.toReviewDto() })
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{reviewId}/answer")
    fun updateReviewAnswer(
            @PathVariable reviewId: Long,
            @RequestBody reviewAnswerDTO: ReviewAnswerDTO
    ): ResponseEntity<String> {
        val existingReview = reviewService.selectReviewById(reviewId)

        var updatedReview: ReviewDto? = null
        transaction {
            Reviews.update({ Reviews.id eq reviewId }) {
                it[reviewAnswer] = reviewAnswerDTO.reviewAnswer
            }
            updatedReview = existingReview?.toReviewDto()
        }

        // 업데이트된 리뷰가 있으면 RabbitMQ로 전송
        updatedReview?.let {
            val reviewResponse = it.reviewAnswer?.let { it1 ->
                ReviewResponse(
                        productId = it.productId,
                        id = it.receivedId,
                        reviewAnswer = it1
                )
            }
//            reviewResponse?.let { it1 -> reviewService.sendReviewResponse(it1) }
            return ResponseEntity.ok("{\"message\": \"리뷰 답변이 업데이트 되었습니다.\"}")
        }

        // 리뷰 업데이트에 실패했다면 에러 메시지 반환
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"리뷰를 업데이트할 수 없습니다.\"}")
    }

    @GetMapping("/answered")
    fun getAnsweredReviewsPaged(
        @RequestHeader("Authorization") token: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val profile = authService.getUserProfileFromToken(token) ?: return ResponseEntity.badRequest().build()
        val answeredReviewsPage = reviewService.findAnsweredReviewsWithPaging(profile.brandName, page, size)
        val response: Map<String, Any> = mapOf(
            "content" to answeredReviewsPage.reviews.map { it.toReviewDto() },
            "totalPages" to answeredReviewsPage.totalPages,
            "totalElements" to answeredReviewsPage.totalElements,
            "currentPage" to page
        )

        println("-----------------------------answered$response")
        println(answeredReviewsPage.reviews.map { it.toReviewDto() })
        // 페이징된 데이터 응답
        return ResponseEntity.ok(response)
    }

}