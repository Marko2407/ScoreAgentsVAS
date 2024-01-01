package com.mvukosav.scoreagentsvas.user.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mvukosav.scoreagentsvas.user.domain.model.User
import com.mvukosav.scoreagentsvas.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Dummy repository for login
 */
class UserRepositoryImpl @Inject constructor(private val context: Context) :
    UserRepository {

    private val isUserLoggedInStateFlow = MutableStateFlow(false)
    override val isUserLoggedIn: StateFlow<Boolean?> = isUserLoggedInStateFlow

    private val userSharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(USER_SHARED_PREFERENCES, Context.MODE_PRIVATE)
    }

    init {
        isUserLoggedInStateFlow.value = wasUserLoggedIn()
    }

    private fun wasUserLoggedIn(): Boolean {
        return userSharedPreferences.getBoolean(IS_USER_LOGGED_IN, false)
    }

    private fun setUserLoggedInSP(value: Boolean) {
        val sp = userSharedPreferences.edit()
        sp.putBoolean(IS_USER_LOGGED_IN, value)
        sp.apply()
    }

    // Dummy data
    override fun getUser(): User? {
        return if (isUserLoggedInStateFlow.value) {
            User(id = 0, username = USERNAME, password = PASSWORD)
        } else {
            null
        }
    }

    override suspend fun loginUser(username: String, password: String) {
        if (username == USERNAME && password == PASSWORD) {
            setUserLoggedInSP(true)
            isUserLoggedInStateFlow.emit(true)
        } else {
            setUserLoggedInSP(false)
            isUserLoggedInStateFlow.emit(false)
        }
    }

    override suspend fun logout() {
        setUserLoggedInSP(false)
        isUserLoggedInStateFlow.emit(false)
    }


    companion object {
        private const val USER_SHARED_PREFERENCES = "USER_SHARED_PREFERENCES"
        const val IS_USER_LOGGED_IN = "IS_USER_LOGGED_IN"
        const val USERNAME = "test"
        const val PASSWORD = "test"
    }

}