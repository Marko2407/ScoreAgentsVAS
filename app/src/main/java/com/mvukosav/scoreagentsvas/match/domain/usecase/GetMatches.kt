package com.mvukosav.scoreagentsvas.match.domain.usecase

import android.util.Log
import com.mvukosav.scoreagentsvas.match.domain.model.Match
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import com.mvukosav.scoreagentsvas.user.domain.model.User
import com.mvukosav.scoreagentsvas.user.domain.repository.UserRepository
import javax.inject.Inject

class GetMatches @Inject constructor(private val matchesRepository: MatchesRepository) {

    suspend operator fun invoke(): Match? {
        val matches = matchesRepository.getAllPreMatches()
        Log.d("LOLOLO", "matcheees $matches")
        return matches
    }
}