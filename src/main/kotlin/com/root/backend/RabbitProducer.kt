package com.root.backend

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/rabbit")
class RabbitController(private val rabbitProducer: RabbitProducer) {
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