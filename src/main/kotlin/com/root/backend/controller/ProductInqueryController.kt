package com.root.backend.controller

import com.root.backend.ProductInquery
import com.root.backend.ProductInqueryDto
import com.root.backend.toProductInqueryDto
import com.root.backend.productInquery.ProductInqueryService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/inqueries")
class ProductInqueryController(private val productInqueryService: ProductInqueryService) {


    private val logger = LoggerFactory.getLogger(ProductInqueryController::class.java)


    @PostMapping
    fun createInquery(@RequestBody inqueryData: ProductInquery): ResponseEntity<String> {
        return try {
            logger.info("Received POST request to create inquery for product: ${inqueryData.productId}")
            productInqueryService.saveReceivedInquery(inqueryData)
            productInqueryService.sendInqueryToRabbitMQ(inqueryData)
            logger.info("Inquery created successfully with id: ${inqueryData.id}")
            ResponseEntity.status(HttpStatus.CREATED).body("문의가 저장되었습니다.") // 201 Created 상태 코드 반환
        } catch (e: Exception) {
            logger.error("Error creating inquery", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("문의 저장 중 오류가 발생했습니다.")
        }
    }

    @GetMapping("/{productId}")
    fun getInqueriesByProduct(
            @PathVariable productId: String
    ): ResponseEntity<List<ProductInqueryDto>> {
        logger.info("Received GET request for inqueries by product id: $productId")
        val inqueries = productInqueryService.findInqueriesByProduct(productId)
        logger.info("Returning ${inqueries.size} inqueries for product id: $productId")
        return ResponseEntity.ok(inqueries.map { it.toProductInqueryDto() })
    }
}