package com.root.backend.controller

import com.root.backend.review.ReviewStatisticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/review-statistics")
class ReviewStatisticsController(private val reviewStatisticsService: ReviewStatisticsService) {

    @GetMapping("/age")
    fun getAgeStatistics(@RequestParam brandName: String): Map<String, Int> {
        println("Received brandName: $brandName")
        return reviewStatisticsService.getAgeGroupStatistics(brandName)
    }

    @GetMapping("/gender")
    fun getGenderStatistics(@RequestParam brandName: String): Map<String, Int> {
        println("Received brandName: $brandName")
        return reviewStatisticsService.getGenderStatistics(brandName)
    }
}