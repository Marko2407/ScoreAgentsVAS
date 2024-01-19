package com.mvukosav.scoreagentsvas.match.domain.repository

import com.mvukosav.scoreagentsvas.match.domain.model.livescores.CurrentOfferGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Livescores
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.LivescoresDTH
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchLiveScoreGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetail
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetailDto
import com.mvukosav.scoreagentsvas.match.domain.model.prematches.Match
import kotlinx.coroutines.flow.StateFlow

interface MatchesRepository {

    val livescoresFlow: StateFlow<List<CurrentOfferGraphQL?>?>

    val favoriteMatchesMutableFlow: StateFlow<List<String?>>

    val matchDetailsMutableFlow: StateFlow<MatchLiveScoreGraphQL?>
    suspend fun getLiveScores(): List<CurrentOfferGraphQL?>?

    suspend fun getLeague(): Any?

    suspend fun setAsFavorite(matchId: String)

    suspend fun getMatchDetails(matchId: String): MatchLiveScoreGraphQL?

    fun clearAgent()

    fun startMatchDetailsAgent(matchId: String)

    fun stopMatchDetailsAgent()

}