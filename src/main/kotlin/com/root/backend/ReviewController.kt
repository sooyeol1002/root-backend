package com.root.backend

import com.root.backend.auth.AuthService
import com.root.backend.auth.Review
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/reviews")
class ReviewController(private val rabbitTemplate: RabbitTemplate, private val reviewService: ReviewService) {

    @PostMapping
    fun createReview(@RequestBody reviewData: Review): ResponseEntity<String> {

        // RabbitMQ로 메시지 전송
        rabbitTemplate.convertAndSend("reviewExchange", "routingKey", reviewData)

        reviewService.saveReceivedReview(reviewData)

        return ResponseEntity.ok("RabbitMQ로 전송완료")
    }
}