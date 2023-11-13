package com.root.backend.productInquery

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.root.backend.*
import com.root.backend.Reviews.brandName
import com.root.backend.controller.UserController
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ProductInqueryService(private val rabbitTemplate: RabbitTemplate,
                            private val jdbcTemplate: JdbcTemplate) {
    private val inqueryRowMapper = RowMapper { rs: ResultSet, _: Int ->
        ProductInquery(
            id = rs.getLong("id"),
            receivedId = rs.getLong("received_id"),
            username = rs.getString("username"),
            productId = rs.getLong("product_id"),
            inqueryCategory = rs.getString("inquery_category"),
            inqueryContent = rs.getString("inquery_content"),
            inqueryAnswer = rs.getString("inquery_answer"),
            inqueryDate = rs.getString("inquery_date"),
            productName = rs.getString("product_name"),
            userLoginId = rs.getString("user_login_id")
        )
    }

    private val logger = LoggerFactory.getLogger(ProductInqueryService::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    fun saveReceivedInquery(inquery: ProductInquery): EntityID<Long> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDateTime = LocalDateTime.now().format(formatter)
        val insertedId = transaction {
            ProductInqueries.insert {
                it[receivedId] = inquery.id
                it[username] = inquery.username
                it[productId] = inquery.productId
                it[inqueryCategory] = inquery.inqueryCategory
                it[inqueryContent] = inquery.inqueryContent
                it[inqueryAnswer] = inquery.inqueryAnswer
                it[inqueryDate] = currentDateTime
                it[productName] = inquery.productName
                it[userLoginId] = inquery.userLoginId
            }
        } get ProductInqueries.id

        return insertedId
    }
    fun sendInqueryResponse(inqueryResponse: InqueryResponse) {
        rabbitTemplate.convertAndSend("product_inquery", inqueryResponse)
        println("문의 답변 RabbitMQ로 전송완료: $inqueryResponse")
    }

    fun selectInqueryById(inqueryId: Long): ProductInquery? {
        val sql = "SELECT * FROM product_inquery WHERE id = ?"
        return jdbcTemplate.query(sql, inqueryRowMapper, inqueryId).firstOrNull()
    }

    fun findUnansweredInquiriesWithPaging(page: Int, size: Int, brandName: String): PagedProductInqueries {
        val offset = page * size
        val likePattern = "%$brandName%"
        val sql = "SELECT * FROM product_inquery " +
                "WHERE (inquery_answer IS NULL OR inquery_answer = '') " +
                "AND product_name LIKE ? LIMIT ? OFFSET ?"
        val inqueries = jdbcTemplate.query(sql, inqueryRowMapper, likePattern, size, offset)

        val countSql = "SELECT COUNT(*) FROM product_inquery " +
                "WHERE (inquery_answer IS NULL OR inquery_answer = '') " +
                "AND product_name LIKE ?"
        val totalInquiries = jdbcTemplate.queryForObject(countSql, arrayOf(likePattern), Int::class.java)

        val totalPages = (totalInquiries + size - 1) / size

        return PagedProductInqueries(inqueries, totalPages, totalInquiries)
    }

    fun findAnsweredInquiriesWithPaging(page: Int, size: Int, brandName: String): PagedProductInqueries {
        val offset = page * size
        val likePattern = "%${brandName}%"
        val sql = "SELECT * FROM product_inquery " +
                "WHERE inquery_answer IS NOT NULL " +
                "AND inquery_answer <> '' " +
                "AND product_name LIKE ? LIMIT ? OFFSET ?"
        val inquiries = jdbcTemplate.query(sql, inqueryRowMapper, likePattern, size, offset)

        val countSql = "SELECT COUNT(*) FROM product_inquery " +
                "WHERE inquery_answer IS NOT NULL " +
                "AND inquery_answer <> '' AND product_name LIKE ?"
        val totalInquiries = jdbcTemplate.queryForObject(countSql, arrayOf(likePattern), Int::class.java)

        val totalPages = (totalInquiries + size - 1) / size

        return PagedProductInqueries(inquiries, totalPages, totalInquiries)
    }

//    fun ResultRow.toProductInquery(): ProductInquery {
//        return ProductInquery(
//                id = this[ProductInqueries.id].value,
//                receivedId = this[ProductInqueries.receivedId],
//                username = this[ProductInqueries.username],
//                productId = this[ProductInqueries.productId],
//                inqueryCategory = this[ProductInqueries.inqueryCategory],
//                inqueryContent = this[ProductInqueries.inqueryContent],
//                inqueryAnswer = this[ProductInqueries.inqueryAnswer],
//                inqueryDate = this[ProductInqueries.inqueryDate]
//        )
//    }

//    fun findInqueriesByProduct(productId: String): List<ProductInquery> =
//            transaction {
//        ProductInqueries.select { ProductInqueries.productId eq productId }
//                .map { it.toProductInquery() }
//    }
//
//    fun updateInqueryAnswer(inqueryId: Long, answer: String) {
//        var updatedInquery: ProductInqueryDto? = null
//        transaction {
//            ProductInqueries.update({ ProductInqueries.id eq inqueryId }) {
//                it[inqueryAnswer] = answer
//            }
//            updatedInquery = selectInqueryById(inqueryId)?.toProductInqueryDto()
//        }
//
//        updatedInquery?.let {
//            sendInqueryToRabbitMQ(it)
//            logger.info("문의 답변이 업데이트되었고 RabbitMQ로 메시지가 전송되었습니다: $it")
//        }
//    }
//
//    private fun selectInqueryById(inquiryId: Long): ProductInquery? {
//        return ProductInqueries.select { ProductInqueries.id eq inquiryId }
//                .mapNotNull { it.toProductInquery() }
//                .singleOrNull()
//    }

}