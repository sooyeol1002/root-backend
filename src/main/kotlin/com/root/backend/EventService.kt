package com.root.backend

import com.root.backend.auth.AuthProfile
import com.root.backend.auth.Event
import com.root.backend.auth.Events
import com.root.backend.auth.Identities
import com.root.backend.auth.util.JwtUtil
import com.root.backend.auth.util.JwtUtil.extractToken
import com.root.backend.auth.util.JwtUtil.validateToken
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventService(private val database: Database) {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    fun getEvents(token: String, userIdFromToken: EntityID<Long>): List<Event>? {
        val authProfile = validateTokenWithBearer(token) ?: return null
        logger.info("Received token length: ${token.length} - Starts with: ${token.take(10)} ... Ends with: ${token.takeLast(10)}")

        return transaction {
            Events.select { Events.eventID eq authProfile.id }.map {
                Event(
                    it[Events.id].value,
                    it[Events.title],
                    it[Events.startDate],
                    it[Events.endDate],
                    it[Events.color]
                )
            }
        }
    }

    fun addEvent(token: String, event: Event, userIdFromToken: Long): Boolean {
        val authProfile = validateTokenWithBearer(token) ?: return false
        logger.info("Received token length: ${token.length} - Starts with: ${token.take(10)} ... Ends with: ${token.takeLast(10)}")

        return transaction {
            try {
                Events.insert {
                    it[eventID] = EntityID(authProfile.id, Identities)
                    it[title] = event.title
                    it[startDate] = event.startDate
                    it[endDate] = event.endDate.minusDays(1)
                    it[color] = event.color
                }
                true
            } catch (e: Exception) {
                logger.error("Error adding event: $e")
                false
            }
        }
    }

    fun updateEvent(token: String, event: Event, userIdFromToken: Long): Boolean {
        val authProfile = validateTokenWithBearer(token) ?: return false

        return transaction {
            try {
                Events.update({ Events.id eq event.id }) {
                    it[title] = event.title
                    it[startDate] = event.startDate
                    it[endDate] = event.endDate.minusDays(1)
                    it[color] = event.color
                } > 0
            } catch (e: Exception) {
                logger.error("Error updating event: $e")
                false
            }
        }
    }

    fun deleteEvent(token: String, eventId: Long, userIdFromToken: Long): Boolean {
        val authProfile = validateTokenWithBearer(token) ?: return false

        return transaction {
            try {
                Events.deleteWhere { Events.id eq eventId and (Events.eventID eq authProfile.id) } > 0
            } catch (e: Exception) {
                logger.error("Error deleting event: $e")
                false
            }
        }
    }
    fun validateTokenWithBearer(bearerToken: String): AuthProfile? {
        val actualToken = extractToken(bearerToken) ?: return null
        return validateToken(actualToken)
    }
}
