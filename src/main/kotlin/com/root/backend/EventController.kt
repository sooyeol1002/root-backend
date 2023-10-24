package com.root.backend

import com.root.backend.auth.Auth
import com.root.backend.auth.Event
import com.root.backend.auth.Identities
import com.root.backend.auth.util.JwtUtil
import org.jetbrains.exposed.dao.id.EntityID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/events")
class EventController(private val eventService: EventService) {

    @Auth
    @GetMapping
    fun getEvents(@RequestHeader("Authorization") token: String): ResponseEntity<List<Event>> {
        val authProfile = eventService.validateTokenWithBearer(token) ?: return ResponseEntity.status(401).build()
        val userIdFromToken = EntityID(authProfile.id, Identities)

        val events = eventService.getEvents(token, userIdFromToken)
        return if (events != null) {
            ResponseEntity.ok(events)
        } else {
            ResponseEntity.status(401).build()
        }
    }

    @Auth
    @PostMapping
    fun addEvent(@RequestHeader("Authorization") token: String, @RequestBody event: Event): ResponseEntity<Void> {
        val authProfile = eventService.validateTokenWithBearer(token) ?: return ResponseEntity.status(401).build()
        val userIdFromToken = authProfile.id
        return if (eventService.addEvent(token, event, userIdFromToken)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(500).build()
        }
    }

    @Auth
    @PutMapping("/{id}")
    fun updateEvent(@RequestHeader("Authorization") token: String, @RequestBody event: Event): ResponseEntity<Void> {
        val authProfile = eventService.validateTokenWithBearer(token) ?: return ResponseEntity.status(401).build()
        val userIdFromToken = authProfile.id

        return if (eventService.updateEvent(token, event, userIdFromToken)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(500).build()
        }
    }

    @Auth
    @DeleteMapping("/{eventId}")
    fun deleteEvent(@RequestHeader("Authorization") token: String, @PathVariable eventId: Long): ResponseEntity<Void> {
        val authProfile = eventService.validateTokenWithBearer(token) ?: return ResponseEntity.status(401).build()
        val userIdFromToken = authProfile.id

        return if (eventService.deleteEvent(token, eventId, userIdFromToken)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(500).build()
        }
    }
}