package com.root.backend.controller

import com.root.backend.*
import com.root.backend.auth.AuthService
import com.root.backend.review.ReviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Tag(name = "리뷰관리API")
@RestController
@RequestMapping("/reviews")
class ReviewController(private val rabbitTemplate: RabbitTemplate,
                       private val reviewService: ReviewService,
                       private val authService: AuthService) {

    @Operation(summary = "리뷰받기")
    @PostMapping
    fun createReview(@RequestBody reviewData: Review): ResponseEntity<String> {

        val reviewResponse = ReviewResponse(
                id = reviewData.receivedId,
                productId = reviewData.productId,
                reviewAnswer = null,
        )

        println("Sending review response to RabbitMQ: $reviewResponse")
        reviewService.sendReviewResponse(reviewResponse)
//        rabbitTemplate.convertAndSend("review-response", reviewResponse)

        // 성공 응답 반환
        return ResponseEntity.ok("리뷰가 처리되었습니다. ID: ${reviewResponse.id}")
    }

    @Operation(summary = "미답변 리뷰 페이징")
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


    @Operation(summary = "리뷰답변")
    @PutMapping("/{reviewId}/answer")
    fun updateReviewAnswer(
            @PathVariable reviewId: Long,
            @RequestBody reviewAnswerDTO: ReviewAnswerDTO
    ): ResponseEntity<String> {

        transaction {
            Reviews.update({ Reviews.id eq reviewId }) {
                it[reviewAnswer] = reviewAnswerDTO.reviewAnswer
            }
        }


        val updatedReview = reviewService.selectReviewById(reviewId)?.toReviewDto()
        println("Updated Review: $updatedReview")


        updatedReview?.let {
            println("Review Answer: ${it.reviewAnswer}")

            val reviewResponse = it.reviewAnswer?.let { it1 ->
                ReviewResponse(
                        productId = it.productId,
                        id = it.receivedId,
                        reviewAnswer = it1
                )
            }
            println("ReviewResponse object: $reviewResponse")


            if (reviewResponse != null) {
                println("Sending review response to RabbitMQ: $reviewResponse")
                reviewService.sendReviewResponse(reviewResponse)
            } else {
                println("Review response is null, not sending to RabbitMQ")
            }
            return ResponseEntity.ok("{\"message\": \"리뷰 답변이 업데이트 되었습니다.\"}")
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"리뷰를 업데이트할 수 없습니다.\"}")
    }


    @Operation(summary = "답변완료 리뷰 페이징")
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