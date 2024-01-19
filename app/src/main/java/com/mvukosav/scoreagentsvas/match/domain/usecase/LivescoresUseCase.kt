package com.mvukosav.scoreagentsvas.match.domain.usecase

import com.mvukosav.scoreagentsvas.match.domain.model.livescores.CurrentOfferGraphQL
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class LivescoresUseCase @Inject constructor(private val matchesRepository: MatchesRepository) {

    operator fun invoke(): Flow<List<CurrentOfferGraphQL?>?> =
        matchesRepository.livescoresFlow.distinctUntilChanged { old, new -> old == new }
}
