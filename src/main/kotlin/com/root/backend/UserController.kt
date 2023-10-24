package com.root.backend

import com.root.backend.auth.*
import com.root.backend.auth.util.JwtUtil
import com.root.backend.auth.util.JwtUtil.extractToken
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@RestController
@RequestMapping("/user")
class UserController(private val authService: AuthService) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)
    private val PROFILE_IMAGE_PATH = "files/profileImage"

    @Auth
    @PostMapping("/register", consumes = ["multipart/form-data"])
    fun registProfile(
        @RequestHeader("Authorization") token: String,
        @RequestParam("brandName") brandName: String,
        @RequestParam("businessNumber") businessNumber: String,
        @RequestParam("representativeName") representativeName: String,
        @RequestParam("brandIntro") brandIntro: String,
        @RequestParam("profileImage") profileImage: MultipartFile

    ): ResponseEntity<String> {
        try {
            logger.info("Registering profile with brandName: $brandName, businessNumber: $businessNumber")

            val profileData = Profile(brandName, businessNumber, representativeName,brandIntro, listOf(profileImage))
            val isSuccess = authService.registerProfile(token, profileData, listOf(profileImage))

            return if (isSuccess) {
                ResponseEntity.ok("프로필 등록 완료.")
            } else {
                logger.error("Profile registration failed for user with token: $token")
                ResponseEntity.badRequest().body("프로필 등록 실패.")
            }
        } catch (e: Exception) {
            logger.error("Error while  registering profile for user with token: $token", e)
            return ResponseEntity.status(500).body("서버 내부 에러.")
        }
    }

    @Auth
    @GetMapping("/profileImage/{userId}/{uuid}")
    fun getProfileImage(@RequestHeader("Authorization") token: String,
                        @PathVariable userId: String,
                        @PathVariable uuid: String
    ): ResponseEntity<ByteArray> {
        val actualToken = extractToken(token) ?: return ResponseEntity.status(403).body(null)
        val authProfile = JwtUtil.validateToken(actualToken)
        if (authProfile == null) {
            return ResponseEntity.status(403).body(null)
        }

        val userIdFromToken = EntityID(authProfile.id, Identities)

        val profileMeta: ResultRow = transaction {
            ProfilesMeta.select { (ProfilesMeta.profileID eq userIdFromToken) and (ProfilesMeta.uuidFileName eq uuid) }
                .singleOrNull()
        } ?: return ResponseEntity.status(404).body(null)

        val uuidFileName = profileMeta[ProfilesMeta.uuidFileName]
        val contentType = profileMeta[ProfilesMeta.contentType]
        val dirPath = Paths.get(PROFILE_IMAGE_PATH)
        val imagePath = dirPath.resolve(uuidFileName)

        if (!Files.exists(imagePath)) {
            return ResponseEntity.status(404).body(null)
        }

        val imageBytes = Files.readAllBytes(imagePath)
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(imageBytes)
    }

    @Auth
    @GetMapping("/brandName")
    fun getBrandName(@RequestHeader("Authorization") token: String): ResponseEntity<String> {
        try {
            val actualToken = extractToken(token) ?: return ResponseEntity.status(403).body(null)
            val authProfile = JwtUtil.validateToken(actualToken)
            if (authProfile == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰 검증 실패")
            }
            val userId = EntityID(authProfile.id, Identities)

            // DB에서 상호명 검색
            val brandName = transaction {
                Profiles.select { Profiles.identityId eq userId }.singleOrNull()?.get(Profiles.brandName)
            } ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("브랜드 정보가 없습니다.")

            return ResponseEntity.ok(brandName)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 에러")
        }
    }

    @Auth
    @GetMapping("/getUserInfo")
    fun getUserInfo(@RequestHeader("Authorization") token: String): ResponseEntity<Map<String, String>> {
        val actualToken = extractToken(token) ?: return ResponseEntity.status(403).body(null)
        val authProfile = JwtUtil.validateToken(actualToken)
        if (authProfile == null) {
            return ResponseEntity.status(403).body(null)
        }

        val userId = authProfile.id

        val profileMeta: ResultRow = transaction {
            ProfilesMeta.select { ProfilesMeta.profileID eq EntityID(userId, Identities) }.singleOrNull()
        } ?: return ResponseEntity.status(404).body(null)

        val uuid = profileMeta[ProfilesMeta.uuidFileName]

            return ResponseEntity.ok(mapOf("userId" to userId.toString(), "uuid" to uuid))
    }
}