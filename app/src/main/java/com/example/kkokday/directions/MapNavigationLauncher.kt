package com.example.kkokday.directions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * [MapNavigationApp] 딥링크 실행 + 미설치 시 플레이스토어 폴백을 담당한다. 설치 여부를
 * 먼저 PackageManager로 확인해서, 미설치 앱은 딥링크 시도 없이 바로 스토어 상세 페이지로
 * 보낸다(목록에서 숨기지 않고 설치를 유도하는 쪽을 택함).
 */
object MapNavigationLauncher {

    fun launch(context: Context, app: MapNavigationApp, destination: NavigationDestination) {
        if (isInstalled(context, app.packageName)) {
            openDeepLink(context, app, destination)
        } else {
            openPlayStore(context, app.packageName)
        }
    }

    private fun isInstalled(context: Context, packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    private fun openDeepLink(context: Context, app: MapNavigationApp, destination: NavigationDestination) {
        val uri = app.buildDeepLinkUri(destination, context.packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage(app.packageName) }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // 설치 확인은 통과했지만 딥링크 처리 액티비티가 없는 예외적인 경우의 방어막.
            openPlayStore(context, app.packageName)
        }
    }

    private fun openPlayStore(context: Context, packageName: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")),
            )
        } catch (_: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
                ),
            )
        }
    }
}
