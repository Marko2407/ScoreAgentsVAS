package com.mvukosav.scoreagentsvas.match.presentation.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.toUpperCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvukosav.scoreagentsvas.MainUIEvent
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Livescores
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Stage
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Status
import com.mvukosav.scoreagentsvas.match.domain.model.prematches.Match
import com.mvukosav.scoreagentsvas.match.domain.usecase.FavoriteMatches
import com.mvukosav.scoreagentsvas.match.domain.usecase.LivescoresUseCase
import com.mvukosav.scoreagentsvas.match.domain.usecase.Matches
import com.mvukosav.scoreagentsvas.match.domain.usecase.RefreshLivescores
import com.mvukosav.scoreagentsvas.match.domain.usecase.RefreshMatches
import com.mvukosav.scoreagentsvas.match.domain.usecase.StopAgent
import com.mvukosav.scoreagentsvas.match.ui.UiData
import com.mvukosav.scoreagentsvas.match.ui.UiMatch
import com.mvukosav.scoreagentsvas.match.ui.UiMatches
import com.mvukosav.scoreagentsvas.match.ui.UiOdds
import com.mvukosav.scoreagentsvas.user.domain.usecase.LogoutUser
import com.mvukosav.scoreagentsvas.utils.formatToNewPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val logout: LogoutUser,
    private val refreshMatches: RefreshMatches,
    private val refreshLivescores: RefreshLivescores,
    private val stopAgent: StopAgent,
    private val matches: Matches,
    private val livescores: LivescoresUseCase,
    private val favoriteMatches: FavoriteMatches
) : ViewModel() {
    private val _state: MutableStateFlow<HomeScreenState> =
        MutableStateFlow(HomeScreenState.Loading)
    var state: StateFlow<HomeScreenState> = _state

    private val _events: MutableStateFlow<HomeScreenUiEvent?> = MutableStateFlow(null)
    val events = _events

    init {
        viewModelScope.launch {
            _state.emit(HomeScreenState.Loading)
            renderData(refreshLivescores())
            observePrematch()
            observeLiveScore()
            observeFavorites()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAgent()
    }

    private fun observePrematch() {
        viewModelScope.launch {
            matches().collectLatest {
                Log.d("LOLOLO", "Collector $it")

            }
        }
    }

    private fun observeLiveScore() {
        viewModelScope.launch {
            livescores().collectLatest {
                renderData(it)
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteMatches().collectLatest {
                Log.d("LOLOLO", "Collector for favorites $it")
            }
        }
    }

    private suspend fun renderData(data: Livescores?) {
        if (data == null || data.livescoresItem.size == 0) {
            _state.emit(HomeScreenState.Error("Unable to fetch matches", ::refresh))
        } else {
            _state.emit(HomeScreenState.Data(dataToUiDataMapper(data)))
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.emit(HomeScreenState.Loading)
            refreshLivescores()
        }
    }


    private fun dataToUiDataMapper(data: Match): UiData {
        val listOfUiMatches = data.results.map { matchResponse ->
            UiMatches(
                league = matchResponse.league_name,
                match = matchResponse.match_previews.filter { preview ->
                    preview.excitement_rating > 8.0
                }.map { matchPreview ->
                    UiMatch(
                        id = matchPreview.id,
                        startTime = "${matchPreview.date} ${matchPreview.time}",
                        fixtures = "${matchPreview.teams.home.name} - ${matchPreview.teams.away.name}",
                        publicRate = matchPreview.excitement_rating,
                        odds1 = UiOdds(2.2, mutableStateOf(false)),
                        odds2 = UiOdds(4.2, mutableStateOf(false)),
                        odds3 = UiOdds(2.0, mutableStateOf(false))
                    )
                }.toImmutableList()
            )
        }.filter { uiMatches ->
            uiMatches.match.isNotEmpty()
        }

        return UiData(data.updated_at.formatToNewPattern(), listOfUiMatches)
    }

    private fun dataToUiDataMapper(data: Livescores): UiData {
        val listOfUiMatches = data.livescoresItem.map {
            UiMatches(
                league = it.league_name,
                match = stagesToUiMatch(it.stage)
            )
        }
        val lastUpdate = Calendar.getInstance().time.toLocaleString()
        return UiData(lastUpdate, listOfUiMatches)
    }

    private fun stagesToUiMatch(stage: List<Stage?>): ImmutableList<UiMatch> {
        val uiMatches: MutableList<UiMatch> = mutableListOf()
        stage.forEach {
            it?.matches?.forEach { match ->
                val item = UiMatch(
                    id = match?.id,
                    startTime = "${match?.date} ${match?.time}",
                    fixtures = "${match?.teams?.home?.name} - ${match?.teams?.away?.name}",
                    publicRate = match?.match_preview?.excitement_rating ?: 0.0,
                    status = Status.fromName(match?.status ?: "unknown"),
                    isFavorite = match?.isFavorite ?: false,
                    odds1 = UiOdds(
                        odds = match?.odds?.match_winner?.home ?: 0.0,
                        isSelected = mutableStateOf(false)
                    ),
                    odds2 = UiOdds(
                        odds = match?.odds?.match_winner?.draw ?: 0.0,
                        isSelected = mutableStateOf(false)
                    ),
                    odds3 = UiOdds(
                        odds = match?.odds?.match_winner?.away ?: 0.0,
                        isSelected = mutableStateOf(false)
                    ),
                    onMatchClicked = {
                        if (match?.id != null) onMatchClicked(match.id)
                        else onMatchClicked(0)
                    }
                )
                uiMatches.add(item)
            }
        }

        return uiMatches.toImmutableList()
    }

    private fun onMatchClicked(matchId: Int) {
        viewModelScope.launch {
            Log.d("LOLOLO_events", "event $matchId")
            _events.emit(HomeScreenUiEvent.NavigateToMatchDetails(matchId))
        }
    }


    fun logoutUser() {
        viewModelScope.launch {
            logout()
        }
    }
}