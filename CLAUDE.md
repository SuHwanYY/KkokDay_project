# KkokDay 개발 가이드

## 기본 기술 스택 (기능 구현 시 항상 적용)

- **UI**: XML 레이아웃이 아닌 Jetpack Compose로 작성한다.
- **비동기 처리**: Coroutines/Flow를 사용한다 (RxJava, AsyncTask 등 레거시 방식 지양).
- **의존성 주입**: Hilt를 사용한다.
- **디자인 시스템**: Material3 (`androidx.compose.material3`)를 사용한다.

라이브러리나 아키텍처 패턴을 선택해야 하는 상황에서는, 오래되었거나 레거시로 취급되는 방식보다 현재 안드로이드 업계에서 널리 채택되는 최신 방식을 우선적으로 고려한다.
(예: LiveData보다 Flow/StateFlow, 수동 Dagger 설정보다 Hilt, View 기반 UI보다 Compose)

## 현재 상태 (참고용)

- Compose Navigation(`androidx.navigation:navigation-compose`)까지만 세팅되어 있고, Hilt/ViewModel/Repository 계층은 아직 도입되지 않았다.
- 실제 인증 로직(로그인/회원가입/카카오 로그인) 등 새 기능을 구현할 때 위 스택 기준으로 필요한 의존성을 새로 추가하면 된다.
