package com.mvukosav.scoreagentsvas.match.domain.usecase

import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import javax.inject.Inject

class StartMatchDetailsAgent @Inject constructor(private val matchesRepository: MatchesRepository) {

    operator fun invoke(matchId: String) =  matchesRepository.startMatchDetailsAgent(matchId)

}