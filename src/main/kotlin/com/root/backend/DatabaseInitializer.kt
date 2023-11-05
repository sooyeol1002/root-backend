package com.root.backend

import com.root.backend.auth.util.HashUtil
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DatabaseInitializer(private val database: Database) {

    @PostConstruct
    fun init() {
        insertDummyData()
        insertDummyReviewData()
    }

    private fun insertDummyData() {
        transaction {
            // 데이터가 이미 존재하는지 확인
            val existingUsernames = Identities.select { Identities.username inList listOf("user1", "user2", "user3") }
                .map { it[Identities.username] }

            val usersToInsert = listOf(
                User(1L, "user1", "pass1"),
                User(2L, "user2", "pass2")
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

    private fun insertDummyReviewData() {
        transaction {
            val existingReviews = Reviews.selectAll().count()

            // 이미 리뷰 데이터가 있다면, 중복 데이터를 피하기 위해 함수 종료
            if (existingReviews > 0) return@transaction

            val reviewsToInsert = listOf(
                    Review(1, "듀랑고", 101, "1990-01-01", "남", "듀랑고 텐트의 내구성이 매우 만족스럽습니다.", 5, 1),
                    Review(2, "코베아", 116, "1985-10-02", "여", "코베아 스토브 사용이 편리하고 안정적입니다.", 2, 2),
                    Review(3, "듀랑고", 102, "2000-10-27", "남", "캠핑용 의자는 경량이면서도 편안했어요.", 3, 3),
                    Review(4, "코베아", 117, "1992-05-16", "여", "그릴 성능이 좋고 청소가 간편합니다.", 4, 4),
                    Review(5, "듀랑고", 103, "1998-03-20", "남", "가격은 높지만 품질이 그만큼 우수합니다.", 1, 5),
                    Review(6, "코베아", 118, "1987-08-30", "여", "랜턴 밝기 조절이 손쉽고 오래가네요.", 5, 6),
                    Review(7, "듀랑고", 104, "1995-12-05", "남", "캠프장에서 눈에 띄는 스타일 좋아요.", 2, 7),
                    Review(8, "코베아", 119, "1982-04-17", "여", "코베아 제품은 항상 신뢰가 갑니다.", 3, 8),
                    Review(9, "듀랑고", 105, "2003-06-22", "남", "휴대성과 실용성을 겸비한 쿠킹기어에 만족.", 5, 9),
                    Review(10, "코베아", 120, "1991-09-11", "여", "캠핑 친구들 모두 코베아 제품을 추천해요.", 1, 10),
                    Review(11, "듀랑고", 106, "1986-11-19", "남", "고급스러운 디자인에 성능도 훌륭합니다.", 4, 11),
                    Review(12, "코베아", 121, "2001-02-14", "여", "가족 캠핑에 적합한 대형 텐트가 인상적이었어요.", 5, 12),
                    Review(13, "듀랑고", 107, "1993-07-28", "남", "가격 대비 성능이 우수해 추천드려요.", 2, 13),
                    Review (14, "코베아", 122, "1989-10-30", "여", "아이들과 사용하기 안전한 제품들이 많아요.", 3, 14),
                    Review(15, "듀랑고", 108, "2002-01-21", "남", "캠핑 필수품인 듀랑고 제품, 만족합니다.", 1, 15),
                    Review (16, "코베아", 123, "1994-07-10", "남", "코베아의 쿨러 백스는 여름 캠핑에 완벽해요.", 4, 16),
                    Review(17, "듀랑고", 109, "1988-09-15", "여", "듀랑고 캠핑 장비로 품격 있는 캠핑을 즐겼습니다.", 5, 17),
                    Review(18, "코베아", 124, "2000-12-02", "남", "코베아 랜턴은 밤새도록 우리를 밝혀주었어요.", 3, 18),
                    Review(19, "듀랑고", 110, "1996-04-25", "여", "듀랑고 캠핑 용품은 실용적이면서도 견고해요.", 1, 19),
                    Review(20, "코베아", 125, "1985-05-20", "남", "코베아 제품으로 캠핑의 품격을 높였어요.", 2, 20),
                    Review(21, "듀랑고", 111, "1997-08-31", "여", "비바람에도 견디는 듀랑고 텐트가 인상적입니다.", 4, 21),
                    Review(22, "코베아", 126, "1989-03-15", "남", "캠핑 초보자에게 코베아 제품이 딱 좋아요.", 5, 22),
                    Review(23, "듀랑고", 112, "1990-06-18", "여", "내구성 강한 듀랑고 제품으로 마음이 놓여요.", 3, 23),
                    Review(24, "코베아", 127, "1998-12-08", "남", "휴대가 편리한 코베아 제품을 추천합니다.", 1, 24),
                    Review(25, "듀랑고", 113, "1991-10-22", "여", "듀랑고 브랜드 제품은 언제나 만족스러워요.", 2, 25),
                    Review(26, "코베아", 128, "2004-07-26", "남", "코베아 캠핑용품으로 캠핑을 더 즐겁게!", 4, 26),
                    Review(27, "듀랑고", 114, "1993-11-12", "여", "아이들과의 캠핑에 듀랑고 제품이 안성맞춤입니다.", 3, 27),
                    Review(28, "코베아", 129, "1987-01-06", "남", "코베아 제품은 가성비가 좋아 추천해요.", 5, 28),
                    Review(29, "듀랑고", 115, "1995-09-29", "여", "듀랑고 제품은 가격이 좀 높지만 품질이 좋아요.", 2, 29),
                    Review(30, "코베아", 130, "1995-03-03", "남", "가족과의 캠핑을 위해 코베아 제품을 선택했습니다.", 1, 30),
                    Review(31, "듀랑고", 131, "1984-04-17", "여", "듀랑고 캠핑 용품이 캠핑을 한층 더 특별하게 만들어요.", 5, 31),
                    Review(32, "코베아", 132, "1999-05-21", "남", "코베아 제품이 캠핑을 더 편리하게 해줘요.", 3, 32),
                    Review(33, "듀랑고", 133, "2001-07-13", "여", "듀랑고 텐트는 설치가 간편해서 좋아요.", 1, 33),
                    Review(34, "코베아", 134, "1994-08-08", "남", "코베아의 다양한 제품 라인업이 마음에 듭니다.", 2, 34),
                    Review(35, "듀랑고", 135, "1986-02-19", "여", "듀랑고 텐트는 내구성이 뛰어나 장기간 사용 가능해요.", 4, 35),
                    Review(36, "코베아", 133, "1994-04-05", "남", "코베아 랜턴은 밤 캠핑의 분위기를 완성시켜줘요.",5, 36),
                    Review(37, "듀랑고", 119, "2003-11-30", "여", "디자인과 기능이 우수한 듀랑고의 제품군.", 4, 37),
                    Review(38, "코베아", 134, "1988-06-18", "남", "코베아 쿨러는 여름 캠핑의 필수 아이템이에요.", 4, 38),
                    Review(39, "듀랑고", 120, "1996-09-07", "여", "내구성과 실용성을 겸비한 듀랑고 캠핑 기어.", 3, 39),
                    Review(40, "코베아", 135, "2005-02-23", "남", "코베아 제품으로 가족 캠핑을 더욱 즐겁게 보냈습니다.", 4, 40)
            )

            for (review in reviewsToInsert) {
                Reviews.insert {
                    it[brandName] = review.brandName
                    it[productNumber] = review.productNumber
                    it[birthDate] = review.birthDate
                    it[gender] = review.gender
                    it[content] = review.content
                    it[scope] = review.scope
                    it[userId] = review.userId
                }
            }
        }
    }
}