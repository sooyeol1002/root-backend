package com.root.backend

import com.root.backend.auth.AuthService
import com.root.backend.auth.Profile
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@RestController
@RequestMapping("/user")
class UserController(private val authService: AuthService) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)

    @PostMapping("/register", consumes = ["multipart/form-data"])
    fun registerProfile(
        @RequestHeader("Authorization") token: String,
        @RequestParam("brandName") brandName: String,
        @RequestParam("businessNumber") businessNumber: String,
        @RequestParam("representativeName") representativeName: String,
        @RequestParam("brandIntro") brandIntro: String,
        @RequestParam("profileImage") profileImage: MultipartFile

    ): ResponseEntity<String> {
        try {
            logger.info("Registering profile with brandName: $brandName, businessNumber: $businessNumber")

            val targetDirectory = "files/profileImage"
            val targetPath = Paths.get(targetDirectory, profileImage.originalFilename)
            Files.copy(profileImage.inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)

            val profileImageList = listOf(profileImage)

            val profileData = Profile(brandName, businessNumber, representativeName,brandIntro, profileImageList)
            val isSuccess = authService.registerProfile(token, profileData, listOf(profileImage))

            return if (isSuccess) {
                ResponseEntity.ok("프로필 등록 완료.")
            } else {
                logger.error("Profile registration failed for user with token: $token")
                ResponseEntity.badRequest().body("프로필 등록 실패.")
            }
        } catch (e: Exception) {
            logger.error("Error while registering profile for user with token: $token", e)
            return ResponseEntity.status(500).body("서버 내부 에러.")
        }
    }
}