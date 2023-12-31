package com.root.backend.controller

import com.root.backend.auth.AuthService
import com.root.backend.auth.util.JwtUtil
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*

@Tag(name="인증처리API")
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

    @PostMapping("/login")
    fun login(
            @RequestParam("username") username: String,
            @RequestParam("password") password: String,
            res: HttpServletResponse,): ResponseEntity<*> {

        val (result, message) = service.authenticate(username, password)
        println(username)
        println(password)

        if (result) {
            val generatedToken  = message
            println("Token : $generatedToken")

            val cookie = Cookie("token", generatedToken)
            cookie.path = "/"
            cookie.maxAge = (JwtUtil.TOKEN_TIMEOUT / 1000L).toInt()
            cookie.domain = "192.168.100.152"

            res.addCookie(cookie)

            val redirectUrl = "http://192.168.100.152:5000/home"
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(
                            ServletUriComponentsBuilder
                                    .fromHttpUrl(redirectUrl)
                                    .build().toUri()
                    )
                    .build<Any>()
        } else {
            println("Login failed: $message")
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED).body(mapOf("status" to "error", "message" to message))

        }
    }
}