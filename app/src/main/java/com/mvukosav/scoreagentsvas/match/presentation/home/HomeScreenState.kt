package com.mvukosav.scoreagentsvas.match.presentation.home

import com.mvukosav.scoreagentsvas.match.domain.model.Match

sealed class HomeScreenState {

    data object Loading : HomeScreenState()
    data class Data(val items: Match) : HomeScreenState()
    data class Error(val errorMessage: String) : HomeScreenState()
}