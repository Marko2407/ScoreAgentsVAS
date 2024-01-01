package com.mvukosav.scoreagentsvas.user.domain.repository

import com.mvukosav.scoreagentsvas.user.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val isUserLoggedIn: StateFlow<Boolean?>

    fun getUser(): User?

    suspend fun loginUser(username: String, password: String)

    suspend fun logout()

}