# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ---- Kakao SDK (v2-user) ----
# 공식 문서 권장 규칙: SDK 내부 응답 모델과, 그 모델을 리플렉션으로 (역)직렬화하는
# Gson 관련 타입을 R8이 제거/난독화하지 않도록 유지한다.
-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn org.jetbrains.annotations.**

# ---- kotlinx.serialization ----
# 공식 권장 규칙(https://github.com/Kotlin/kotlinx.serialization#android): 컴파일 타임에
# 생성되는 $serializer 및 Companion.serializer()를 R8이 제거하지 않도록 유지한다.
# 앱 내 @Serializable DTO(예: data.network.KakaoLocalApiService의 응답 DTO)가 대상이다.
-keepattributes InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.example.kkokday.**$$serializer { *; }
-keepclassmembers class com.example.kkokday.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.kkokday.** {
    kotlinx.serialization.KSerializer serializer(...);
}