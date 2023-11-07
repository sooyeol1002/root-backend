package com.root.backend.configuration

import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfiguration {

    @Bean
    fun jackson2MessageConverter(): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory,
                       jackson2MessageConverter: Jackson2JsonMessageConverter): RabbitTemplate {
        val rabbitTemplate  = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = jackson2MessageConverter()
        return rabbitTemplate
    }

}