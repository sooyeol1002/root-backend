package com.root.backend.productInquery

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.root.backend.ProductInquery
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ProductInqueryListener(private val productInqueryService: ProductInqueryService) {
    private val logger = LoggerFactory.getLogger(ProductInqueryService::class.java)
    private val objectMapper = jacksonObjectMapper()
    @RabbitListener(queues = ["product-inquery"])
    fun receiveInquery(message: String) {
        try {
            val inquery: ProductInquery = objectMapper.readValue(message)
            productInqueryService.saveReceivedInquery(inquery)
        } catch (e: Exception) {
            logger.error("Error processing inquery message: $message", e)
        }
    }

//    @RabbitListener(queues = ["inquery-response"])
//    fun processInqueryAnswer(inquery: ProductInquery) {
//        if (inquery.id != null && inquery.inqueryAnswer != null) {
//            try {
//                productInqueryService.updateInqueryAnswer(inquery.id, inquery.inqueryAnswer)
//                println("Inquery answer updated: $inquery")
//            } catch (e: Exception) {
//                println("Error updating inquery answer: ${e.message}")
//            }
//        } else {
//            println("No action required: Inquery answer is null or inquery ID is missing.")
//        }
//    }
}