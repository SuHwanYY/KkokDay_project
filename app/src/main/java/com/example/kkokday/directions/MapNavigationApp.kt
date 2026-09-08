package com.example.kkokday.directions

import android.net.Uri

/**
 * 길찾기 바텀시트에서 고를 수 있는 지도 앱 3종. [buildDeepLinkUri]가 각 앱의 길찾기 URL
 * Scheme을 만든다 — 출발지는 넘기지 않아 각 지도 앱이 현재 위치를 출발지로 자동 사용한다.
 *
 * 파라미터 출처(2026-09 기준 확인):
 * - 카카오맵: kakaomap://route?ep={lat},{lng}&by=CAR (devtalk.kakao.com 공식 답변, 패키지
 *   net.daum.android.map — 스토어 상품명 "KakaoMap"으로 동일 앱 확인됨)
 * - 네이버지도: nmap://route/car?dlat=&dlng=&dname=&appname= (NAVER Cloud Platform
 *   공식 가이드, appname 필수, 패키지 com.nhn.android.nmap)
 * - 티맵: tmap://route?goalx=&goaly=&goalname=&referrer= (SK Open API 커뮤니티 공식 답변,
 *   패키지 com.skt.tmap.ku)
 */
enum class MapNavigationApp(val label: String, val packageName: String) {
    KAKAO_MAP(label = "카카오맵", packageName = "net.daum.android.map") {
        override fun buildDeepLinkUri(destination: NavigationDestination, callerPackageName: String): Uri =
            Uri.Builder()
                .scheme("kakaomap")
                .authority("route")
                .appendQueryParameter("ep", "${destination.latitude},${destination.longitude}")
                .appendQueryParameter("by", "CAR")
                .build()
    },
    NAVER_MAP(label = "네이버지도", packageName = "com.nhn.android.nmap") {
        override fun buildDeepLinkUri(destination: NavigationDestination, callerPackageName: String): Uri =
            Uri.Builder()
                .scheme("nmap")
                .authority("route")
                .appendPath("car")
                .appendQueryParameter("dlat", destination.latitude.toString())
                .appendQueryParameter("dlng", destination.longitude.toString())
                .appendQueryParameter("dname", destination.placeName)
                .appendQueryParameter("appname", callerPackageName)
                .build()
    },
    TMAP(label = "티맵", packageName = "com.skt.tmap.ku") {
        override fun buildDeepLinkUri(destination: NavigationDestination, callerPackageName: String): Uri =
            Uri.Builder()
                .scheme("tmap")
                .authority("route")
                .appendQueryParameter("goalx", destination.longitude.toString())
                .appendQueryParameter("goaly", destination.latitude.toString())
                .appendQueryParameter("goalname", destination.placeName)
                .appendQueryParameter("referrer", callerPackageName)
                .build()
    },
    ;

    /** [callerPackageName]은 네이버지도(appname)/티맵(referrer)이 호출 앱을 식별하는 데 쓴다. */
    abstract fun buildDeepLinkUri(destination: NavigationDestination, callerPackageName: String): Uri
}
