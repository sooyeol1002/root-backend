package com.root.backend

import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

object Identities : LongIdTable("identity") {
    val secret = varchar("secret", 200)
    val username = varchar("username", length = 100)
}

object Profiles : LongIdTable("profile") {
    val identityId = reference("identity_id", Identities )
    val brandName = varchar("brand_name", 200)
    val businessNumber = varchar("business_number", 12)
    val representativeName = varchar("representative_name", 100)
    val brandIntro = text("brand_intro")
    val originalFileName = varchar("original_file_name", 200)
    val uuidFileName = varchar("uuid", 50).uniqueIndex()
    val contentType = varchar("content_type", 100)
}


object Events : LongIdTable("event") {
    val eventID = reference("identity_id", Identities)
    val title = varchar("title", 255)
    val startDate = date("start_date")
    val endDate = date("end_date")
    val color = varchar("color", 10)
}

object Reviews : LongIdTable("review") {
    val brandName = varchar("brand_name", 255)
    val productNumber = integer("product_number")
    val birthDate = varchar("birth_date", 15)
    val gender = varchar("gender", 10)
    val content = text("content")
    val scope = integer("scope")
}

@Configuration
class AuthTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Identities, Profiles, Events, Reviews)
        }
    }
}

