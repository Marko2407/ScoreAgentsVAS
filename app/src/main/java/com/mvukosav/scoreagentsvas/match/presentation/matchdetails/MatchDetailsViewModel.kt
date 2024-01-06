package com.mvukosav.scoreagentsvas.match.presentation.matchdetails

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetail
import com.mvukosav.scoreagentsvas.match.domain.usecase.AddFavoriteMatches
import com.mvukosav.scoreagentsvas.match.domain.usecase.ChangeMatchStatus
import com.mvukosav.scoreagentsvas.match.domain.usecase.GetMatchDetails
import com.mvukosav.scoreagentsvas.match.domain.usecase.MatchDetailsFlowUseCase
import com.mvukosav.scoreagentsvas.match.ui.UiOdds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addFavoriteMatches: AddFavoriteMatches,
    private val getMatchDetails: GetMatchDetails,
    private val matchDetails: MatchDetailsFlowUseCase,
    private val changeMatchStatus: ChangeMatchStatus
) : ViewModel() {

    private val _state: MutableStateFlow<MatchDetailsScreenState> =
        MutableStateFlow(MatchDetailsScreenState.Loading)
    var state: StateFlow<MatchDetailsScreenState> = _state

    private var currentMatchId: Int? = null

    init {
        savedStateHandle.get<Int>("matchId")?.let { matchId ->
            if (matchId != -1) {
                //get match and save with this id
                Log.d("LOLOLO_Match", "MATCH ID : $matchId")
                currentMatchId = matchId
                viewModelScope.launch {
                    _state.emit(MatchDetailsScreenState.Loading)
                    getMatchDetails(matchId)
                    observeMatchDetails()
                }
            }
        }
    }

    private fun observeMatchDetails() {
        viewModelScope.launch {
            matchDetails().collectLatest {
                Log.d("LOLOLO", "Collector matchDetails $it")
                renderData(it)
            }
        }
    }

    private suspend fun renderData(data: MatchDetail?) {
        if (data == null) {
            _state.emit(MatchDetailsScreenState.Error("Unable to details", ::refresh))
        } else {
            _state.emit(MatchDetailsScreenState.Data(dataToUiMapper(data)))
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.emit(MatchDetailsScreenState.Loading)
            getMatchDetails(currentMatchId ?: -1)
        }
    }

    private fun dataToUiMapper(data: MatchDetail): UiMatchData {
        return UiMatchData(
            id = data.id,
            startTime = data.startTime,
            league = data.league,
            homeTeam = data.homeTeam,
            awayTeam = data.awayTeam,
            status = data.status,
            minute = data.minute,
            winner = data.winner,
            goals = data.goals,
            event = data.events,
            odds = UiOdds(data.oddsHome, mutableStateOf(false)),
            excitementRating = data.excitementRating,
            isFavorite = data.isFavorite,
            matchPreview = data.matchPreview,
            onFavoriteClick = ::onFavoriteClick,
            changeMatchStatus = ::changeMatchStatus
        )
    }

    private fun changeMatchStatus() {
        viewModelScope.launch {
            Log.d("MARKO", "tu si $currentMatchId")
            changeMatchStatus(matchId = currentMatchId ?: -1)
        }
    }

    private fun onFavoriteClick() {
        viewModelScope.launch {
            addFavoriteMatches(matchId = currentMatchId ?: -1)
        }
    }
}