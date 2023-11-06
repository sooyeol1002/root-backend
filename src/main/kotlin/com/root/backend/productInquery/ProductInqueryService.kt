package com.root.backend.productInquery

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.root.backend.ProductInqueries
import com.root.backend.ProductInquery
import com.root.backend.ProductInqueryDto
import com.root.backend.toProductInqueryDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ProductInqueryService(private val rabbitTemplate: RabbitTemplate) {

    private val logger = LoggerFactory.getLogger(ProductInqueryService::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    fun saveReceivedInquery(inquery: ProductInquery) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDateTime = LocalDateTime.now().format(formatter)
        transaction {
            ProductInqueries.insert {
                it[userLoginId] = inquery.userLoginId
                it[username] = inquery.username
                it[productId] = inquery.productId
                it[inqueryCategory] = inquery.inqueryCategory
                it[inqueryContent] = inquery.inqueryContent
                it[inqueryAnswer] = inquery.inqueryAnswer
                it[inqueryDate] = currentDateTime
            }
        }

    }
    fun sendInqueryToRabbitMQ(inquery: ProductInquery) {
        try {
            logger.debug("Attempting to send inquery to RabbitMQ: $inquery") // 직렬화 전 객체 상태 로깅
            val message = objectMapper.writeValueAsString(inquery)
            rabbitTemplate.convertAndSend("product_inquery_exchange", "product_inquery_routing_key", message)
            logger.info("Inquery sent to RabbitMQ: $message")
        } catch (e: Exception) {
            logger.error("Failed to send inquery to RabbitMQ", e)
            throw e
        }
    }

    fun ResultRow.toProductInquery(): ProductInquery {
        return ProductInquery(
                id = this[ProductInqueries.id].value,
                userLoginId = this[ProductInqueries.userLoginId],
                username = this[ProductInqueries.username],
                productId = this[ProductInqueries.productId],
                inqueryCategory = this[ProductInqueries.inqueryCategory],
                inqueryContent = this[ProductInqueries.inqueryContent],
                inqueryAnswer = this[ProductInqueries.inqueryAnswer],
                inqueryDate = this[ProductInqueries.inqueryDate]
        )
    }

    fun findInqueriesByProduct(productId: String): List<ProductInquery> =
            transaction {
        ProductInqueries.select { ProductInqueries.productId eq productId }
                .map { it.toProductInquery() }
    }

    fun updateInqueryAnswer(inqueryId: Long, answer: String) {
        var updatedInquery: ProductInqueryDto? = null
        transaction {
            ProductInqueries.update({ ProductInqueries.id eq inqueryId }) {
                it[inqueryAnswer] = answer
            }
            updatedInquery = selectInqueryById(inqueryId)?.toProductInqueryDto()
        }

        updatedInquery?.let {
            sendInqueryToRabbitMQ(it)
            logger.info("문의 답변이 업데이트되었고 RabbitMQ로 메시지가 전송되었습니다: $it")
        }
    }

    private fun selectInqueryById(inquiryId: Long): ProductInquery? {
        return ProductInqueries.select { ProductInqueries.id eq inquiryId }
                .mapNotNull { it.toProductInquery() }
                .singleOrNull()
    }
    fun sendInqueryToRabbitMQ(inqueryDto: ProductInqueryDto) {
        try {
            logger.debug("RabbitMQ로 문의를 전송하려고 시도합니다: $inqueryDto") // 직렬화 전 객체 상태 로깅
            val message = objectMapper.writeValueAsString(inqueryDto)
            rabbitTemplate.convertAndSend(message)
            logger.info("Inquiry DTO가 RabbitMQ로 전송되었습니다: $message")
        } catch (e: Exception) {
            logger.error("Inquiry DTO를 RabbitMQ로 전송하는 데 실패했습니다", e)
            throw e
        }
    }
}