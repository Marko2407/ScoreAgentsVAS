package com.mvukosav.scoreagentsvas.match.domain.usecase

import com.mvukosav.scoreagentsvas.match.domain.model.prematches.Match
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import javax.inject.Inject

class RefreshMatches @Inject constructor(private val matchesRepository: MatchesRepository) {

    suspend operator fun invoke(): Match? {
        return matchesRepository.getAllPreMatches()
    }
}