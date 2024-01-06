package com.mvukosav.scoreagentsvas.match.presentation.home

import com.mvukosav.scoreagentsvas.match.ui.UiData

sealed class HomeScreenState {

    data object Loading : HomeScreenState()
    data class Data(val items: UiData) : HomeScreenState()
    data class Error(val errorMessage: String, val refresh: () -> Unit) : HomeScreenState()
}