package com.mvukosav.scoreagentsvas.match.domain.usecase

import android.util.Log
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class MatchesIds @Inject constructor(private val matchesRepository: MatchesRepository) {

}
