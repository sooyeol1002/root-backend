package com.root.backend.auth


data class SignupRequest (
     val username: String,
     val password: String,
     val nickname: String,
     val email: String,
)

data class Profile (
     val id: Long, // 프로필 id
     val username: String, // 로그인 사용자이름
)

data class User(val id: Long, val username: String, val password: String)

val users = listOf(
        User(1L, "user1", "pass1"),
        User(2L, "user2", "pass2"),
        User(3L, "user3", "pass3")
)

data class LoginRequest(val username: String, val password: String)