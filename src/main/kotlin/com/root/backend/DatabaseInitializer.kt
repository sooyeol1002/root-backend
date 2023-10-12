package com.root.backend

import com.root.backend.auth.Identities
import com.root.backend.auth.User
import com.root.backend.auth.util.HashUtil
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DatabaseInitializer(private val database: Database) {

    @PostConstruct
    fun init() {
        insertDummyData()
    }

    private fun insertDummyData() {
        transaction {
            // 데이터가 이미 존재하는지 확인
            val existingUsernames = Identities.select { Identities.username inList listOf("user1", "user2", "user3") }
                .map { it[Identities.username] }

            val usersToInsert = listOf(
                User(1L, "user1", "pass1"),
                User(2L, "user2", "pass2"),
                User(3L, "user3", "pass3")
            ).filter { it.username !in existingUsernames }

            for (user in usersToInsert) {
                Identities.insert {
                    it[id] = EntityID(user.id, Identities)
                    it[username] = user.username
                    it[secret] = HashUtil.createHash(user.password)  // 비밀번호를 해싱하여 저장
                }
            }
        }
    }
}