package com.root.backend

import com.root.backend.auth.AuthService
import com.root.backend.auth.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(private val authService: AuthService) {

    @PostMapping("/register")
    fun registerProfile(
        @RequestHeader("Authorization") token: String,
        @RequestBody profileData: Profile
    ): ResponseEntity<String> {
        val isSuccess = authService.registerProfile(token, profileData)

        return if (isSuccess) {
            ResponseEntity.ok("프로필 등록 완료.")
        } else {
            ResponseEntity.badRequest().body("프로필 등록 실패.")
        }
    }
}