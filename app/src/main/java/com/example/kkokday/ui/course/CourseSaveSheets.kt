package com.example.kkokday.ui.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kkokday.data.course.Course
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow

/** 코스 탭에서 "+"로 새 코스를 만들 때 뜨는 이름(필수)+설명(선택) 입력 다이얼로그. */
@Composable
fun NewCourseDialog(
    onConfirm: (title: String, description: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("새 코스 만들기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KkokDayMainTextDark)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("코스 이름") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KkokDayMainYellow,
                        unfocusedBorderColor = KkokDayMainCardBorder,
                        cursorColor = KkokDayMainYellow,
                        focusedTextColor = KkokDayMainTextDark,
                        unfocusedTextColor = KkokDayMainTextDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedLabelColor = KkokDayMainYellow,
                        unfocusedLabelColor = KkokDayMainSubText,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명(선택)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KkokDayMainYellow,
                        unfocusedBorderColor = KkokDayMainCardBorder,
                        cursorColor = KkokDayMainYellow,
                        focusedTextColor = KkokDayMainTextDark,
                        unfocusedTextColor = KkokDayMainTextDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedLabelColor = KkokDayMainYellow,
                        unfocusedLabelColor = KkokDayMainSubText,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title.trim(), description.trim()) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
            ) {
                Text("만들기", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = KkokDayMainSubText)
            }
        },
    )
}

/**
 * "담기" 클릭 시 바로 뜨는 보유 코스 목록. 하나 고르면 그 코스 끝에 선택한 장소들이 붙는다.
 * 코스가 하나도 없으면 코스 탭에서 먼저 만들라는 안내와, 있으면 그 탭으로 바로 이동하는
 * 버튼([onNavigateToCourseTab])을 보여준다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExistingCoursePickerBottomSheet(
    courses: List<Course>,
    isLoading: Boolean,
    onCourseSelected: (Course) -> Unit,
    onDismiss: () -> Unit,
    onCreateNewCourseClick: (() -> Unit)? = null,
    onNavigateToCourseTab: (() -> Unit)? = null,
    title: String = "코스에 담기",
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (onCreateNewCourseClick != null) {
                OutlinedButton(
                    onClick = onCreateNewCourseClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KkokDayMainTextDark),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = KkokDayMainYellow)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("새 코스 만들기", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            when {
                isLoading -> Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KkokDayMainYellow)
                }
                courses.isEmpty() -> Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Filled.SearchOff, contentDescription = null, tint = KkokDayMainYellow, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "만들어둔 코스가 없어요",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = KkokDayMainTextDark,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "코스 탭에서 먼저 코스를 만들어주세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = KkokDayMainSubText,
                        textAlign = TextAlign.Center,
                    )
                    if (onNavigateToCourseTab != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToCourseTab,
                            colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
                        ) {
                            Text("코스 탭으로 이동", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(courses, key = { it.id }) { course ->
                        CourseListItem(course = course, onClick = { onCourseSelected(course) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CourseListItem(course: Course, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "장소 ${course.places.size}개",
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDayMainSubText,
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = KkokDayMainSubText,
            )
        }
    }
}
