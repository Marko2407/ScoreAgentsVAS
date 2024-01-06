package com.mvukosav.scoreagentsvas.match.presentation.matchdetails

abstract class MatchDetailsScreenState {

    data object Loading : MatchDetailsScreenState()
    data class Data(val items: UiMatchData) : MatchDetailsScreenState()
    data class Error(val errorMessage: String, val refresh: () -> Unit) : MatchDetailsScreenState()
}

