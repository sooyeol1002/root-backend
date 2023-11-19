package com.root.backend

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Tag(name = "RabbitMQ API")
@RestController
@RequestMapping("/rabbit")
class RabbitController(private val rabbitProducer: RabbitProducer) {

    @Operation(summary = "메세지 전송")
    @PostMapping("/message")
    fun sendMessage(@RequestBody message: String) {
        rabbitProducer.send(message)
    }
}
@Service
class RabbitProducer(private val rabbitTemplate: RabbitTemplate) {
    fun send(message: String) {
        rabbitTemplate.convertAndSend("my-queue", message)
    }
}