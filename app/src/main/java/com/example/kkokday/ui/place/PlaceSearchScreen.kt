package com.example.kkokday.ui.place

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.data.place.KakaoPlace
import com.example.kkokday.ui.theme.KkokDayMainBackground
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainRed
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTheme

@Composable
fun PlaceSearchScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlaceSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.locationConfirmed) {
        if (uiState.locationConfirmed) {
            viewModel.consumeLocationConfirmed()
            onNavigateBack()
        }
    }

    PlaceSearchScreenContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearchClick = viewModel::onSearchClick,
        onPlaceClick = viewModel::onPlaceSelected,
        onConfirmClick = viewModel::onConfirmClick,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun PlaceSearchScreenContent(
    uiState: PlaceSearchUiState,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onPlaceClick: (KakaoPlace) -> Unit,
    onConfirmClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Surface(modifier = Modifier.fillMaxSize(), color = KkokDayMainBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = KkokDayMainTextDark,
                    )
                }
                Text(
                    text = "위치 지정",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                )
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("숙소명이나 주소를 입력해주세요") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            onSearchClick()
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "검색")
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onSearchClick()
                        },
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KkokDayMainYellow,
                        unfocusedBorderColor = KkokDayMainCardBorder,
                        focusedLabelColor = KkokDayMainYellow,
                        cursorColor = KkokDayMainYellow,
                        focusedTextColor = KkokDayMainTextDark,
                        unfocusedTextColor = KkokDayMainTextDark,
                        focusedContainerColor = KkokDayMainBackground,
                        unfocusedContainerColor = KkokDayMainBackground,
                        focusedPlaceholderColor = KkokDayMainSubText,
                        unfocusedPlaceholderColor = KkokDayMainSubText,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                uiState.selectedPlace?.let { place ->
                    Spacer(modifier = Modifier.height(12.dp))
                    SelectedPlaceField(place = place)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onConfirmClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KkokDayMainYellow,
                            contentColor = KkokDayMainTextDark,
                        ),
                    ) {
                        Text(
                            text = "이 위치로 콕 찍기",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 장소를 선택한 뒤에는 위의 SelectedPlaceField만 보이면 되므로 결과 목록/안내
            // 영역은 그린다. 재검색(onSearchClick)이 selectedPlace를 다시 null로 돌려놓기
            // 전까지는 이 영역을 비워 둔다.
            if (uiState.selectedPlace == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState.status) {
                        PlaceSearchStatus.IDLE -> SearchPrompt()
                        PlaceSearchStatus.LOADING -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = KkokDayMainYellow,
                        )
                        PlaceSearchStatus.EMPTY -> EmptySearchResults()
                        PlaceSearchStatus.ERROR -> CenteredMessage(
                            uiState.errorMessage ?: "오류가 발생했어요",
                            color = MaterialTheme.colorScheme.error,
                        )
                        PlaceSearchStatus.SUCCESS -> LazyColumn(
                            contentPadding = SearchResultsPadding,
                        ) {
                            items(uiState.results, key = { it.id }) { place ->
                                PlaceResultRow(place = place, onClick = { onPlaceClick(place) })
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private val SearchResultsPadding = PaddingValues(horizontal = 20.dp)

@Composable
private fun BoxScope.CenteredMessage(text: String, color: Color = KkokDayMainSubText) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .align(Alignment.Center)
            .padding(top = 40.dp),
    )
}

@Composable
private fun BoxScope.SearchPrompt() {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.TravelExplore,
            contentDescription = null,
            tint = KkokDayMainYellow,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "어디로 떠나볼까요?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "숙소명이나 주소를 입력하면\n주변 장소를 찾아드려요",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BoxScope.EmptySearchResults() {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            tint = KkokDayMainYellow,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "검색 결과가 없어요",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "숙소명이나 주소를 다시 확인하고\n다른 키워드로 검색해보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PlaceResultRow(place: KakaoPlace, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, KkokDayMainCardBorder),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = KkokDayMainRed,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.placeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = place.roadAddressName ?: place.addressName,
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDayMainSubText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 검색창 아래에 뜨는 "선택한 장소" 필드. 이름/주소만 보여준다 — 좌표는 화면에는
 * 노출하지 않고 [PlaceSearchUiState.selectedPlace]에만 담아 다음 화면(카테고리별
 * 주변 검색)에 그대로 넘긴다.
 */
@Composable
private fun SelectedPlaceField(place: KakaoPlace) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, KkokDayMainYellow),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = KkokDayMainYellow,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.placeName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = place.roadAddressName ?: place.addressName,
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDayMainSubText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PlaceSearchScreenIdlePreview() {
    KkokDayTheme {
        PlaceSearchScreenContent(
            uiState = PlaceSearchUiState(),
            onQueryChange = {},
            onSearchClick = {},
            onPlaceClick = {},
            onConfirmClick = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PlaceSearchScreenEmptyPreview() {
    KkokDayTheme {
        PlaceSearchScreenContent(
            uiState = PlaceSearchUiState(query = "존재하지않는장소12345", status = PlaceSearchStatus.EMPTY),
            onQueryChange = {},
            onSearchClick = {},
            onPlaceClick = {},
            onConfirmClick = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PlaceSearchScreenResultsPreview() {
    KkokDayTheme {
        PlaceSearchScreenContent(
            uiState = PlaceSearchUiState(
                query = "해운대",
                status = PlaceSearchStatus.SUCCESS,
                results = listOf(
                    KakaoPlace(
                        id = "1",
                        placeName = "해운대해수욕장",
                        addressName = "부산 해운대구 우동",
                        roadAddressName = "부산 해운대구 해운대해변로 264",
                        longitude = 129.1603,
                        latitude = 35.1587,
                    ),
                ),
            ),
            onQueryChange = {},
            onSearchClick = {},
            onPlaceClick = {},
            onConfirmClick = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PlaceSearchScreenSelectedPreview() {
    KkokDayTheme {
        PlaceSearchScreenContent(
            uiState = PlaceSearchUiState(
                query = "해운대",
                status = PlaceSearchStatus.SUCCESS,
                selectedPlace = KakaoPlace(
                    id = "1",
                    placeName = "해운대해수욕장",
                    addressName = "부산 해운대구 우동",
                    roadAddressName = "부산 해운대구 해운대해변로 264",
                    longitude = 129.1603,
                    latitude = 35.1587,
                ),
            ),
            onQueryChange = {},
            onSearchClick = {},
            onPlaceClick = {},
            onConfirmClick = {},
            onNavigateBack = {},
        )
    }
}
