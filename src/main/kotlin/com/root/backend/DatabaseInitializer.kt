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
                    Review(1, "스노우피크", 101, "1990-01-01", "남", "스노우피크 텐트의 내구성이 매우 만족스럽습니다."),
                    Review(2, "코베아", 116, "1985-10-02", "여", "코베아 스토브 사용이 편리하고 안정적입니다."),
                    Review(3, "스노우피크", 102, "2000-10-27", "남", "캠핑용 의자는 경량이면서도 편안했어요."),
                    Review(4, "코베아", 117, "1992-05-16", "여", "그릴 성능이 좋고 청소가 간편합니다."),
                    Review(5, "스노우피크", 103, "1998-03-20", "남", "가격은 높지만 품질이 그만큼 우수합니다."),
                    Review(6, "코베아", 118, "1987-08-30", "여", "랜턴 밝기 조절이 손쉽고 오래가네요."),
                    Review(7, "스노우피크", 104, "1995-12-05", "남", "캠프장에서 눈에 띄는 스타일 좋아요."),
                    Review(8, "코베아", 119, "1982-04-17", "여", "코베아 제품은 항상 신뢰가 갑니다."),
                    Review(9, "스노우피크", 105, "2003-06-22", "남", "휴대성과 실용성을 겸비한 쿠킹기어에 만족."),
                    Review(10, "코베아", 120, "1991-09-11", "여", "캠핑 친구들 모두 코베아 제품을 추천해요."),
                    Review(11, "스노우피크", 106, "1986-11-19", "남", "고급스러운 디자인에 성능도 훌륭합니다."),
                    Review(12, "코베아", 121, "2001-02-14", "여", "가족 캠핑에 적합한 대형 텐트가 인상적이었어요."),
                    Review(13, "스노우피크", 107, "1993-07-28", "남", "가격 대비 성능이 우수해 추천드려요."),
                    Review(14, "코베아", 122, "1989-10-30", "여", "아이들과 사용하기 안전한 제품들이 많아요."),
                    Review(15, "스노우피크", 108, "2002-01-21", "남", "캠핑 필수품인 스노우피크 제품, 만족합니다."),
                    Review(16, "코베아", 123, "1994-07-10", "남", "코베아의 쿨러 백스는 여름 캠핑에 완벽해요."),
                    Review(17, "스노우피크", 109, "1988-09-15", "여", "스노우피크 캠핑 장비로 품격 있는 캠핑을 즐겼습니다."),
                    Review(18, "코베아", 124, "2004-05-20", "남", "캠핑 초보자에게도 쉽게 다가가는 제품들이 많네요."),
                    Review(19, "스노우피크", 110, "1983-12-11", "여", "디자인과 기능성 모두 만족스러운 브랜드입니다."),
                    Review(20, "코베아", 125, "1999-08-07", "남", "가성비 최고의 캠핑 기어를 찾는다면 코베아!"),
                    Review(21, "스노우피크", 111, "1996-04-22", "여", "튼튼하고 실용적인 스노우피크 제품에 반했어요."),
                    Review(22, "코베아", 126, "1984-11-05", "남", "야외 활동이 많은 저에게 코베아 제품은 필수품입니다."),
                    Review(23, "스노우피크", 112, "2005-03-19", "여", "다양한 캠핑 액세서리가 있어 선택의 폭이 넓네요."),
                    Review(24, "코베아", 127, "1990-06-14", "남", "휴대용 가스레인지가 정말 편리하고 안전해요."),
                    Review(25, "스노우피크", 113, "1987-01-29", "여", "자연 속에서 더욱 빛나는 스노우피크의 매력!"),
                    Review(26, "코베아", 128, "2002-09-17", "남", "접이식 캠핑 의자는 가볍고 튼튼해서 좋아요."),
                    Review(27, "스노우피크", 114, "1997-02-08", "여", "프리미엄 캠핑 장비의 대명사, 스노우피크!"),
                    Review(28, "코베아", 129, "1985-07-21", "남", "코베아 제품들은 항상 내구성이 뛰어난 것 같아요."),
                    Review(29, "스노우피크", 115, "2006-10-12", "여", "스타일과 기능을 겸비한 캠핑 기어가 많습니다."),
                    Review(30, "코베아", 130, "1995-03-03", "남", "가족과의 캠핑을 위해 코베아 제품을 선택했습니다."),
                    Review(31, "스노우피크", 116, "1989-08-27", "여", "스노우피크 제품으로 캠핑의 질을 높였어요."),
                    Review(32, "코베아", 131, "2004-12-18", "남", "견고하고 신뢰할 수 있는 코베아의 캠핑 장비."),
                    Review(33, "스노우피크", 117, "1986-05-09", "여", "포터블한 스노우피크 제품들이 캠핑을 더 즐겁게 해줘요."),
                    Review(34, "코베아", 132, "2001-01-26", "남", "코베아의 텐트는 설치가 간편하고 튼튼해요."),
                    Review(35, "스노우피크", 118, "1998-08-15", "여", "고품질 캠핑 장비를 찾는다면 스노우피크 추천!"),
                    Review(36, "코베아", 133, "1994-04-05", "남", "코베아 랜턴은 밤 캠핑의 분위기를 완성시켜줘요."),
                    Review(37, "스노우피크", 119, "2003-11-30", "여", "디자인과 기능이 우수한 스노우피크의 제품군."),
                    Review(38, "코베아", 134, "1988-06-18", "남", "코베아 쿨러는 여름 캠핑의 필수 아이템이에요."),
                    Review(39, "스노우피크", 120, "1996-09-07", "여", "내구성과 실용성을 겸비한 스노우피크 캠핑 기어."),
                    Review(40, "코베아", 135, "2005-02-23", "남", "코베아 제품으로 가족 캠핑을 더욱 즐겁게 보냈습니다.")
            )

            for (review in reviewsToInsert) {
                Reviews.insert {
                    it[brandName] = review.brandName
                    it[productNumber] = review.productNumber
                    it[birthDate] = review.birthDate
                    it[gender] = review.gender
                    it[content] = review.content
                }
            }
        }
    }
}