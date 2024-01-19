package com.mvukosav.scoreagentsvas.match.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mvukosav.scoreagentsvas.match.presentation.home.HomeScreenState
import com.mvukosav.scoreagentsvas.match.presentation.home.HomeScreenUiEvent
import com.mvukosav.scoreagentsvas.match.presentation.home.HomeScreenViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.events.collectLatest {
            when (it) {
                is HomeScreenUiEvent.NavigateToMatchDetails -> {
                    navController.navigate("match_details/?matchId=${it.matchId}")
                    viewModel.events.value = null
                }

                null -> {}
            }
        }
    }
    when (state) {
        is HomeScreenState.Data -> HomeScreenData(state = state as HomeScreenState.Data)
        is HomeScreenState.Error -> ErrorScreen(state = state as HomeScreenState.Error)
        is HomeScreenState.Loading -> LoadingScreen()
    }
}

@Composable
fun HomeScreenData(state: HomeScreenState.Data) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Posljednje ažurirano:${state.items.lastUpdate}",
            modifier = Modifier.padding(start = 10.dp, top = 10.dp),
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(10.dp))
        Matches(state.items)
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.Blue)
    }
}

@Composable
fun ErrorScreen(state: HomeScreenState.Error) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedButton(onClick = state.refresh, shape = RoundedCornerShape(4.dp)) {
            Text(state.errorMessage + " Refresh?")
        }
    }
}


@Composable
fun Matches(state: UiData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            state.uiMatches.forEach {
                item {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                            .fillMaxWidth()
                            .padding(top = 5.dp)
                    ) {
                        Text(
                            text = it.league.toString(),
                            modifier = Modifier.padding(start = 10.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    it.match?.forEach {
                        MatchItem(item = it)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

data class UiData(
    val lastUpdate: String,
    val uiMatches: List<UiMatches>
)

data class UiMatches(
    val league: String?,
    val match: ImmutableList<UiMatch>?
)