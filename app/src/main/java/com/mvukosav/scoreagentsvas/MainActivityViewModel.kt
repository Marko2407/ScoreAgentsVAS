package com.mvukosav.scoreagentsvas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvukosav.scoreagentsvas.user.domain.usecase.IsUserLoggedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val isUserLoggedIn: IsUserLoggedIn) :
    ViewModel() {

    private val _events: MutableStateFlow<MainUIEvent?> = MutableStateFlow(null)
    val events = _events

    init {
        observeIsUserLoggedIn()
    }

    private fun observeIsUserLoggedIn() {
        viewModelScope.launch {
            delay(2000)
            isUserLoggedIn().collectLatest {
                if (it == false) {
                    _events.emit(MainUIEvent.NavigateToLogin)
                } else {
                    _events.emit(MainUIEvent.NavigateToHome)
                }
            }
        }
    }
}