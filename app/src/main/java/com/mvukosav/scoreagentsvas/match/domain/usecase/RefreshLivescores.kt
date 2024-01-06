package com.mvukosav.scoreagentsvas.match.domain.usecase

import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Livescores
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import javax.inject.Inject

class RefreshLivescores @Inject constructor(private val matchesRepository: MatchesRepository) {

    suspend operator fun invoke(): Livescores? {
        val matches = matchesRepository.getLiveScores()
        return matches
    }
}