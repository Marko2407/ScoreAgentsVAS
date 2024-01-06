package com.mvukosav.scoreagentsvas.match.domain.usecase

import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import javax.inject.Inject

class GetMatchDetails @Inject constructor(private val matchesRepository: MatchesRepository) {

    suspend operator fun invoke(matchId: Int) = matchesRepository.getMatchDetails(matchId = matchId)
}
