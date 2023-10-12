package com.root.backend.auth

import java.awt.Image


data class SignupRequest (
     val username: String,
     val password: String,
     val nickname: String,
     val email: String,
)

data class AuthProfile (
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

data class Profile (
    val brandName : String, // 브랜드명
    val businessNumber : String, // 사업자번호, 일단 숫자로 가고, 나중에 000-00-00000 으로 갈수도
    val representativeName : String, // 대표자명
    val brandIntro : String, // 브랜드 한줄소개
    val profileImage : String // 프로필사진
)