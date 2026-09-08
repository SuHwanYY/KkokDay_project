package com.example.kkokday.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.kkokday.data.auth.PasswordResetConfig
import com.example.kkokday.data.place.placeDocId
import com.example.kkokday.ui.auth.kakaosetup.KakaoNicknameSetupScreen
import com.example.kkokday.ui.category.CategoryPlacesScreen
import com.example.kkokday.ui.auth.login.LoginScreen
import com.example.kkokday.ui.course.CourseDetailRoute
import com.example.kkokday.ui.course.CourseListRoute
import com.example.kkokday.ui.favorite.FavoritesRoute
import com.example.kkokday.ui.auth.passwordreset.ForgotPasswordScreen
import com.example.kkokday.ui.auth.passwordreset.ResetPasswordScreen
import com.example.kkokday.ui.auth.signup.SignupScreen
import com.example.kkokday.ui.auth.verification.EmailVerificationScreen
import com.example.kkokday.ui.main.MainHomeRoute
import com.example.kkokday.ui.main.model.BottomNavDestination
import com.example.kkokday.ui.my.MyRoute
import com.example.kkokday.ui.place.PlaceSearchScreen
import com.example.kkokday.ui.review.PlaceReviewRoute
import com.example.kkokday.ui.splash.SplashScreen
import com.example.kkokday.ui.vote.VoteDetailRoute
import com.example.kkokday.ui.vote.VoteListRoute

object KkokDayRoute {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"

    private const val EMAIL_VERIFICATION_BASE = "email_verification"
    const val EMAIL_VERIFICATION_NOTICE_ARG = "notice"
    const val EMAIL_VERIFICATION =
        "$EMAIL_VERIFICATION_BASE?$EMAIL_VERIFICATION_NOTICE_ARG={$EMAIL_VERIFICATION_NOTICE_ARG}"

    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val PLACE_SEARCH = "place_search"
    const val FAVORITES = "favorites"
    const val COURSES = "courses"
    const val VOTE = "vote"
    const val MY = "my"

    private const val CATEGORY_PLACES_BASE = "category_places"
    const val CATEGORY_PLACES_CODE_ARG = "categoryGroupCode"
    const val CATEGORY_PLACES = "$CATEGORY_PLACES_BASE/{$CATEGORY_PLACES_CODE_ARG}"

    private const val COURSE_DETAIL_BASE = "course_detail"
    const val COURSE_DETAIL_ID_ARG = "courseId"
    const val COURSE_DETAIL = "$COURSE_DETAIL_BASE/{$COURSE_DETAIL_ID_ARG}"

    private const val VOTE_DETAIL_BASE = "vote_detail"
    const val VOTE_DETAIL_ID_ARG = "courseId"
    const val VOTE_DETAIL = "$VOTE_DETAIL_BASE/{$VOTE_DETAIL_ID_ARG}"

    private const val PLACE_REVIEW_BASE = "place_review"
    const val PLACE_REVIEW_DOC_ID_ARG = "placeDocId"
    const val PLACE_REVIEW_NAME_ARG = "placeName"
    const val PLACE_REVIEW_ADDRESS_ARG = "address"
    const val PLACE_REVIEW = "$PLACE_REVIEW_BASE/{$PLACE_REVIEW_DOC_ID_ARG}?" +
        "$PLACE_REVIEW_NAME_ARG={$PLACE_REVIEW_NAME_ARG}&" +
        "$PLACE_REVIEW_ADDRESS_ARG={$PLACE_REVIEW_ADDRESS_ARG}"

    private const val RESET_PASSWORD_BASE = "reset_password"
    const val RESET_PASSWORD_OOB_CODE_ARG = "oobCode"
    const val RESET_PASSWORD = "$RESET_PASSWORD_BASE?$RESET_PASSWORD_OOB_CODE_ARG={$RESET_PASSWORD_OOB_CODE_ARG}"

    private const val KAKAO_NICKNAME_SETUP_BASE = "kakao_nickname_setup"
    const val KAKAO_NICKNAME_SETUP_ID_ARG = "kakaoId"
    const val KAKAO_NICKNAME_SETUP_NICKNAME_ARG = "nickname"
    const val KAKAO_NICKNAME_SETUP_PROFILE_IMAGE_ARG = "profileImageUrl"
    const val KAKAO_NICKNAME_SETUP = "$KAKAO_NICKNAME_SETUP_BASE/{$KAKAO_NICKNAME_SETUP_ID_ARG}?" +
        "$KAKAO_NICKNAME_SETUP_NICKNAME_ARG={$KAKAO_NICKNAME_SETUP_NICKNAME_ARG}&" +
        "$KAKAO_NICKNAME_SETUP_PROFILE_IMAGE_ARG={$KAKAO_NICKNAME_SETUP_PROFILE_IMAGE_ARG}"

    /** notice가 없으면 EMAIL_VERIFICATION 패턴의 인자 없이 이동할 실제 라우트 문자열을 만든다. */
    fun emailVerificationRoute(notice: String? = null): String = if (notice != null) {
        "$EMAIL_VERIFICATION_BASE?$EMAIL_VERIFICATION_NOTICE_ARG=${Uri.encode(notice)}"
    } else {
        EMAIL_VERIFICATION_BASE
    }

    /** 카카오 최초 로그인 직후, 프로필 정보를 담아 닉네임 설정 화면으로 이동할 라우트를 만든다. */
    fun kakaoNicknameSetupRoute(kakaoId: Long, nicknameSuggestion: String?, profileImageUrl: String?): String {
        return "$KAKAO_NICKNAME_SETUP_BASE/$kakaoId" +
            "?$KAKAO_NICKNAME_SETUP_NICKNAME_ARG=${Uri.encode(nicknameSuggestion.orEmpty())}" +
            "&$KAKAO_NICKNAME_SETUP_PROFILE_IMAGE_ARG=${Uri.encode(profileImageUrl.orEmpty())}"
    }

    /** 카테고리 코드는 항상 영문 알파벳+숫자(FD6 등)라 별도 인코딩 없이 경로에 그대로 태운다. */
    fun categoryPlacesRoute(categoryGroupCode: String): String = "$CATEGORY_PLACES_BASE/$categoryGroupCode"

    /** courseId는 Firestore 자동 생성 문서 ID(영문/숫자)라 별도 인코딩 없이 경로에 그대로 태운다. */
    fun courseDetailRoute(courseId: String): String = "$COURSE_DETAIL_BASE/$courseId"

    fun voteDetailRoute(courseId: String): String = "$VOTE_DETAIL_BASE/$courseId"

    /**
     * placeDocId는 좌표 폴백 시 "위도_경도" 형태(점 포함)일 수 있어 안전하게 인코딩한다.
     * placeName/address는 헤더 표시용으로만 쓰고, 화면 진입 후 실제 평점·리뷰는 Firestore에서
     * 새로 조회한다.
     */
    fun placeReviewRoute(placeDocId: String, placeName: String, address: String): String =
        "$PLACE_REVIEW_BASE/${Uri.encode(placeDocId)}" +
            "?$PLACE_REVIEW_NAME_ARG=${Uri.encode(placeName)}" +
            "&$PLACE_REVIEW_ADDRESS_ARG=${Uri.encode(address)}"

    /**
     * 바텀 네비게이션 탭 → 실제 라우트. 화면이 아직 없는 탭이 생기면 여기만 null로 두면
     * [navigateToBottomTab]은 손댈 필요 없이 새 탭이 바로 라우팅에 붙는다.
     */
    fun bottomTabRoute(destination: BottomNavDestination): String? = when (destination) {
        BottomNavDestination.HOME -> HOME
        BottomNavDestination.FAVORITE -> FAVORITES
        BottomNavDestination.COURSE -> COURSES
        BottomNavDestination.VOTE -> VOTE
        BottomNavDestination.MY -> MY
    }
}

/**
 * 상태바/제스처 바와 콘텐츠가 겹치는 문제를 화면마다 각자 처리하지 않도록, 목적지 하나당
 * 한 번 [WindowInsets.systemBars] 만큼 패딩을 준 뒤 실제 화면을 그린다.
 *
 * 새 화면을 추가할 땐 [androidx.navigation.compose.composable] 대신 이 함수를 쓰면 인셋
 * 처리가 자동으로 따라온다. 스플래시처럼 시스템 바 뒤까지 이미지를 꽉 채우고 싶은
 * 화면만 예외적으로 [androidx.navigation.compose.composable]을 직접 쓴다.
 */
private fun NavGraphBuilder.insetSafeComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
    ) { backStackEntry ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            content(backStackEntry)
        }
    }
}

private const val SPLASH_TRANSITION_DURATION_MILLIS = 350

/**
 * 바텀 네비게이션이 있는 화면(즐겨찾기/코스 등)끼리 서로 전환할 때 쓴다. 특정 탭만 아는
 * 특별 분기 없이 [KkokDayRoute.bottomTabRoute] 하나로 모든 [BottomNavDestination]을
 * 동일하게 처리한다 — 나중에 투표·마이 화면이 생기면 그 매핑만 채우면 되고 여긴 안 건드려도 된다.
 *
 * 아직 화면이 없는 탭(매핑이 null)이면 아무것도 안 하고 false를 반환한다 — 호출부(화면)가
 * 이 값을 보고 "아직 준비 중인 기능이에요" 같은 안내를 보여줄지 판단한다.
 *
 * HOME은 항상 백스택 맨 아래에 있는 인스턴스를 그대로 재사용하도록 popBackStack()을
 * 쓰고(화면을 새로 만들지 않아 더 가볍다), 다른 탭으로 갈 땐 지금 화면을 그 탭으로
 * 교체한다(popUpTo(currentRoute, inclusive=true) — Home 위에 탭 화면이 계속 쌓이는 대신
 * 항상 하나만 남는다).
 */
private fun NavHostController.navigateToBottomTab(currentRoute: String, destination: BottomNavDestination): Boolean {
    val targetRoute = KkokDayRoute.bottomTabRoute(destination) ?: return false

    // 바텀 네비게이션을 연타하면, 화면 전환 애니메이션이 끝나기 전(=아직 currentBackStackEntry가
    // RESUMED가 아닌 상태)에 이전 화면(예: 즐겨찾기)의 클릭 콜백이 한 번 더 실행될 수 있다.
    // 이때 이 콜백에 캡처된 currentRoute는 이미 stale해도(백스택이 바뀐 뒤라도) 그대로
    // 넘어오므로, 안드로이드 공식 내비게이션 샘플에서 쓰는 패턴대로 최상단 엔트리가
    // RESUMED일 때만 실행되게 막는다.
    if (currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return true

    if (targetRoute == currentRoute) return true
    if (destination == BottomNavDestination.HOME) {
        // 로그인 이후 백스택은 [HOME, 현재 탭] 두 개만 유지되므로, 이미 HOME만 남아있을 때
        // (팝할 대상이 없을 때) popBackStack()을 또 호출하면 마지막 남은 엔트리까지 지워져
        // 화면이 통째로 빈 채로 남는다 — previousBackStackEntry가 있을 때만 팝한다.
        if (previousBackStackEntry != null) {
            popBackStack()
        }
    } else {
        navigate(targetRoute) {
            popUpTo(currentRoute) { inclusive = true }
        }
    }
    return true
}

/**
 * 스플래시 화면(splash.png)에서 로그인/홈으로 넘어갈 때만 은은한 페이드+살짝 위로
 * 올라오는 슬라이드를 적용한다. 다른 경로(로그아웃→로그인, 가입완료→홈 등)로
 * 들어올 때는 건드리지 않도록 initialState가 SPLASH일 때만 적용한다.
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.splashEnterTransition(): EnterTransition {
    if (initialState.destination.route != KkokDayRoute.SPLASH) return EnterTransition.None
    return fadeIn(animationSpec = tween(SPLASH_TRANSITION_DURATION_MILLIS)) +
        slideInVertically(animationSpec = tween(SPLASH_TRANSITION_DURATION_MILLIS)) { fullHeight -> fullHeight / 12 }
}

@Composable
fun KkokDayNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = KkokDayRoute.SPLASH,
    ) {
        composable(
            route = KkokDayRoute.SPLASH,
            exitTransition = { fadeOut(animationSpec = tween(SPLASH_TRANSITION_DURATION_MILLIS)) },
        ) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(KkokDayRoute.LOGIN) {
                        popUpTo(KkokDayRoute.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(KkokDayRoute.HOME) {
                        popUpTo(KkokDayRoute.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        insetSafeComposable(
            route = KkokDayRoute.LOGIN,
            enterTransition = { splashEnterTransition() },
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(KkokDayRoute.HOME) {
                        popUpTo(KkokDayRoute.LOGIN) { inclusive = true }
                    }
                },
                onNeedsEmailVerification = {
                    navController.navigate(KkokDayRoute.emailVerificationRoute()) {
                        popUpTo(KkokDayRoute.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(KkokDayRoute.SIGNUP)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(KkokDayRoute.FORGOT_PASSWORD)
                },
                onNeedsKakaoNicknameSetup = { kakaoId, nicknameSuggestion, profileImageUrl ->
                    // LOGIN을 백스택에서 지우지 않는다 — 지우면(popUpTo inclusive) 이 화면이
                    // 백스택의 유일한 항목이 되어, 시스템 뒤로가기를 누를 때 로그인 화면으로
                    // 돌아가는 대신 앱이 그대로 종료돼버린다. Firebase 로그인은 됐지만 닉네임을
                    // 아직 확정하지 않은 상태로 남는데, 재로그인 시 findExistingNickname이 다시
                    // null을 반환해 이 화면으로 정상적으로 돌아오므로 안전하다.
                    navController.navigate(
                        KkokDayRoute.kakaoNicknameSetupRoute(kakaoId, nicknameSuggestion, profileImageUrl),
                    )
                },
            )
        }
        insetSafeComposable(
            route = KkokDayRoute.KAKAO_NICKNAME_SETUP,
            arguments = listOf(
                navArgument(KkokDayRoute.KAKAO_NICKNAME_SETUP_ID_ARG) { type = NavType.LongType },
                navArgument(KkokDayRoute.KAKAO_NICKNAME_SETUP_NICKNAME_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(KkokDayRoute.KAKAO_NICKNAME_SETUP_PROFILE_IMAGE_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            KakaoNicknameSetupScreen(
                onSetupComplete = {
                    navController.navigate(KkokDayRoute.HOME) {
                        popUpTo(KkokDayRoute.KAKAO_NICKNAME_SETUP) { inclusive = true }
                    }
                },
            )
        }
        insetSafeComposable(KkokDayRoute.SIGNUP) {
            SignupScreen(
                onSignUpSuccess = {
                    navController.navigate(KkokDayRoute.emailVerificationRoute()) {
                        popUpTo(KkokDayRoute.LOGIN) { inclusive = true }
                    }
                },
                onExistingAccountLoginSuccess = {
                    // 이미 인증까지 끝난 본인 계정으로 로그인된 경우 — 일반 로그인 성공과 동일하게 홈으로.
                    navController.navigate(KkokDayRoute.HOME) {
                        popUpTo(KkokDayRoute.LOGIN) { inclusive = true }
                    }
                },
                onExistingUnverifiedAccount = { notice ->
                    // 예전에 가입만 시작한 본인 계정 — 인증 메일을 재발송했다는 안내와 함께 인증 대기 화면으로.
                    navController.navigate(KkokDayRoute.emailVerificationRoute(notice)) {
                        popUpTo(KkokDayRoute.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(KkokDayRoute.FORGOT_PASSWORD)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
        insetSafeComposable(
            route = KkokDayRoute.EMAIL_VERIFICATION,
            arguments = listOf(
                navArgument(KkokDayRoute.EMAIL_VERIFICATION_NOTICE_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            EmailVerificationScreen(
                onVerified = {
                    navController.navigate(KkokDayRoute.HOME) {
                        popUpTo(KkokDayRoute.EMAIL_VERIFICATION) { inclusive = true }
                    }
                },
                onSignedOut = {
                    navController.navigate(KkokDayRoute.LOGIN) {
                        popUpTo(KkokDayRoute.EMAIL_VERIFICATION) { inclusive = true }
                    }
                },
            )
        }
        insetSafeComposable(KkokDayRoute.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
        insetSafeComposable(
            route = KkokDayRoute.RESET_PASSWORD,
            arguments = listOf(
                navArgument(KkokDayRoute.RESET_PASSWORD_OOB_CODE_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${PasswordResetConfig.RESET_PASSWORD_DEEP_LINK}?" +
                        "${KkokDayRoute.RESET_PASSWORD_OOB_CODE_ARG}={${KkokDayRoute.RESET_PASSWORD_OOB_CODE_ARG}}"
                },
            ),
        ) {
            ResetPasswordScreen(
                onResetSuccess = {
                    navController.navigate(KkokDayRoute.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onInvalidLink = {
                    navController.navigate(KkokDayRoute.FORGOT_PASSWORD) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
            )
        }
        insetSafeComposable(
            route = KkokDayRoute.HOME,
            enterTransition = { splashEnterTransition() },
        ) {
            MainHomeRoute(
                onSearchPlaceClick = {
                    navController.navigate(KkokDayRoute.PLACE_SEARCH)
                },
                onCategoryClick = { category ->
                    navController.navigate(KkokDayRoute.categoryPlacesRoute(category.categoryGroupCode))
                },
                onFavoriteTabClick = {
                    navController.navigate(KkokDayRoute.FAVORITES)
                },
                onCourseTabClick = {
                    navController.navigate(KkokDayRoute.COURSES)
                },
                onVoteTabClick = {
                    navController.navigate(KkokDayRoute.VOTE)
                },
                onMyTabClick = {
                    navController.navigate(KkokDayRoute.MY)
                },
                onNavigateToCourseDetail = { courseId ->
                    navController.navigate(KkokDayRoute.courseDetailRoute(courseId))
                },
                onNavigateToVoteDetail = { courseId ->
                    navController.navigate(KkokDayRoute.voteDetailRoute(courseId))
                },
            )
        }
        insetSafeComposable(KkokDayRoute.PLACE_SEARCH) {
            PlaceSearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
        insetSafeComposable(KkokDayRoute.FAVORITES) {
            FavoritesRoute(
                onNavigateTab = { destination ->
                    navController.navigateToBottomTab(KkokDayRoute.FAVORITES, destination)
                },
                onNavigateToPlaceReview = { place ->
                    navController.navigate(
                        KkokDayRoute.placeReviewRoute(place.placeDocId(), place.placeName, place.address),
                    )
                },
            )
        }
        insetSafeComposable(
            route = KkokDayRoute.CATEGORY_PLACES,
            arguments = listOf(
                navArgument(KkokDayRoute.CATEGORY_PLACES_CODE_ARG) { type = NavType.StringType },
            ),
        ) {
            CategoryPlacesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCourses = {
                    navController.navigate(KkokDayRoute.COURSES) {
                        popUpTo(KkokDayRoute.HOME)
                    }
                },
                onNavigateToPlaceReview = { place ->
                    navController.navigate(
                        KkokDayRoute.placeReviewRoute(place.placeDocId(), place.placeName, place.address),
                    )
                },
            )
        }
        insetSafeComposable(KkokDayRoute.COURSES) {
            CourseListRoute(
                onNavigateTab = { destination ->
                    navController.navigateToBottomTab(KkokDayRoute.COURSES, destination)
                },
                onCourseClick = { courseId ->
                    navController.navigate(KkokDayRoute.courseDetailRoute(courseId))
                },
            )
        }
        insetSafeComposable(
            route = KkokDayRoute.COURSE_DETAIL,
            arguments = listOf(
                navArgument(KkokDayRoute.COURSE_DETAIL_ID_ARG) { type = NavType.StringType },
            ),
        ) {
            CourseDetailRoute(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
        insetSafeComposable(
            route = KkokDayRoute.PLACE_REVIEW,
            arguments = listOf(
                navArgument(KkokDayRoute.PLACE_REVIEW_DOC_ID_ARG) { type = NavType.StringType },
                navArgument(KkokDayRoute.PLACE_REVIEW_NAME_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(KkokDayRoute.PLACE_REVIEW_ADDRESS_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            PlaceReviewRoute(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
        insetSafeComposable(KkokDayRoute.VOTE) {
            VoteListRoute(
                onNavigateTab = { destination ->
                    navController.navigateToBottomTab(KkokDayRoute.VOTE, destination)
                },
                onCourseClick = { courseId ->
                    navController.navigate(KkokDayRoute.voteDetailRoute(courseId))
                },
            )
        }
        insetSafeComposable(
            route = KkokDayRoute.VOTE_DETAIL,
            arguments = listOf(
                navArgument(KkokDayRoute.VOTE_DETAIL_ID_ARG) { type = NavType.StringType },
            ),
        ) {
            VoteDetailRoute(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
        insetSafeComposable(KkokDayRoute.MY) {
            MyRoute(
                onNavigateTab = { destination ->
                    navController.navigateToBottomTab(KkokDayRoute.MY, destination)
                },
                onSignedOut = {
                    navController.navigate(KkokDayRoute.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateToPlaceReview = { review ->
                    navController.navigate(
                        KkokDayRoute.placeReviewRoute(review.placeDocId, review.placeName, review.address),
                    )
                },
            )
        }
    }
}
