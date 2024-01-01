package com.mvukosav.scoreagentsvas

sealed class MainUIEvent {
    data object NavigateToLogin : MainUIEvent()
    data object NavigateToHome : MainUIEvent()
}