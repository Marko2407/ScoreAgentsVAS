package com.mvukosav.scoreagentsvas.match.presentation.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvukosav.scoreagentsvas.match.domain.model.Match
import com.mvukosav.scoreagentsvas.match.domain.usecase.GetMatches
import com.mvukosav.scoreagentsvas.match.ui.UiData
import com.mvukosav.scoreagentsvas.match.ui.UiMatch
import com.mvukosav.scoreagentsvas.match.ui.UiMatches
import com.mvukosav.scoreagentsvas.match.ui.UiOdds
import com.mvukosav.scoreagentsvas.user.domain.usecase.LogoutUser
import com.mvukosav.scoreagentsvas.utils.formatToNewPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val logout: LogoutUser,
    private val getMatches: GetMatches
) : ViewModel() {
    private val _state: MutableStateFlow<HomeScreenState> =
        MutableStateFlow(HomeScreenState.Loading)
    var state: StateFlow<HomeScreenState> = _state

    init {
        _state.value = HomeScreenState.Loading
        getData()
    }

    fun getData() {
        viewModelScope.launch {
            _state.emit(HomeScreenState.Loading)
            val data = getMatches()
            if (data == null || data.count == 0) _state.emit(HomeScreenState.Error("Unable to fetch matches"))
            else _state.emit(HomeScreenState.Data(dataToUiDataMapper(data)))
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
                        startTime = "${matchPreview.date} ${matchPreview.time}",
                        fixtures = "${matchPreview.teams.home.name} - ${matchPreview.teams.away.name}",
                        publicRate = "${matchPreview.excitement_rating}",
                        country = matchResponse.country.name,
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

    fun logoutUser() {
        viewModelScope.launch {
            logout()
        }
    }
}