package com.root.backend

import org.springframework.web.multipart.MultipartFile
import java.io.Serializable
import java.time.LocalDate

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

data class Profile(
        val brandName : String, // 브랜드명
        val businessNumber : String, // 사업자번호, 일단 숫자로 가고, 나중에 000-00-00000 으로 갈수도
        val representativeName : String, // 대표자명
        val brandIntro : String, // 브랜드 한줄소개
        val profileImage : List<MultipartFile>, // 프로필사진
        val originalFileName : String,
        var uuidFileName : String,
        val contentType: String
)

data class Event (
    var id: Long,
    var title: String,
    var startDate: LocalDate,
    var endDate: LocalDate,
    var color: String
)

data class Review(
        val id: Long,
        val brandName: String,
        val productNumber: Int,
        val birthDate: String,
        val gender: String,
        val content: String,
        val scope: Int,
        val userId: Int
) : Serializable {
    fun calculateAge(): Int {
        val birthYear = birthDate.split("-")[0].toInt()
        val currentYear = LocalDate.now().year
        return currentYear - birthYear
    }
}

data class ReviewDto(
        val id: Long,
        val brandName: String,
        val productNumber: Int,
        val gender: String,
        val content: String,
        val age: Int,
        val scope: Int,
        val userId: Int
)
fun Review.toReviewDto(): ReviewDto {
    return ReviewDto(
            id = this.id,
            brandName = this.brandName,
            productNumber = this.productNumber,
            gender = this.gender,
            content = this.content,
            age = this.calculateAge(),
            scope = this.scope,
            userId = this.userId
    )
}

data class PagedReviews(
        val reviews: List<Review>,
        val totalPages: Int,
        val totalElements: Int
)

data class ReviewAnswer(
        var reviewId: Long,
        val productNumber: Int,
        val content: String,
        val userId: Int,
)
data class ReviewAnswerDto(
        val productNumber: Int,
        val content: String,
        val reviewId: Int,
        val userId: Int,
        val id: Long
)
data class ReviewResponse(
        val status: String,
        val message: String
)