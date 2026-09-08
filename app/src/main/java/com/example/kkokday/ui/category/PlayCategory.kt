package com.example.kkokday.ui.category

/**
 * "놀거리"(CT1) 세부 업종. 카카오 category_group_code=CT1만으로는 이 업종들을 구분할 수
 * 없어서, [classificationKeywords](CT1 카테고리 크롤 결과의 category_name 매칭용)와
 * [searchKeywords](키워드검색 보완용)를 함께 들고 있는다.
 *
 * 보드게임카페처럼 카카오가 CE7(카페)로 분류해놓는 업종도 있어서, CT1 카테고리 결과만
 * 믿지 않고 키워드 검색으로 직접 보완한다(FD6 "술집" 보완과 동일한 패턴).
 */
enum class PlayCategory(
    val label: String,
    val classificationKeywords: List<String>,
    val searchKeywords: List<String>,
) {
    MOVIE("영화관", listOf("영화관"), listOf("영화관")),
    PC_BANG("PC방", listOf("PC방", "피시방"), listOf("PC방")),
    KARAOKE("노래방", listOf("노래방"), listOf("노래방")),
    BILLIARDS("당구장", listOf("당구"), listOf("당구장")),
    BOWLING("볼링장", listOf("볼링"), listOf("볼링장")),
    BOARD_GAME_CAFE("보드게임카페", listOf("보드게임"), listOf("보드게임카페")),
    JJIMJILBANG("찜질방·사우나", listOf("찜질방", "사우나"), listOf("찜질방", "사우나")),
    ESCAPE_ROOM("방탈출카페", listOf("방탈출"), listOf("방탈출카페")),
    /**
     * 셀프사진관은 애초에 CT1(놀거리) 카테고리 크롤에 안 잡힌다 — 카카오가 "문화,예술 >
     * 사진 > 사진관,포토스튜디오"로 분류해서 [classificationKeywords]도 사실상 매칭되지
     * 않는다. 그래서 전량을 [searchKeywords] 키워드 검색으로 채우는데, 범용 단어("사진관")
     * 대신 프랜차이즈 브랜드명을 나열해 검색 정확도를 높인다. [CategoryPlacesViewModel]이
     * 이 브랜드 검색 결과와, 리스트에 없는 신생 브랜드를 잡기 위한 "네컷" 보조 검색 결과
     * 모두에 category_name의 "즉석사진" 태그(또는 상호명에 검색 브랜드명이 그대로 포함되는지)
     * 기준으로 한 번 더 검증해서, 여권사진/증명사진 찍는 일반 사진관이 섞이지 않게 한다
     * (실측 확인: 셀프사진관 프랜차이즈는 카카오가 "…> 즉석사진 > {브랜드명}"으로 태그하고,
     * 일반 사진관은 이 세그먼트 없이 "…> 사진관,포토스튜디오"에서 끊긴다).
     */
    SELF_PHOTO("셀프사진관", listOf("셀프사진관"), SELF_PHOTO_BRAND_KEYWORDS),
    ETC("기타", emptyList(), emptyList()),
}

/**
 * 셀프사진관 프랜차이즈 브랜드 화이트리스트(2026-09 기준, 나무위키 "즉석사진관" 문서와
 * 업계 기사 교차 확인). 새 브랜드가 계속 생기는 업종이라 주기적으로 갱신이 필요하다.
 */
val SELF_PHOTO_BRAND_KEYWORDS = listOf(
    "인생네컷", "포토이즘", "하루필름", "포토그레이", "셀픽스", "포토시그니처",
    "포토드링크", "모노맨션", "럭스포토부스", "원포토", "포토엘",
    "인싸포토", "포토에어", "포토매틱", "인생사진", "썸컷", "무드팔레트", "필름한잔",
)
