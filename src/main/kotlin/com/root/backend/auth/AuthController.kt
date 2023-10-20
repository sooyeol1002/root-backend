package com.root.backend.auth

import com.root.backend.auth.util.JwtUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.apache.tomcat.util.http.parser.Authorization
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthController(private val service: AuthService) {

    //1. (브라우저) 로그인 요청
    // [RequestLine]
    //   HTTP 1.1 POST 로그인주소
    // [RequestHeader]
    //   content-type: www-form-urlencoded
    // [Body]
    //   id=...&pw=...
    //2. (서버) 로그인 요청을 받고 인증처리 후 쿠키 응답 및 웹페이지로 이동
    // HTTP Status 302 (리다이렉트)
    // [Response Header]
    //   Set-Cookie: 인증키=키........; domain=.naver.com
    //   Location: "리다이렉트 주소"
    //3. (브라우저) 쿠키를 생성(도메인에 맞게)

    @Auth
    @PostMapping("/login")
    fun login(
            @RequestBody loginRequest: LoginRequest,
            res: HttpServletResponse,
            @RequestHeader("Authorization") authorization: String?
    ): ResponseEntity<*> {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("status" to "error", "message" to "No Authorization header provided"))
        }

        val token = authorization.substring(7)

        val (result, message) =
            service.authenticate(loginRequest.username, loginRequest.password)
        println(loginRequest.username)
        println(loginRequest.password)

        if (result) {
            val generatedToken  = message
            println("Token Token : $generatedToken")

            val cookie = Cookie("token", generatedToken)
            cookie.path = "/"
            cookie.maxAge = (JwtUtil.TOKEN_TIMEOUT / 1000L).toInt()
            cookie.domain = "localhost"

            res.addCookie(cookie)

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(mapOf("status" to "success",
                            "token" to generatedToken,
                            "redirectUrl" to "http://localhost:5000/home"))
        } else {
            println("Login failed: $message")
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED).body(mapOf("status" to "error", "message" to message))

        }
    }
}