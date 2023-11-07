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

@RestController
@RequestMapping("/reviews")
class ReviewController(private val rabbitTemplate: RabbitTemplate,
                       private val reviewService: ReviewService,
                       private val authService: AuthService) {

    @PostMapping
    fun createReview(@RequestBody reviewData: Review): ResponseEntity<String> {
        // reviewData를 사용하여 리뷰를 저장합니다.
        reviewService.saveReceivedReview(reviewData)

        // ReviewResponse 객체 생성 시, 요청받은 원본 ID를 사용합니다.
        val reviewResponse = ReviewResponse(
                id = reviewData.receivedId, // 여기서 reviewData.id는 외부에서 받은 리뷰의 ID입니다.
                productId = reviewData.productId,
                reviewAnswer = null // 리뷰 응답이 null이 아닌 경우 여기에 할당합니다.
        )

        // RabbitMQ를 사용하여 리뷰 응답을 전송
        rabbitTemplate.messageConverter = Jackson2JsonMessageConverter()
        rabbitTemplate.convertAndSend("review-response", reviewResponse)

        // 성공 응답 반환
        return ResponseEntity.ok("리뷰가 처리되었습니다. ID: ${reviewResponse.id}")
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
            @RequestBody reviewAnswerDTO: ReviewAnswerDTO
    ): ResponseEntity<String> {
        var updatedReview: ReviewDto? = null
        transaction {
            // 데이터베이스에 리뷰 답변 업데이트
            Reviews.update({ Reviews.id eq reviewId }) {
                it[reviewAnswer] = reviewAnswerDTO.reviewAnswer
            }
            // 업데이트된 리뷰 정보 가져오기
            updatedReview = selectReviewById(reviewId)?.toReviewDto()
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
            reviewResponse?.let { it1 -> reviewService.sendReviewResponse(it1) }
            return ResponseEntity.ok("{\"message\": \"리뷰 답변이 업데이트 되었습니다.\"}")
        }

        // 리뷰 업데이트에 실패했다면 에러 메시지 반환
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"리뷰를 업데이트할 수 없습니다.\"}")
    }
    private fun selectReviewById(reviewId: Long): Review? {
        return Reviews.select { Reviews.id eq reviewId }
                .mapNotNull { toReview(it) }
                .singleOrNull()
    }
    private fun toReview(row: ResultRow): Review =
            Review(
                    id = row[Reviews.id].value,
                    brandName = row[Reviews.brandName],
                    productId = row[Reviews.productId],
                    birth = row[Reviews.birth],
                    gender = row[Reviews.gender],
                    reviewContent = row[Reviews.reviewContent],
                    scope = row[Reviews.scope],
                    userId = row[Reviews.userId],
                    reviewAnswer = row[Reviews.reviewAnswer],
                    receivedId = row[Reviews.receivedId]
            )

}