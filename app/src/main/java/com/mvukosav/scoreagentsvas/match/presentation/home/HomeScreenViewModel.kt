package com.mvukosav.scoreagentsvas.match.presentation.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.CurrentOfferGraphQL
import com.mvukosav.scoreagentsvas.match.domain.usecase.FavoriteMatches
import com.mvukosav.scoreagentsvas.match.domain.usecase.LivescoresUseCase
import com.mvukosav.scoreagentsvas.match.domain.usecase.RefreshLivescores
import com.mvukosav.scoreagentsvas.match.domain.usecase.StopAgent
import com.mvukosav.scoreagentsvas.match.ui.UiData
import com.mvukosav.scoreagentsvas.match.ui.UiMatch
import com.mvukosav.scoreagentsvas.match.ui.UiMatches
import com.mvukosav.scoreagentsvas.match.ui.UiOdds
import com.mvukosav.scoreagentsvas.user.domain.usecase.LogoutUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.immutableListOf
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
    private val refreshLivescores: RefreshLivescores,
    private val stopAgent: StopAgent,
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
            observeLiveScore()
            observeFavorites()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAgent()
    }

    private fun observeLiveScore() {
        viewModelScope.launch {
            livescores().collectLatest {
                Log.d("LOLOLO_LIVE", "CollectorLIVE $it")
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

    private suspend fun renderData(data: List<CurrentOfferGraphQL?>?) {
        if (data.isNullOrEmpty()) {
            _state.emit(HomeScreenState.Error("Unable to fetch matches", ::refresh))
        } else {
            val mappedData = dataToUiDataMapper(data)
            if (mappedData.uiMatches.isNotEmpty()) {
                _state.emit(HomeScreenState.Data(mappedData))
            }else{
                _state.emit(HomeScreenState.Error("Offer not available at the moment", ::refresh))
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.emit(HomeScreenState.Loading)
            refreshLivescores()
        }
    }

    private fun dataToUiDataMapper(data: List<CurrentOfferGraphQL?>?): UiData {
        val lastUpdate = Calendar.getInstance().time.toLocaleString()
        return UiData(lastUpdate = lastUpdate, data?.map { offer ->
            UiMatches(league = offer?.league_name.toString(), match = offer?.matches?.map { match ->
                UiMatch(
                    id = match.id,
                    startTime = match.startTime ?: "",
                    fixtures = "${match.homeTeam} - ${match.awayTeam}",
                    publicRate = match.excitementRating ?: "0.0",
                    status = match.status,
                    isFavorite = match.isFavorite,
                    odds1 = UiOdds(match.oddsHome ?: 0.0, mutableStateOf(false)),
                    odds2 = UiOdds(match.oddsDraw ?: 0.0, mutableStateOf(false)),
                    odds3 = UiOdds(match.oddsAway ?: 0.0, mutableStateOf(false)),
                    onMatchClicked = { onMatchClicked(match.id!!) }
                )
            }?.toImmutableList() ?: immutableListOf())
        } ?: listOf())

    }


    private fun onMatchClicked(matchId: String) {
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