package com.mvukosav.scoreagentsvas.user.domain.usecase

import com.mvukosav.scoreagentsvas.user.domain.repository.UserRepository
import javax.inject.Inject

class LoginUser @Inject constructor(private val userRepository: UserRepository) {

    suspend operator fun invoke(username: String, password: String) =
        userRepository.loginUser(username, password)
}