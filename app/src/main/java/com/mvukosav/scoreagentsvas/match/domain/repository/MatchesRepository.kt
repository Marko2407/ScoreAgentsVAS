package com.mvukosav.scoreagentsvas.match.domain.repository

import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Livescores
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.LivescoresDTH
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetail
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetailDto
import com.mvukosav.scoreagentsvas.match.domain.model.prematches.Match
import kotlinx.coroutines.flow.StateFlow

interface MatchesRepository {

    val matchesFlow: StateFlow<Match?>

    val livescoresFlow: StateFlow<Livescores?>

    val favoriteMatchesMutableFlow: StateFlow<List<Int?>>

    val matchDetailsMutableFlow: StateFlow<MatchDetail?>
    suspend fun getAllPreMatches(): Match?

    suspend fun getLiveScores(): Livescores?

    suspend fun getLeague(): Any?

    suspend fun setAsFavorite(matchId: Int)

    suspend fun getMatchDetails(matchId: Int): MatchDetail?

    fun clearAgent()

   suspend fun changeMatchStatus(matchId: Int)
}