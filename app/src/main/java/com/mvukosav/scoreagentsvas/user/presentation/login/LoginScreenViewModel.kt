package com.mvukosav.scoreagentsvas.user.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvukosav.scoreagentsvas.user.domain.usecase.LoginUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val loginUser: LoginUser,
) : ViewModel() {

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginUser(username, password)
        }
    }
}