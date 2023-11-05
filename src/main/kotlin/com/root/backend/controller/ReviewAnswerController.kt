package com.root.backend.controller

import com.root.backend.ReviewAnswer
import com.root.backend.ReviewAnswerDto
import com.root.backend.review.ReviewAnswerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/review-answers")
class ReviewAnswerController(private val reviewAnswerService: ReviewAnswerService) {

    @PostMapping("/{reviewId}/answer")
    fun saveReviewAnswer(
            @PathVariable reviewId: Long,
            @RequestBody reviewAnswerDto: ReviewAnswerDto
    ): ResponseEntity<Any> {
        // 리뷰 응답 객체 생성
        val reviewAnswer = ReviewAnswer(
                reviewId = reviewId,
                productNumber = reviewAnswerDto.productNumber,
                content = reviewAnswerDto.content,
                userId = reviewAnswerDto.userId,
        )

        // 리뷰 응답 처리
        reviewAnswerService.handleReviewAnswer(reviewAnswer)

        // 성공적으로 처리되었음을 응답
        return ResponseEntity.status(HttpStatus.CREATED).body("Review answer saved and message sent.")
    }
}
