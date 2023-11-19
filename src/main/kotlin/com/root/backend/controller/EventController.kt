package com.root.backend.controller

import com.root.backend.auth.Auth
import com.root.backend.Event
import com.root.backend.Identities
import com.root.backend.event.EventService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.jetbrains.exposed.dao.id.EntityID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "일정관리API")
@RestController
@RequestMapping("/events")
class EventController(private val eventService: EventService) {

    @Operation(summary = "일정 불러오기", security = [SecurityRequirement(name = "bearer-key")])
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

    @Operation(summary = "일정 추가", security = [SecurityRequirement(name = "bearer-key")])
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

    @Operation(summary = "일정 수정", security = [SecurityRequirement(name = "bearer-key")])
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

    @Operation(summary = "일정 삭제", security = [SecurityRequirement(name = "bearer-key")])
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