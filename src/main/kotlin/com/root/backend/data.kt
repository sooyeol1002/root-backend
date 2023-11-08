package com.root.backend

import org.springframework.web.multipart.MultipartFile
import java.io.Serializable
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

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
        val productId: Long,
        val birth: String,
        val gender: String,
        val reviewContent: String,
        val scope: Int,
        val userId: Long,
        val reviewAnswer: String? = null,
        val receivedId: Long
) : Serializable {
    fun calculateAge(): Int {
        // "yyyymmdd" 형태의 문자열을 LocalDate 객체로 파싱
        val birthDate = LocalDate.parse(
                birth, DateTimeFormatter.ofPattern("yyyyMMdd")
        )
        // 현재 날짜
        val currentDate = LocalDate.now()
        // 나이 계산
        return Period.between(birthDate, currentDate).years
    }
}

data class ReviewDto(
        val id: Long,
        val brandName: String,
        val productId: Long,
        val gender: String,
        val reviewContent: String,
        val age: Int,
        val scope: Int,
        val userId: Long,
        val reviewAnswer: String?,
        val receivedId: Long
)
fun Review.toReviewDto(): ReviewDto {
    return ReviewDto(
            id = this.id,
            brandName = this.brandName,
            productId = this.productId,
            gender = this.gender,
            reviewContent = this.reviewContent,
            age = this.calculateAge(),
            scope = this.scope,
            userId = this.userId,
            reviewAnswer = this.reviewAnswer,
            receivedId = this.receivedId
    )
}
data class ReviewAnswerDTO(
        val reviewAnswer: String
)

data class PagedReviews(
        val reviews: List<Review>,
        val totalPages: Int,
        val totalElements: Int
)

data class ReviewResponse(
        val productId: Long,
        val id: Long,
        val reviewAnswer: String?
)

data class InqueryResponse(
        val id: Long,
        val productId: Long,
        val inqueryAnswer: String?,
        val productName: String
)

data class ProductInquery(
    val id: Long,//
    val receivedId: Long,
    val username: String,
    val productId: Long,//
    val userLoginId: String,
    val productName: String,
    val inqueryCategory: String,
    val inqueryContent: String,
    val inqueryAnswer: String?,//
    val inqueryDate: String
)

data class InqueryAnswerDTO(
    val inqueryAnswer: String
)

data class ProductInqueryDto(
        val id: Long,
        val receivedId: Long,
        val username: String,
        val productId: Long,
        val userLoginId: String,
        val productName: String,
        val inqueryCategory: String,
        val inqueryContent: String,
        val inqueryAnswer: String?,
        val inqueryDate: String
)

fun ProductInquery.toProductInqueryDto(): ProductInqueryDto {
    return ProductInqueryDto(
            id = this.id,
            receivedId = this.receivedId,
            username = this.username,
            productId = this.productId,
            inqueryCategory = this.inqueryCategory,
            inqueryContent = this.inqueryContent,
            inqueryAnswer = this.inqueryAnswer,
            inqueryDate = this.inqueryDate,
            productName = this.productName,
            userLoginId = this.userLoginId
    )
}

data class Brand(
    val id: Long,
    val name: String,
    val representativeName: String,
    val intro: String,
    val imageUuidName: String
)

data class BrandResponse(
    val id: Long,
    val name: String,
    val representativeName: String,
    val intro: String,
    val imageUuidName: String
)