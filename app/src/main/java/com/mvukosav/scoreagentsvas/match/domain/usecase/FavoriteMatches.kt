package com.mvukosav.scoreagentsvas.match.domain.usecase

import android.util.Log
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class FavoriteMatches @Inject constructor(private val matchesRepository: MatchesRepository) {

    operator fun invoke(): Flow<List<Int?>?> =
        matchesRepository.favoriteMatchesMutableFlow.distinctUntilChanged { old, new ->
            Log.d("LOLOLO_INVOKE_FAVO", "old $old, new $new")
            old == new
        }
}
