package com.mvukosav.scoreagentsvas.match.domain.usecase

import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import javax.inject.Inject

class StopAgent @Inject constructor(private val matchesRepository: MatchesRepository) {

    operator fun invoke() =  matchesRepository.clearAgent()

}