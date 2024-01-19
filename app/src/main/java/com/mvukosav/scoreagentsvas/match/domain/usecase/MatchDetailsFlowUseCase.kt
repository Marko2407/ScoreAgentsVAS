package com.mvukosav.scoreagentsvas.match.domain.usecase

import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchLiveScoreGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetail
import com.mvukosav.scoreagentsvas.match.domain.model.prematches.Match
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class MatchDetailsFlowUseCase @Inject constructor(private val matchesRepository: MatchesRepository) {

    operator fun invoke(): Flow<MatchLiveScoreGraphQL?> =
        matchesRepository.matchDetailsMutableFlow.distinctUntilChanged { old, new -> old == new }
}
