package com.root.backend.controller

import com.root.backend.*
import com.root.backend.productInquery.ProductInqueryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/inqueries")
class ProductInqueryController(private val rabbitTemplate: RabbitTemplate,
                               private val productInqueryService: ProductInqueryService) {


    private val logger = LoggerFactory.getLogger(ProductInqueryController::class.java)


    @PostMapping
    fun createInquery(@RequestBody inqueryData: ProductInquery): ResponseEntity<String> {

        val inqueryResponse = InqueryResponse(
            id = inqueryData.receivedId,
            productId = inqueryData.productId,
            productName = inqueryData.productName,
            inqueryAnswer = null
        )

        productInqueryService.sendInqueryResponse(inqueryResponse)
        rabbitTemplate.convertAndSend("inquery-response", inqueryResponse)

        return ResponseEntity.ok("문의가 처리되었습니다. ID: ${inqueryResponse.id}")
    }

    @GetMapping("/unanswered")
    fun getUnansweredInquiries(
        @RequestHeader("Authorization") token: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val unansweredInquiries = productInqueryService.findUnansweredInquiriesWithPaging(page, size)
        val response: Map<String, Any> = mapOf(
            "content" to unansweredInquiries.inqueries.map { it.toProductInqueryDto() },
            "totalPages" to unansweredInquiries.totalPages,
            "totalElements" to unansweredInquiries.totalElements,
            "currentPage" to page
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/answered")
    fun getAnsweredInquiries(
        @RequestHeader("Authorization") token: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val answeredInquiries = productInqueryService.findAnsweredInquiriesWithPaging(page, size)
        val response: Map<String, Any> = mapOf(
            "content" to answeredInquiries.inqueries.map { it.toProductInqueryDto() },
            "totalPages" to answeredInquiries.totalPages,
            "totalElements" to answeredInquiries.totalElements,
            "currentPage" to page
        )
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{inqueryId}/answer")
    fun updateInqueryAnswer(
        @PathVariable inqueryId: Long,
        @RequestBody inqueryAnswerDTO: InqueryAnswerDTO
    ): ResponseEntity<String> {
//        val existingInquery = productInqueryService.selectInqueryById(inqueryId)
//
//        if (existingInquery != null && existingInquery.inqueryAnswer != null) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"error\": \"이미 답변이 등록된 문의입니다.\"}")
//        }

        var updatedInquery: ProductInqueryDto? = null

        transaction {
            ProductInqueries.update ({ ProductInqueries.id eq inqueryId }) {
                it[inqueryAnswer] = inqueryAnswerDTO.inqueryAnswer
            }
            updatedInquery = selectInqueryById(inqueryId)?.toProductInqueryDto()
        }

        updatedInquery?.let {
            val inqueryResponse = it.inqueryAnswer?.let { it2 ->
                InqueryResponse(
                    id = it.receivedId,
                    productId = it.productId,
                    productName = it.productName,
                    inqueryAnswer = it2
                )
            }

//            inqueryResponse?.let { it2 -> productInqueryService.sendInqueryResponse(it2)}
            inqueryResponse?.let { it1 -> rabbitTemplate.convertAndSend("inquery-response", it1) }
            return ResponseEntity.ok("{\"message\": \"문의 답변이 업데이트 되었습니다.\"}")
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"문의를 업데이트할 수 없습니다.\"}")
    }
    private fun selectInqueryById(inqueryId: Long): ProductInquery? {
        return ProductInqueries.select { ProductInqueries.id eq inqueryId }
            .mapNotNull { toProductInquery(it) }.singleOrNull()
    }
    private fun toProductInquery(row: ResultRow): ProductInquery =
        ProductInquery(
                    id = row[ProductInqueries.id].value,
                    receivedId = row[ProductInqueries.receivedId],
                    username = row[ProductInqueries.username],
                    productId = row[ProductInqueries.productId],//
                    productName = row[ProductInqueries.productName],
                    inqueryCategory = row[ProductInqueries.inqueryCategory],
                    inqueryContent = row[ProductInqueries.inqueryContent],
                    inqueryAnswer =  row[ProductInqueries.inqueryAnswer],//
                    inqueryDate = row[ProductInqueries.inqueryDate],
                    userLoginId = row[ProductInqueries.userLoginId]
        )
}