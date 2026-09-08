// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    // google-services.json이 app/ 폴더에 추가되면 app/build.gradle.kts에서 이 플러그인을 적용하세요.
    alias(libs.plugins.google.services) apply false
}