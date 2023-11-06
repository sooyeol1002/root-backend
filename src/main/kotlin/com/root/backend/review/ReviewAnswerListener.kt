package com.root.backend.review

import com.root.backend.ReviewAnswer
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class ReviewAnswerListener(private val reviewAnswerService: ReviewAnswerService) {
    @RabbitListener(queues = ["review-answer-queue"])
    fun receiveReviewAnswer(@Payload reviewAnswerJson: String) {
        try {
            // JSON 문자열을 ReviewAnswer 객체로 변환
            val reviewAnswer = reviewAnswerService.mapper.readValue(reviewAnswerJson, ReviewAnswer::class.java)

            // ReviewAnswer 객체를 처리
            reviewAnswerService.saveReviewAnswerToDatabase(reviewAnswer)

            println("Review Answer processed successfully: $reviewAnswer")

        } catch (e: Exception) {
            // 추후 서버 간 통신을 구현할 때 이 부분의 예외 처리를 수정할 수 있도록 주석을 남겨둡니다.
            println("Error while processing Review Answer, message will be discarded: ${e.message}")
        }
    }
}