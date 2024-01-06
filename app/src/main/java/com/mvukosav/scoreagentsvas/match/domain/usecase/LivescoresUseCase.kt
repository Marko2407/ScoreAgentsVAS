package com.mvukosav.scoreagentsvas.match.domain.usecase

import android.util.Log
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Livescores
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class LivescoresUseCase @Inject constructor(private val matchesRepository: MatchesRepository) {

    operator fun invoke(): Flow<Livescores?> =
        matchesRepository.livescoresFlow.distinctUntilChanged { old, new ->
            // Flatten the isFavorite properties for both old and new data
//            val oldFavorites = old?.livescoresItem?.flatMap { item ->
//                item.stage.flatMap { stage ->
//                    stage?.matches?.map { match ->
//                        match?.isFavorite
//                    } ?: emptyList()
//                }
//            }
//
//            val newFavorites = new?.livescoresItem?.flatMap { item ->
//                item.stage.flatMap { stage ->
//                    stage?.matches?.map { match ->
//                        match?.isFavorite
//                    } ?: emptyList()
//                }
//            }

            // Compare the flattened lists, considering all properties except isFavorite
//            Log.d("LOLOLO_DIST", "favorite is the same ?\n ${oldFavorites} \n ${newFavorites}")
//            oldFavorites == newFavorites
            old == new
        }
}
