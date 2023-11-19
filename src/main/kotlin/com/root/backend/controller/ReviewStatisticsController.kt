package com.root.backend.controller

import com.root.backend.review.ReviewStatisticsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "리뷰통계 API")
@RestController
@RequestMapping("/review-statistics")
class ReviewStatisticsController(private val reviewStatisticsService: ReviewStatisticsService) {

    @Operation(summary = "나이 통계")
    @GetMapping("/age")
    fun getAgeStatistics(@RequestParam brandName: String): Map<String, Int> {
        println("Received brandName: $brandName")
        return reviewStatisticsService.getAgeGroupStatistics(brandName)
    }

    @Operation(summary = "성별 통계")
    @GetMapping("/gender")
    fun getGenderStatistics(@RequestParam brandName: String): Map<String, Int> {
        println("Received brandName: $brandName")
        return reviewStatisticsService.getGenderStatistics(brandName)
    }

    @Operation(summary = "별점 통계")
    @GetMapping("/product-scores")
    fun getProductScoresStatistics(@RequestParam brandName: String): ResponseEntity<String> {
        return try {
            val statistics = reviewStatisticsService.getProductScopeStatistics(brandName)
            ResponseEntity.ok(statistics)
        } catch (e: Exception) {
            e.printStackTrace()
            // 에러 처리
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

}