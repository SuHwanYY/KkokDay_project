package com.example.kkokday.ui.main.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.kkokday.R
import com.example.kkokday.ui.theme.KkokDayCategoryBgCafe
import com.example.kkokday.ui.theme.KkokDayCategoryBgConvenienceStore
import com.example.kkokday.ui.theme.KkokDayCategoryBgEntertainment
import com.example.kkokday.ui.theme.KkokDayCategoryBgRestaurant
import com.example.kkokday.ui.theme.KkokDayCategoryBgWalkSpot

/**
 * 홈 화면 카테고리 아이콘 한 개. [backgroundColor]는 카테고리 박스의 파스텔톤 배경색.
 * [categoryGroupCode]는 카카오 로컬 카테고리 검색 API의 category_group_code — 클릭 시
 * 이 코드로 주변 검색 화면(CategoryPlacesScreen)에 진입한다.
 *
 * 아이콘은 둘 중 하나로 표현한다 — 대부분은 drawable [iconRes](원형 배지가 이미
 * 그려진 PNG), 전용 아이콘 이미지가 없는 카테고리는 [iconVector] + [iconAccentColor]로
 * 만든 색상 원 위에 흰색 벡터 아이콘을 그린다.
 */
data class HomeCategory(
    val label: String,
    val categoryGroupCode: String,
    val backgroundColor: Color,
    @DrawableRes val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val iconAccentColor: Color = Color.Unspecified,
)

/** 홈 화면 카테고리 5종 (음식점/카페/편의점/놀거리/산책·명소). 영화관은 "놀거리"(CT1)
 * 검색 결과에 자연히 포함되는 업종이라 별도 버튼을 두지 않는다. */
val dummyHomeCategories = listOf(
    HomeCategory(
        label = "음식점",
        categoryGroupCode = "FD6",
        iconRes = R.drawable.ic_restaurant,
        backgroundColor = KkokDayCategoryBgRestaurant,
    ),
    HomeCategory(
        label = "카페",
        categoryGroupCode = "CE7",
        iconRes = R.drawable.ic_cafe,
        backgroundColor = KkokDayCategoryBgCafe,
    ),
    HomeCategory(
        label = "편의점",
        categoryGroupCode = "CS2",
        iconRes = R.drawable.ic_convenience_store,
        backgroundColor = KkokDayCategoryBgConvenienceStore,
    ),
    HomeCategory(
        label = "놀거리",
        categoryGroupCode = "CT1",
        iconRes = R.drawable.ic_entertainment,
        backgroundColor = KkokDayCategoryBgEntertainment,
    ),
    HomeCategory(
        label = "산책·명소",
        categoryGroupCode = "AT4",
        iconRes = R.drawable.ic_walk_spot,
        backgroundColor = KkokDayCategoryBgWalkSpot,
    ),
)

/** 하단 네비게이션 탭. */
enum class BottomNavDestination(val label: String) {
    HOME("홈"),
    FAVORITE("즐겨찾기"),
    COURSE("코스"),
    VOTE("투표"),
    MY("마이"),
}
