package com.mvukosav.scoreagentsvas.match.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mvukosav.scoreagentsvas.match.presentation.home.HomeScreenState
import com.mvukosav.scoreagentsvas.match.presentation.home.HomeScreenViewModel

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    when (state) {
        is HomeScreenState.Data -> HomeScreenData(state = state as HomeScreenState.Data)
        is HomeScreenState.Error -> {}
        is HomeScreenState.Loading -> LoadingScreen()
    }
}

@Composable
fun HomeScreenData(state: HomeScreenState.Data) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Prematches: ${state.items.count}", fontSize = 24.sp, color = Color.Black)
        LazyColumn {
            state.items.results.forEach {
                item {
                    Text(text = it.league_name)
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}