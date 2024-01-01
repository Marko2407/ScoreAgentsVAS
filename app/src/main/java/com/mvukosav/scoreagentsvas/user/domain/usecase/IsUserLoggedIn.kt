package com.mvukosav.scoreagentsvas.user.domain.usecase

import com.mvukosav.scoreagentsvas.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsUserLoggedIn @Inject constructor(private val userRepository: UserRepository) {

    operator fun invoke(): Flow<Boolean?> {
        return userRepository.isUserLoggedIn
    }
}