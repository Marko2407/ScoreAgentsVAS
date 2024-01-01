package com.mvukosav.scoreagentsvas.match.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvukosav.scoreagentsvas.match.domain.usecase.GetMatches
import com.mvukosav.scoreagentsvas.user.domain.usecase.IsUserLoggedIn
import com.mvukosav.scoreagentsvas.user.domain.usecase.LoginUser
import com.mvukosav.scoreagentsvas.user.domain.usecase.LogoutUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
            else _state.emit(HomeScreenState.Data(data))
        }
    }


    fun logoutUser() {
        viewModelScope.launch {
            logout()
        }
    }
}