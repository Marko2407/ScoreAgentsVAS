package com.mvukosav.scoreagentsvas.match.domain.repository

import com.mvukosav.scoreagentsvas.match.domain.model.Match
import kotlinx.coroutines.flow.StateFlow

interface MatchesRepository {

    suspend fun getAllPreMatches(): Match?

    suspend fun getLiveScores(): Any?

    suspend fun getLeague(): Any?
}