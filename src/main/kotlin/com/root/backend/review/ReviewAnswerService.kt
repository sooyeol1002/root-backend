package com.root.backend.review

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.root.backend.ReviewAnswer
import com.root.backend.ReviewAnswers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class ReviewAnswerService(private val rabbitTemplate: RabbitTemplate) {
    val mapper = jacksonObjectMapper()

    fun handleReviewAnswer(reviewAnswer: ReviewAnswer) {
        // 데이터베이스에 리뷰 응답 저장
        saveReviewAnswerToDatabase(reviewAnswer)

        // RabbitMQ로 메시지 보내기
//        createReviewAnswerMessage(reviewAnswer)
    }
    private fun saveReviewAnswerToDatabase(reviewAnswer: ReviewAnswer): Long {
        // ReviewAnswers 테이블에 데이터를 삽입하는 트랜잭션
        return transaction {
            val inserted = ReviewAnswers.insert {
                it[reviewId] = reviewAnswer.reviewId
                it[productNumber] = reviewAnswer.productNumber
                it[content] = reviewAnswer.content
                it[userId] = reviewAnswer.userId
            }
            inserted[ReviewAnswers.id].value
        }
    }

    private fun createReviewAnswerMessage(reviewAnswer: ReviewAnswer) {
        // RabbitMQ의 'review-answer-queue'로 리뷰 응답 JSON 메시지 전송
        rabbitTemplate.convertAndSend("review-answer-queue", mapper.writeValueAsString(reviewAnswer))
    }
}