package com.mvukosav.scoreagentsvas.match.presentation.home

sealed class HomeScreenUiEvent {
    data class NavigateToMatchDetails(val matchId: Int): HomeScreenUiEvent()
}