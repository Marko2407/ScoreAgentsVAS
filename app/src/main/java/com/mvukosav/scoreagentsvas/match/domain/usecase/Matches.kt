package com.mvukosav.scoreagentsvas.match.domain.usecase

import com.mvukosav.scoreagentsvas.match.domain.model.prematches.Match
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class Matches @Inject constructor(private val matchesRepository: MatchesRepository) {

    operator fun invoke(): Flow<Match?> = matchesRepository.matchesFlow.distinctUntilChanged { old, new -> old?.updated_at != new?.updated_at }
}
