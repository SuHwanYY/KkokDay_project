package com.example.kkokday.ui.course

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kkokday.R
import com.example.kkokday.data.course.Course
import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.util.WalkingDistance
import com.example.kkokday.util.formatDistance
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 코스 상세 화면의 "이미지로 공유" 버튼이 띄우는 전체화면 다이얼로그.
 *
 * 배경 일러스트 3종([SHARE_BACKGROUNDS])은 각각 "가장자리는 흐리고 가운데는 또렷한 카드
 * 구도"가 이미 그림 안에 그려진 완성된 화면 디자인이다(941×1672 원본에서 또렷한 영역이
 * 대략 가로 8.8~91%, 세로 7~91% 지점 — [SHARP_LEFT]/[SHARP_RIGHT]/[SHARP_TOP]/[SHARP_BOTTOM],
 * 3장을 직접 열어 픽셀 단위로 확인한 값). 그래서 앱이 또 한 번 작은 카드로 감싸지 않고
 * 상태바 바로 아래부터 화면 끝까지 이미지를 꽉 채운다([ContentScale.Crop] + 중앙 정렬).
 * 화면 비율이 이미지와 다르면 Crop이 위아래 또는 좌우를 살짝 잘라내는데, [computeSharpRegion]이
 * 그 크롭을 감안해서 "또렷한 영역"이 실제 화면 좌표로 어디에 해당하는지 매 기기마다 다시
 * 계산하고, 제목·핀 보드를 전부 그 영역 안쪽에만 배치한다.
 *
 * 핀은 더 이상 코드로 그리지 않고, res/drawable의 물방울(지도 마커) 이미지 5종
 * (ic_marker_yellow/red/green/blue/pink)을 그대로 쓴다 — [PinMarker] 참고.
 *
 * 캡처는 [android.view.View.draw]로 뜬다 — 이 방식에 이르기까지 두 번 헛다리를 짚었다.
 * `GraphicsLayer.toImageBitmap()`은 미리보기는 멀쩡한데 실제 저장 비트맵이 완전히
 * 투명했고, [PixelCopy]는 (겉보기엔 성공해서 유효한 비트맵을 주는데도) 다이얼로그가 아니라
 * 그 뒤에 있던 화면을 읽어왔다 — 둘 다 픽셀을 직접 찍어봐야만 드러나는 문제라 미리보기만
 * 봐서는 못 잡는다. PixelCopy 건의 진짜 원인은 `LocalView.current`를 이 함수 맨 위,
 * 즉 [Dialog]의 콘텐츠 람다 **바깥**에서 읽고 있었던 것 — 그러면 다이얼로그 자신의 뷰가
 * 아니라 이 함수를 호출한 바깥쪽(코스 상세 화면)의 뷰를 들고 있게 된다. `LocalView.current`를
 * `Dialog { }` 블록 **안**으로 옮기자마자 바로 고쳐졌다.
 *
 * 지금 방식은 X/배경전환/저장 버튼을 캡처 순간에만 [isCapturingChrome]로 잠깐
 * 컴포지션에서 빼고, 화면 전체를 비트맵으로 직접 그린 뒤([captureViewToBitmap]) 카드
 * 영역만 잘라내고 버튼을 즉시 복원한다 — 비동기 콜백이나 별도 창 판독이 전혀 없다.
 * 완성된 비트맵은 [saveBitmapToGallery]로 기기 갤러리에 바로 저장한다.
 */
@Composable
fun CourseShareImageDialog(
    course: Course,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var captureBoundsInWindow by remember { mutableStateOf<Rect?>(null) }
    var backgroundIndex by remember { mutableStateOf(Random.nextInt(SHARE_BACKGROUNDS.size)) }
    var isSaving by remember { mutableStateOf(false) }
    // 캡처하는 그 순간에만 true — X/배경전환/저장 버튼(과 에러 문구)을 잠깐 컴포지션에서
    // 빼서, 화면 전체를 그대로 비트맵으로 떠도 이 UI 크롬이 결과물에 안 찍히게 한다.
    var isCapturingChrome by remember { mutableStateOf(false) }
    var statusError by remember { mutableStateOf<String?>(null) }

    // 장소가 12곳을 넘는 코스는 공유 이미지가 생성되는(=이 다이얼로그가 뜨는) 시점에 바로
    // 안내한다 — 저장 버튼을 누르기 전에도 보드가 이미 잘려 있으니 그때 알려줘야 한다.
    LaunchedEffect(course.id) {
        if (course.places.size > MAX_SHARE_PLACES) {
            Toast.makeText(context, "이미지에는 ${MAX_SHARE_PLACES}곳까지만 표시됩니다", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
        ),
    ) {
        val view = LocalView.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            ShareCardContent(
                course = course,
                background = SHARE_BACKGROUNDS[backgroundIndex],
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates -> captureBoundsInWindow = coordinates.boundsInWindow() },
            )

            if (!isCapturingChrome) {
                // X(닫기)와 저장 버튼을 한 자리에 묶어 배경 그림의 흐린 가장자리(위쪽 여백) 안에만
                // 놓는다 — 그림의 또렷한 중앙 영역(제목·핀 보드)을 절대 가리지 않는다.
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.32f)),
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "닫기", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            val bounds = captureBoundsInWindow
                            if (bounds == null) {
                                statusError = "이미지를 만들지 못했어요. 다시 시도해 주세요"
                                return@IconButton
                            }
                            statusError = null
                            scope.launch {
                                try {
                                    val bitmap = captureViewToBitmap(view, bounds) {
                                        isCapturingChrome = it
                                    }
                                    isSaving = true
                                    val saved = withContext(Dispatchers.IO) {
                                        saveBitmapToGallery(context, bitmap, course.id)
                                    }
                                    if (saved != null) {
                                        Toast.makeText(context, "갤러리에 저장했어요", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } else {
                                        statusError = "이미지를 저장하지 못했어요. 다시 시도해 주세요"
                                    }
                                } catch (t: Throwable) {
                                    isCapturingChrome = false
                                    statusError = "이미지를 만들지 못했어요. 다시 시도해 주세요"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving && course.places.isNotEmpty(),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.32f)),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Filled.Download, contentDescription = "이미지 저장", tint = Color.White)
                        }
                    }
                }

                IconButton(
                    onClick = { backgroundIndex = (backgroundIndex + 1) % SHARE_BACKGROUNDS.size },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.32f)),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "배경 바꾸기", tint = Color.White)
                }

                statusError?.let { message ->
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 20.dp)
                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

/** 배경 원본(941×1672) 가로:세로 비율. */
private const val IMAGE_ASPECT = 941f / 1672f

/**
 * 배경 원본 이미지 안에서 "또렷한 카드" 영역의 경계(원본 기준 비율). 3장(sunset/seaside/
 * starrynight)을 직접 열어 픽셀 스캔으로 흰 테두리 위치를 확인한 값 — 세 장 모두 거의 동일한
 * 틀로 제작돼 있었다(좌 8.7~8.9%, 우 91%, 상 6.5~8%, 하 90~92.5%).
 */
private const val SHARP_LEFT = 0.088f
private const val SHARP_RIGHT = 0.911f
private const val SHARP_TOP = 0.070f
private const val SHARP_BOTTOM = 0.91f

// 코스 보드가 화면 가운데에 너무 작게 몰려 보인다는 피드백을 반영해 위/아래 여유를 계속
// 더 열었다(위 0.24 → 0.20 → 0.14 → 0.10, 아래 0.90 → 0.96 → 0.97) — 장소 12개까지도
// 축소 없이 아래쪽까지 최대한 쓰도록 세로 예산을 크게 늘렸다.
private const val SKY_TOP_FRACTION = 0.10f
private const val SKY_BOTTOM_FRACTION = 0.97f

/** 보드 자연 높이가 세로 예산([SKY_TOP_FRACTION]~[SKY_BOTTOM_FRACTION])보다 작게 나오는
 * 경우(장소가 적거나 화면이 클 때), 남는 여유 공간을 위/아래로 얼마나 나눠 줄지의 비율.
 * 기존엔 정확히 반반(0.5)으로 나눠 보드를 세로 중앙에 놓았는데, 그러다 보니 핀 1행이 화면
 * 중간께에서 시작하는 느낌이 든다는 피드백이 있었다 — 여유의 대부분을 아래로 보내고 위로는
 * 조금만 나눠서(0.2) 보드 자체가 위쪽으로 붙어 보이게 한다. */
private const val BOARD_TOP_LEFTOVER_BIAS = 0.2f

/** 1행 핀 위로 라벨(장소명)과 핀 머리가 들어갈 자리를 행 높이의 몇 배로 미리 비워둘지.
 * grid 위치 자체가 이미 rowPx/2만큼 아래에서 시작하기 때문에(행 높이 절반이 기본 여유로
 * 깔려 있음), 이 비율은 그 위에 "추가로" 얹는 몫이다 — 0.40도 여전히 과했다는 피드백을
 * 반영해 0.15까지 과감하게 줄였다. 더 줄이면 라벨이 제목 영역과 겹칠 수 있다. */
private const val FIRST_ROW_TOP_BUFFER_RATIO = 0.15f

/** 제목 아래로 핀 1행이 시작되기 전 최소로 띄워야 하는 간격. 실제 장소명(2줄로 줄바꿈되는
 * 긴 이름 포함)으로 실기기에서 확인해보니 28dp까지 줄였을 땐 제목 부제와 1행 라벨이 겹쳤다 —
 * 36dp로 늘려 안전 여백을 확보했었는데, 핀 보드 시작 위치가 너무 아래라는 피드백이 반복돼서
 * 18dp까지 과감하게 줄였다(이전에 겹침이 확인된 28dp보다도 낮은 값이라 실기기에서 제목
 * 부제와 1행 라벨이 겹치는지 꼭 확인이 필요하다 — 겹치면 이 값을 다시 올려야 한다). */
private val MIN_TITLE_TO_BOARD_GAP = 18.dp
private val TITLE_TOP_PADDING = 16.dp

/** 제목 아래 핀 1행이 시작되기 전, 핀 위 장소명 라벨 한 칸 몫으로 미리 예약해두는 높이.
 * 실제 장소명은 2줄로 줄바꿈되는 경우가 흔해(예: "세븐일레븐 마포홍대점") 40dp로는 부족해
 * 겹치는 문제가 있었다 — 56dp로 늘렸었는데, 보드 시작 위치가 너무 아래라는 피드백에 48dp로
 * 살짝 줄였다(40dp의 겹침 지점보다는 여전히 위 여유를 둔 값). */
private val LABEL_ALLOWANCE = 48.dp

/**
 * 배경 이미지 3종과, 배경마다 이미 그려진 손글씨 캡션 위치를 피해 제목을 어느 쪽에/얼마나
 * 좁게 배치할지(또렷한 영역 폭 대비 비율). sunset은 캡션이 왼쪽 위라 제목을 오른쪽으로,
 * 나머지 둘은 캡션이 오른쪽이라 제목을 왼쪽(기본)에 둔다.
 */
private data class ShareBackground(
    val drawableRes: Int,
    val titleOnRight: Boolean,
    val titleWidthFraction: Float,
)

private val SHARE_BACKGROUNDS = listOf(
    ShareBackground(R.drawable.course_share_bg_sunset, titleOnRight = true, titleWidthFraction = 0.5f),
    ShareBackground(R.drawable.course_share_bg_seaside, titleOnRight = false, titleWidthFraction = 0.62f),
    ShareBackground(R.drawable.course_share_bg_starrynight, titleOnRight = false, titleWidthFraction = 0.62f),
)

/** 제목·부제·라벨·핀 번호까지 화면 전체에 쓰는 굵은 기본 산세리프 — 손글씨체보다 훨씬 잘
 * 읽혀서(실기기 피드백 반영) Gaegu/여기어때 잘난체 대신 이걸로 통일했다. */
private val ShareTextFontFamily = FontFamily.Default

/** 물방울 핀 이미지 5종(노랑·빨강·초록·파랑·핑크) — 장소가 5개를 넘으면 순서대로 반복한다. */
private val PIN_MARKER_DRAWABLES = listOf(
    R.drawable.ic_marker_yellow,
    R.drawable.ic_marker_red,
    R.drawable.ic_marker_green,
    R.drawable.ic_marker_blue,
    R.drawable.ic_marker_pink,
)

// 아래 4개는 전부 "또렷한 영역 폭 대비 비율"이다 — 고정 dp 대신 비율로 잡아야 기기와 무관하게
// 항상 같은 비례로 보인다. 핀 크기 자체를 더 키우기보다(0.60까지 키웠다가 다시 줄임) 칸 안에서
// 핀이 차지하는 비율을 줄여서(0.60 → 0.50) 핀 사이 가로 점선이 더 길고 여유 있게 보이도록
// 했다. 행 간격은 다시 넉넉하게(1.45 → 1.6) — 라벨이 2줄로 줄바꿈될 때도 위 행과 안 겹치게.
private const val COLUMN_WIDTH_FRACTION = 0.42f
private const val REGION_SIDE_PADDING_FRACTION = 0.04f
private const val ROW_TO_COLUMN_RATIO = 1.6f
private const val PIN_TO_COLUMN_RATIO = 0.50f
/** ic_marker_*.png 원본(211×240) 실제 가로:세로 비율 — 핀 이미지가 안 찌그러지게 이 비율로
 * 박스를 잡는다. */
private const val PIN_HEIGHT_TO_WIDTH_RATIO = 240f / 211f
/** 핀 이미지 안에서 실제로 뾰족한 꼬리 끝(장소 지점)이 있는 세로 위치(이미지 높이 대비 비율).
 * 이미지 하단에는 은은한 글로우 여백이 조금 더 남아있어 꼬리 끝이 이미지 바닥과 정확히 일치하지
 * 않는다 — 그 자리를 지도상의 실제 좌표(장소 지점)에 맞추기 위해 보정용으로 쓴다. */
private const val PIN_TIP_Y_FRACTION = 0.92f
/** 핀 머리(동그란 부분) 중심의 세로 위치(이미지 높이 대비 비율) — 번호를 이 자리에 겹쳐 그린다. */
private const val PIN_NUMBER_CENTER_Y_FRACTION = 0.37f

/** 같은 줄 옆 칸 라벨과 안 닿도록, 라벨 폭을 칸 폭의 이 비율만큼만 쓴다(양옆에 여백이 남는다). */
private const val LABEL_WIDTH_TO_COLUMN_RATIO = 0.92f

private const val LABEL_MIN_FONT_SIZE_SP = 9f

/** 공유 이미지 핀 보드에 표시할 최대 장소 수 — 이보다 많으면 처음 이곳까지만 보드에 그리고
 * (제목 아래 "장소 n곳" 부제는 잘리지 않은 실제 전체 개수를 그대로 보여준다), 저장 시 안내
 * 문구를 띄운다. [ZigzagCourseBoard]/[CourseTitleHeader]는 건드리지 않았다. */
private const val MAX_SHARE_PLACES = 12

/** 화면이 배경 원본과 거의 같은 비율이면 Crop이 자연히 잘라내는 여백이 [SHARP_LEFT] 등과
 * 거의 같아져서 계산된 영역 인셋이 0에 가까워질 수 있다 — 제목이 화면 가장자리에 붙어
 * 잘려 보이는 걸 막기 위해 항상 최소로 확보하는 안쪽 여백. */
private val REGION_INNER_MARGIN = 18.dp

/**
 * 화면 비율이 배경 원본(9:16)과 다를 때 [ContentScale.Crop] + 중앙 정렬이 실제로 얼마나
 * 잘라내는지 계산해서, "또렷한 영역"([SHARP_LEFT] 등)이 화면 좌표로 어디에 해당하는지
 * 되돌려 계산한다. 화면이 원본보다 좁고 길면(대부분의 폰) 세로는 꽉 차고 가로만 잘리고,
 * 반대면 가로가 꽉 차고 세로만 잘린다.
 */
private data class SharpRegion(val left: Float, val right: Float, val top: Float, val bottom: Float)

private fun computeSharpRegion(screenWidthPx: Float, screenHeightPx: Float): SharpRegion {
    val screenAspect = screenWidthPx / screenHeightPx
    return if (screenAspect <= IMAGE_ASPECT) {
        val visibleWFrac = screenAspect / IMAGE_ASPECT
        val cropLeft = (1f - visibleWFrac) / 2f
        SharpRegion(
            left = ((SHARP_LEFT - cropLeft) / visibleWFrac).coerceIn(0f, 1f),
            right = ((SHARP_RIGHT - cropLeft) / visibleWFrac).coerceIn(0f, 1f),
            top = SHARP_TOP,
            bottom = SHARP_BOTTOM,
        )
    } else {
        val visibleHFrac = IMAGE_ASPECT / screenAspect
        val cropTop = (1f - visibleHFrac) / 2f
        SharpRegion(
            left = SHARP_LEFT,
            right = SHARP_RIGHT,
            top = ((SHARP_TOP - cropTop) / visibleHFrac).coerceIn(0f, 1f),
            bottom = ((SHARP_BOTTOM - cropTop) / visibleHFrac).coerceIn(0f, 1f),
        )
    }
}

/** 화면 전체(배경+제목+핀 보드) — 이 컴포저블 전체가 캡처 대상이다. */
@Composable
private fun ShareCardContent(course: Course, background: ShareBackground, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    // 제목의 실제 렌더링 높이(줄바꿈 여부에 따라 달라짐)를 측정해서, 핀 1행이 제목 아래로
    // 최소 MIN_TITLE_TO_BOARD_GAP만큼 떨어지게 한다. 첫 프레임엔 0이라 잠깐 더 위에서
    // 시작했다가 측정되는 즉시 아래로 스냅되는데, 실제 저장 캡처는 사용자가 버튼을 눌러야만
    // 일어나서(그때는 이미 여러 프레임 지난 뒤) 결과물에는 영향이 없다.
    var titleHeightPx by remember { mutableStateOf(0f) }

    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val region = remember(widthPx, heightPx) { computeSharpRegion(widthPx, heightPx) }

        Image(
            painter = painterResource(background.drawableRes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
        )

        val marginPx = with(density) { REGION_INNER_MARGIN.toPx() }
        val regionLeftPx = region.left * widthPx + marginPx
        val regionTopPx = region.top * heightPx
        val regionWidthPx = (((region.right - region.left) * widthPx) - marginPx * 2).coerceAtLeast(1f)
        val regionHeightPx = ((region.bottom - region.top) * heightPx).coerceAtLeast(1f)

        Box(
            modifier = Modifier
                .offset { IntOffset(regionLeftPx.roundToInt(), regionTopPx.roundToInt()) }
                .width(with(density) { regionWidthPx.toDp() })
                .height(with(density) { regionHeightPx.toDp() }),
        ) {
            CourseTitleHeader(
                course = course,
                modifier = Modifier
                    .align(if (background.titleOnRight) Alignment.TopEnd else Alignment.TopStart)
                    .padding(top = TITLE_TOP_PADDING)
                    .fillMaxWidth(background.titleWidthFraction)
                    .onGloballyPositioned { coordinates -> titleHeightPx = coordinates.size.height.toFloat() },
                textAlign = if (background.titleOnRight) TextAlign.End else TextAlign.Start,
                horizontalAlignment = if (background.titleOnRight) Alignment.End else Alignment.Start,
            )

            val boardPlaces = remember(course.places) { course.places.take(MAX_SHARE_PLACES) }
            val boardPaddingPx = regionWidthPx * REGION_SIDE_PADDING_FRACTION
            val maxColPx = regionWidthPx * COLUMN_WIDTH_FRACTION
            // 핀 위에 장소명 라벨이 한 줄 더 얹히니(라벨 높이만큼 위에서 다시 안 겹치게), 그 라벨
            // 한 칸 몫(estimateLabelAllowancePx)까지 포함해서 "핀 1행이 시작될 수 있는 최소 y"를 잡는다.
            // 실제 장소명은 길어서 2줄로 줄바꿈되는 경우가 흔해(예: "세븐일레븐 마포홍대점") 40dp로는
            // 부족해 겹치는 문제가 있었다 — 2줄까지 여유 있게 담기도록 56dp로 늘렸다. 아래
            // [ZigzagCourseBoard]의 labelAllowancePx와 반드시 같은 값을 써야 한다(실제 라벨 박스
            // 높이가 여기서 예약한 만큼과 어긋나면 다시 겹칠 수 있다).
            val estimateLabelAllowancePx = with(density) { LABEL_ALLOWANCE.toPx() }
            val minBoardTopPx = with(density) { TITLE_TOP_PADDING.toPx() } + titleHeightPx +
                with(density) { MIN_TITLE_TO_BOARD_GAP.toPx() } + estimateLabelAllowancePx
            val metrics = remember(boardPlaces, regionWidthPx, regionHeightPx, minBoardTopPx) {
                computeBoardMetrics(
                    placeCount = boardPlaces.size,
                    regionWidthPx = regionWidthPx,
                    regionHeightPx = regionHeightPx,
                    boardPaddingPx = boardPaddingPx,
                    maxColPx = maxColPx,
                    minBoardTopPx = minBoardTopPx,
                )
            }
            ZigzagCourseBoard(places = boardPlaces, metrics = metrics)
        }
    }
}

/**
 * 제목("OO 나들이") + 부제(장소 수 / 전체 거리, 2줄) — 알약/배너 배경 없이 배경 이미지 위에
 * 바로 얹는다. 흰 굵은 글자 + 옅은 그림자로 가독성을 확보하고, 아래에 짧은 포인트 밑줄을
 * 곁들인다(하트 장식은 뺐다). 손글씨체(Gaegu/잘난체) 대신 굵은 기본 산세리프([ShareTextFontFamily])를
 * 써서 실기기에서도 또렷하게 읽히게 했다.
 */
@Composable
private fun CourseTitleHeader(
    course: Course,
    modifier: Modifier = Modifier,
    textAlign: TextAlign,
    horizontalAlignment: Alignment.Horizontal,
) {
    val totalMeters = remember(course.places) {
        course.places.zipWithNext().sumOf { (a, b) ->
            WalkingDistance.haversineMeters(a.latitude, a.longitude, b.latitude, b.longitude)
        }
    }
    val titleShadow = Shadow(color = Color.Black.copy(alpha = 0.45f), offset = Offset(1.5f, 2f), blurRadius = 6f)
    val subtitleShadow = Shadow(color = Color.Black.copy(alpha = 0.4f), offset = Offset(1f, 1.5f), blurRadius = 4f)
    val subtitleStyle = TextStyle(
        fontFamily = ShareTextFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        shadow = subtitleShadow,
        textAlign = textAlign,
    )

    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Text(
            text = course.title,
            style = TextStyle(
                fontFamily = ShareTextFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 27.sp,
                color = Color.White,
                shadow = titleShadow,
                textAlign = textAlign,
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFFF8FAB).copy(alpha = 0.85f)),
        )
        Spacer(modifier = Modifier.height(6.dp))
        // 장소 수 / 전체 이동 거리를 한 줄에 "·"로 잇지 않고 2줄로 나눠 보여준다.
        Text(text = "장소 ${course.places.size}곳", style = subtitleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            text = "전체 이동 거리 약 ${formatTotalKm(totalMeters)}",
            style = subtitleStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 핀 보드 배치 계산 결과. [positions]는 "또렷한 영역" 로컬 좌표(px)로, 각 핀의 "꼬리 끝(장소 지점)" 위치다. */
private data class BoardMetrics(
    val columns: Int,
    val colWidthPx: Float,
    val rowHeightPx: Float,
    val pinWidthPx: Float,
    val positions: List<Offset>,
    val scaleFactor: Float,
)

/**
 * 장소 개수·또렷한 영역 크기로 지그재그 핀 보드를 배치한다. 칸 폭·핀 크기는 영역 폭 대비
 * 비율로 고정해서 기기와 무관하게 항상 같은 비례로 보이게 하고, 자연 높이가 영역 안(카드
 * 높이의 [SKY_TOP_FRACTION]~[SKY_BOTTOM_FRACTION], 단 제목 아래 최소 간격이 더 아래라면
 * 그쪽을 우선)에 안 들어갈 때만 전체를 같은 비율로 축소한다(최소 75%까지만 — 그 아래로는
 * 가독성이 무너져서, 극단적으로 장소가 많으면 잘리는 쪽을 허용한다. 보드가 화면 가운데에 너무
 * 작게 몰려 보인다는 피드백을 반영해 하한을 45%→55%→70%→80%→75%로 조정했다 — 80%까지
 * 올렸을 땐 보드 자체가 너무 커져서 12곳짜리 코스에서 마지막 줄이 화면 아래로 잘렸다).
 * 보드가 세로 예산보다 작게 나올 때 남는 공간을 어떻게 나눌지는 [BOARD_TOP_LEFTOVER_BIAS],
 * 1행 위 여백을 얼마나 줄였는지는 [FIRST_ROW_TOP_BUFFER_RATIO] 참고.
 */
private fun computeBoardMetrics(
    placeCount: Int,
    regionWidthPx: Float,
    regionHeightPx: Float,
    boardPaddingPx: Float,
    maxColPx: Float,
    minBoardTopPx: Float,
): BoardMetrics {
    if (placeCount == 0) return BoardMetrics(1, 1f, 1f, 1f, emptyList(), 1f)

    val columns = computeColumns(placeCount)
    val rows = (placeCount + columns - 1) / columns
    val rawColPx = ((regionWidthPx - boardPaddingPx * 2) / columns).coerceAtLeast(1f)
    val baseColPx = minOf(rawColPx, maxColPx)
    val baseRowPx = baseColPx * ROW_TO_COLUMN_RATIO
    val basePinWidthPx = baseColPx * PIN_TO_COLUMN_RATIO

    val skyTopPx = maxOf(regionHeightPx * SKY_TOP_FRACTION, minBoardTopPx)
    val skyBottomPx = regionHeightPx * SKY_BOTTOM_FRACTION
    val skyHeightPx = (skyBottomPx - skyTopPx).coerceAtLeast(1f)

    // 각 행 위에 얹히는 장소명 라벨 한 줄 몫의 여유를 자연 높이에 포함해서 축소 여부를 판단한다.
    val naturalBoardHeightPx = rows * baseRowPx + boardPaddingPx * 2 + baseRowPx * FIRST_ROW_TOP_BUFFER_RATIO
    val scaleFactor = if (naturalBoardHeightPx > skyHeightPx) {
        (skyHeightPx / naturalBoardHeightPx).coerceIn(0.80f, 1f)
    } else {
        1f
    }

    val colPx = baseColPx * scaleFactor
    val rowPx = baseRowPx * scaleFactor
    val pinWidthPx = (basePinWidthPx * scaleFactor).coerceAtMost(colPx * 0.92f)
    val scaledPaddingPx = boardPaddingPx * scaleFactor
    val gridWidthPx = colPx * columns
    val leftInsetPx = scaledPaddingPx + (regionWidthPx - scaledPaddingPx * 2 - gridWidthPx).coerceAtLeast(0f) / 2f
    val boardHeightPx = rows * rowPx + scaledPaddingPx * 2 + rowPx * FIRST_ROW_TOP_BUFFER_RATIO
    val topInsetPx = skyTopPx +
        (skyHeightPx - boardHeightPx).coerceAtLeast(0f) * BOARD_TOP_LEFTOVER_BIAS +
        scaledPaddingPx + rowPx * FIRST_ROW_TOP_BUFFER_RATIO

    val positions = computeGridPositions(placeCount, columns, colPx, rowPx).map {
        Offset(it.x + leftInsetPx, it.y + topInsetPx)
    }

    return BoardMetrics(columns, colPx, rowPx, pinWidthPx, positions, scaleFactor)
}

/** 장소 개수에 따라 한 줄에 들어갈 핀 개수를 정한다. 참고 목업(8곳 → 3-3-2)에 맞춰
 * 중간 규모는 3열을 쓴다 — 가로 간격(핀 사이 점선)이 더 넉넉해 보이도록 12곳까지는 4열로
 * 넘어가지 않고 3열을 유지하고(세로로 늘어나는 대신), 그보다 많아질 때만 4열을 쓴다. */
private fun computeColumns(placeCount: Int): Int = when {
    placeCount <= 3 -> placeCount.coerceAtLeast(1)
    placeCount <= 12 -> 3
    else -> 4
}

/**
 * 지그재그(뱀 모양) 말판 위 각 장소의 중심 좌표(px). 짝수 줄(0, 2, 4...)은 왼쪽→오른쪽,
 * 홀수 줄은 오른쪽→왼쪽으로 열 방향을 뒤집어, 줄이 끝나는 지점이 다음 줄의 시작점과
 * 자연스럽게 이어지게 한다.
 */
private fun computeGridPositions(count: Int, columns: Int, colPx: Float, rowPx: Float): List<Offset> =
    (0 until count).map { index ->
        val row = index / columns
        val colInRow = index % columns
        val col = if (row % 2 == 0) colInRow else (columns - 1 - colInRow)
        Offset(
            x = col * colPx + colPx / 2f,
            y = row * rowPx + rowPx / 2f,
        )
    }

@Composable
private fun ZigzagCourseBoard(places: List<CoursePlace>, metrics: BoardMetrics) {
    if (places.isEmpty()) return
    val density = LocalDensity.current
    val positions = metrics.positions
    val pinHeightPx = metrics.pinWidthPx * PIN_HEIGHT_TO_WIDTH_RATIO
    val labelFontSize = (15 * metrics.scaleFactor).coerceAtLeast(LABEL_MIN_FONT_SIZE_SP).sp
    val numberFontSize = (22 * metrics.scaleFactor).coerceAtLeast(13f).sp
    val labelAllowancePx = with(density) { 40.dp.toPx() } * metrics.scaleFactor
    val labelWidthPx = metrics.colWidthPx * LABEL_WIDTH_TO_COLUMN_RATIO

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (positions.size >= 2) {
            drawPath(
                path = buildSmoothPath(positions),
                color = Color.White.copy(alpha = 0.9f),
                style = Stroke(
                    width = with(density) { (3.dp * metrics.scaleFactor).toPx() },
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(
                            with(density) { (9.dp * metrics.scaleFactor).toPx() },
                            with(density) { (7.dp * metrics.scaleFactor).toPx() },
                        ),
                    ),
                ),
            )

            // 목업처럼 점선이 각 핀에 닿는 자리마다 은은하게 빛나는 원을 얹는다.
            val glowRadiusPx = with(density) { (7.dp * metrics.scaleFactor).toPx() }
            positions.zipWithNext().forEach { (p0, p1) ->
                val dx = p1.x - p0.x
                val dy = p1.y - p0.y
                val glowPoints = listOf(
                    Offset(p0.x + dx * 0.16f, p0.y + dy * 0.16f),
                    Offset(p0.x + dx * 0.84f, p0.y + dy * 0.84f),
                )
                glowPoints.forEach { point ->
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0f)),
                            center = point,
                            radius = glowRadiusPx,
                        ),
                        radius = glowRadiusPx,
                        center = point,
                    )
                }
            }
        }
    }

    places.forEachIndexed { index, place ->
        val center = positions[index]
        val pinDrawableRes = PIN_MARKER_DRAWABLES[index % PIN_MARKER_DRAWABLES.size]

        // 장소명 라벨 — 핀 "위"에 올려서(목업 배치) 아래 태그 텍스트와 자리가 안 겹친다.
        // 라벨 칸의 아래쪽 끝을 (핀 상단 - 여백)에 고정해두고, 안쪽 텍스트는 바닥에 붙여
        // 1줄이든 2줄이든 항상 핀 바로 위에서 끝나게 한다.
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (center.x - labelWidthPx / 2f).roundToInt(),
                        (center.y - pinHeightPx - with(density) { 8.dp.toPx() } - labelAllowancePx).roundToInt(),
                    )
                }
                .width(with(density) { labelWidthPx.toDp() })
                .height(with(density) { labelAllowancePx.toDp() }),
            contentAlignment = Alignment.BottomCenter,
        ) {
            PlaceNameLabel(text = place.placeName, baseFontSize = labelFontSize)
        }

        PinMarker(
            number = index + 1,
            drawableRes = pinDrawableRes,
            widthPx = metrics.pinWidthPx,
            numberFontSize = numberFontSize,
            modifier = Modifier.offset {
                // 이미지의 실제 꼬리 끝은 바닥이 아니라 PIN_TIP_Y_FRACTION 지점에 있어서,
                // 그 지점이 장소 좌표(center)에 오도록 그만큼만 위로 올린다.
                IntOffset(
                    (center.x - metrics.pinWidthPx / 2f).roundToInt(),
                    (center.y - pinHeightPx * PIN_TIP_Y_FRACTION).roundToInt(),
                )
            },
        )
    }

    places.zipWithNext().forEachIndexed { segmentIndex, (from, to) ->
        val p0 = positions[segmentIndex]
        val p1 = positions[segmentIndex + 1]
        // 줄이 꺾이는 구간(다음 핀이 다음 줄로 넘어감)은 라벨을 곡선 중앙이 아니라 시작 핀(p0) 쪽으로
        // 당겨서, 다음 줄 핀의 장소명 라벨과 겹치지 않게 한다.
        val isTurnSegment = (segmentIndex + 1) % metrics.columns == 0
        val t = if (isTurnSegment) 0.28f else 0.5f
        val anchor = Offset(p0.x + (p1.x - p0.x) * t, p0.y + (p1.y - p0.y) * t)

        val dx = p1.x - p0.x
        val dy = p1.y - p0.y
        val length = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val perpPx = with(density) { (20.dp * metrics.scaleFactor).toPx() }
        // abs(dx): 같은 줄 안에서도 오른쪽→왼쪽으로 진행하는 구간(짝수 행)은 dx가 음수라 그대로
        // -dx를 쓰면 라벨이 선 아래로 뒤집힌다 — 진행 방향과 무관하게 항상 선 위쪽에 붙게 한다.
        val tagCenter = Offset(anchor.x + (dy / length) * perpPx, anchor.y + (-abs(dx) / length) * perpPx)

        val distanceMeters = WalkingDistance.haversineMeters(from.latitude, from.longitude, to.latitude, to.longitude)
        val tagWidthPx = with(density) { (84.dp * metrics.scaleFactor).toPx() }

        SegmentTagStack(
            distanceMeters = distanceMeters,
            scaleFactor = metrics.scaleFactor,
            modifier = Modifier
                .offset {
                    IntOffset(
                        (tagCenter.x - tagWidthPx / 2f).roundToInt(),
                        (tagCenter.y - with(density) { 18.dp.toPx() }).roundToInt(),
                    )
                }
                .width(with(density) { tagWidthPx.toDp() }),
        )
    }
}

/**
 * 물방울(지도 마커) 모양 핀 — 더 이상 Path로 직접 그리지 않고, res/drawable에 넣어둔 유리질감
 * 물방울 아이콘([drawableRes], ic_marker_yellow/red/green/blue/pink)을 그대로 쓴다. 그림자·하이라이트·
 * 은은한 글로우가 이미 이미지 안에 녹아있어서 별도로 더 그릴 게 없다. 번호는 그 위에
 * [PIN_NUMBER_CENTER_Y_FRACTION] 높이(핀 머리 중심)에 겹쳐 그린다.
 */
@Composable
private fun PinMarker(
    number: Int,
    drawableRes: Int,
    widthPx: Float,
    numberFontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val widthDp = with(density) { widthPx.toDp() }
    val heightDp = widthDp * PIN_HEIGHT_TO_WIDTH_RATIO
    Box(modifier = modifier.size(widthDp, heightDp)) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit,
        )
        // 핀 머리(동그란 부분) 중심에 번호를 겹친다 — 위쪽 2×PIN_NUMBER_CENTER_Y_FRACTION 높이의
        // 박스를 잡고 가운데 정렬하면, 박스 중심이 정확히 그 비율 지점에 오게 된다.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp * PIN_NUMBER_CENTER_Y_FRACTION * 2f)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$number",
                fontFamily = ShareTextFontFamily,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = numberFontSize,
            )
        }
    }
}

/**
 * 장소명 라벨 — 말줄임표로 잘라내지 않고, 최대 2줄까지 줄바꿈하다가 그래도 넘치면
 * [LABEL_MIN_FONT_SIZE_SP]까지 글자 크기를 1sp씩 줄여가며 전체 텍스트가 다 보이게 한다.
 */
@Composable
private fun PlaceNameLabel(text: String, baseFontSize: TextUnit, modifier: Modifier = Modifier) {
    var fontSize by remember(text, baseFontSize) { mutableStateOf(baseFontSize) }
    val shadow = Shadow(color = Color.Black.copy(alpha = 0.35f), offset = Offset(0.5f, 1f), blurRadius = 3f)
    Text(
        text = text,
        style = TextStyle(
            fontFamily = ShareTextFontFamily,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            shadow = shadow,
        ),
        maxLines = 2,
        overflow = TextOverflow.Clip,
        modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp),
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize.value > LABEL_MIN_FONT_SIZE_SP) {
                fontSize = (fontSize.value - 1f).sp
            }
        },
    )
}

/**
 * 구간 거리 숫자만 표시한다 — "가까워요"/"도보 가능" 같은 거리 구간 태그는 뺐다. 목업처럼
 * 완전히 둥근 흰 알약(스타디움 모양) 배지 안에 짙은 글씨로 넣어서, 배경 그림 위에서도 대비가
 * 뚜렷하고 가독성이 좋게 했다.
 */
@Composable
private fun SegmentTagStack(distanceMeters: Int, scaleFactor: Float, modifier: Modifier = Modifier) {
    val pillShape = RoundedCornerShape(50)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = formatDistance(distanceMeters),
            style = TextStyle(
                fontFamily = ShareTextFontFamily,
                color = Color(0xFF3A3A3A),
                fontWeight = FontWeight.Bold,
                fontSize = (13 * scaleFactor).coerceAtLeast(9f).sp,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            modifier = Modifier
                .shadow(elevation = 2.dp * scaleFactor, shape = pillShape, clip = false)
                .background(Color.White.copy(alpha = 0.94f), pillShape)
                .padding(horizontal = 10.dp * scaleFactor, vertical = 4.dp * scaleFactor),
        )
    }
}

/**
 * 카트멀롬(Catmull-Rom) 스플라인을 3차 베지어로 변환해 [points]를 부드럽게 잇는 경로를 만든다.
 * 같은 줄 안의 핀 사이뿐 아니라 줄이 꺾이는 구간도 같은 점 나열의 일부로 취급돼 자연스럽게
 * 곡선으로 이어진다.
 */
private fun buildSmoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    if (points.size == 1) return path

    val extended = buildList {
        add(points.first())
        addAll(points)
        add(points.last())
    }
    for (i in 1 until extended.size - 2) {
        val p0 = extended[i - 1]
        val p1 = extended[i]
        val p2 = extended[i + 1]
        val p3 = extended[i + 2]
        val cp1 = Offset(p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f)
        val cp2 = Offset(p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f)
        path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
    }
    return path
}

private fun formatTotalKm(totalMeters: Int): String = "%.1fkm".format(totalMeters / 1000.0)

/**
 * [view](다이얼로그를 호스팅하는 AndroidComposeView) 전체를 [android.view.View.draw]로 그대로
 * 비트맵에 그린 뒤, [boundsInWindow]에 해당하는 카드 영역만 잘라낸다. 그리기 전에 [setChromeHidden]
 * 으로 X/배경전환/저장 같은 다이얼로그 자체 UI를 컴포지션에서 잠깐 빼고, 그 상태 변화가 실제로
 * 반영된 프레임이 그려지도록 두 번 기다린 다음 캡처하고, 끝나면 바로 복원한다. `view.draw()`는
 * 동기 소프트웨어 드로우라 별도 창이나 비동기 콜백을 판독할 일이 없다.
 */
private suspend fun captureViewToBitmap(
    view: android.view.View,
    boundsInWindow: Rect,
    setChromeHidden: (Boolean) -> Unit,
): Bitmap {
    setChromeHidden(true)
    withFrameNanos {}
    withFrameNanos {}

    val full = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(full)
    view.draw(canvas)

    setChromeHidden(false)

    val rect = android.graphics.Rect(
        boundsInWindow.left.roundToInt().coerceIn(0, full.width),
        boundsInWindow.top.roundToInt().coerceIn(0, full.height),
        boundsInWindow.right.roundToInt().coerceIn(0, full.width),
        boundsInWindow.bottom.roundToInt().coerceIn(0, full.height),
    )
    return Bitmap.createBitmap(full, rect.left, rect.top, rect.width().coerceAtLeast(1), rect.height().coerceAtLeast(1))
}

/**
 * 완성된 카드 비트맵을 기기 갤러리(Pictures/KkokDay)에 저장한다. Android 10(API 29)부터는
 * `MediaStore`의 scoped storage 경로를 써서 별도 저장소 권한이 필요 없다. 그보다 낮은
 * 버전(minSdk 24까지 지원)에서는 레거시 `MediaStore.Images.Media.insertImage`로 폴백한다
 * (이 경로는 기기에 따라 WRITE_EXTERNAL_STORAGE 권한이 필요할 수 있다).
 */
private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, courseId: String): Uri? {
    val filename = "course_${courseId}_${System.currentTimeMillis()}.png"
    val resolver = context.contentResolver

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/KkokDay")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        resolver.openOutputStream(uri)?.use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            ?: return null
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    } else {
        @Suppress("DEPRECATION")
        val inserted = MediaStore.Images.Media.insertImage(resolver, bitmap, filename, null)
        inserted?.let { Uri.parse(it) }
    }
}
