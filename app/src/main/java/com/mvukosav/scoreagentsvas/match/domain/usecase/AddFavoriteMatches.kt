package com.mvukosav.scoreagentsvas.match.domain.usecase

import android.adservices.adid.AdId
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddFavoriteMatches @Inject constructor(private val matchesRepository: MatchesRepository) {

    suspend operator fun invoke(matchId: String) = matchesRepository.setAsFavorite(matchId)
}
