package com.example.kkokday.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 업로드 전 리사이즈 기본 기준 — 긴 변 최대 픽셀. */
const val DEFAULT_MAX_IMAGE_DIMENSION = 1920

/** JPEG 압축 기본 품질(0~100). 화질 저하는 최소화하면서 용량을 크게 줄이는 절충값. */
const val DEFAULT_JPEG_QUALITY = 80

/**
 * 갤러리에서 고른 이미지를 원본 그대로 올리면 업로드가 오래 걸려서, 올리기 전에 긴 변을
 * [maxDimension] 이내로 줄이고 JPEG [quality]로 재압축한다. EXIF 방향 정보를 읽어 회전/반전까지
 * 반영한 뒤 압축한다 — [Bitmap.compress]는 EXIF 메타데이터를 보존하지 않으므로 이 단계를
 * 빼먹으면 세로로 찍은 사진이 옆으로 눕는다.
 *
 * 리뷰 사진/프로필 사진 등 갤러리 업로드가 필요한 곳이면 어디서나 공통으로 쓴다.
 */
suspend fun compressImageForUpload(
    context: Context,
    uri: Uri,
    maxDimension: Int = DEFAULT_MAX_IMAGE_DIMENSION,
    quality: Int = DEFAULT_JPEG_QUALITY,
): ByteArray = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver

    val orientation = resolver.openInputStream(uri)?.use { input ->
        ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } ?: ExifInterface.ORIENTATION_NORMAL

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)

    val decoded = resolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
    } ?: error("이미지를 읽을 수 없습니다: $uri")

    val oriented = applyExifOrientation(decoded, orientation)
    val scaled = oriented.scaleDownToMaxDimension(maxDimension)

    ByteArrayOutputStream().use { output ->
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
        if (scaled !== oriented) oriented.recycle()
        if (oriented !== decoded) decoded.recycle()
        scaled.recycle()
        output.toByteArray()
    }
}

/**
 * 목표 크기 근처까지만 대략(2의 배수 단위로) 다운샘플링해서 메모리를 아낀다 — 정확한
 * 최종 크기는 이후 [scaleDownToMaxDimension]이 맞춘다.
 */
private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var sampleSize = 1
    val longerSide = maxOf(width, height)
    while (longerSide / (sampleSize * 2) >= maxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}

private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun Bitmap.scaleDownToMaxDimension(maxDimension: Int): Bitmap {
    val longerSide = maxOf(width, height)
    if (longerSide <= maxDimension) return this
    val scale = maxDimension.toFloat() / longerSide
    val targetWidth = (width * scale).toInt().coerceAtLeast(1)
    val targetHeight = (height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
}
