package com.mvukosav.scoreagentsvas.user.domain.usecase

import com.mvukosav.scoreagentsvas.user.domain.model.User
import com.mvukosav.scoreagentsvas.user.domain.repository.UserRepository
import javax.inject.Inject

class GetUser @Inject constructor(private val userRepository: UserRepository) {

    operator fun invoke(): User? = userRepository.getUser()
}