package com.root.backend.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic") // 메시지 구독 prefix
        config.setApplicationDestinationPrefixes("/app") // 메시지 전송 prefix
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:5000",
                "http://192.168.100.151:8080",
                "http://192.168.100.151:5000",
                "http://192.168.100.151:5500",
                "http://192.168.100.152:5500",
                "http://192.168.100.152:5000"
        ).withSockJS()
    }
}