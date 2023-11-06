package com.root.backend.productInquery

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.root.backend.ProductInquery
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class ProductInqueryListener(private val productInqueryService: ProductInqueryService) {
    private val logger = LoggerFactory.getLogger(ProductInqueryService::class.java)
    @RabbitListener(queues = ["product-inquery"])
    fun receiveInquery(message: String) {
        try {
            // 메시지는 JSON 형식일 수 있으니, 이를 ProductInquery 객체로 변환합니다.
            val objectMapper = jacksonObjectMapper()
            val inquery: ProductInquery = objectMapper.readValue(message)

            // 로그를 출력하여 메시지를 받았음을 알립니다.
            logger.info("Received inquery message: $message")

            // 문의를 저장하는 메소드를 호출합니다.
            productInqueryService.saveReceivedInquery(inquery)

            // 성공 로그를 출력합니다.
            logger.info("Inquery saved successfully for userLoginId: ${inquery.userLoginId}")
        } catch (e: Exception) {
            // 예외가 발생할 경우 에러 로그를 출력합니다.
            logger.error("Error processing inquery message: $message", e)
        }
    }

    @RabbitListener(queues = ["inquery-response"])
    fun processInqueryAnswer(inquery: ProductInquery) {
        if (inquery.id != null && inquery.inqueryAnswer != null) {
            try {
                productInqueryService.updateInqueryAnswer(inquery.id, inquery.inqueryAnswer)
                println("Inquery answer updated: $inquery")
            } catch (e: Exception) {
                println("Error updating inquery answer: ${e.message}")
            }
        } else {
            println("No action required: Inquery answer is null or inquery ID is missing.")
        }
    }
}