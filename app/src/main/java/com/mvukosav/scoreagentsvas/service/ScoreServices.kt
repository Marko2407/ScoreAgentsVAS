package com.mvukosav.scoreagentsvas.service

import com.mvukosav.scoreagentsvas.match.domain.model.Match
import retrofit2.http.GET
import retrofit2.http.Query

interface ScoreServices {

    @GET("match-previews-upcoming")
    suspend fun getAllPreMatches(): Match?

    @GET("livescores")
    suspend fun getLiveScores(): Any

    @GET("league")
    suspend fun getLeague(): Any

    @GET("country")
    suspend fun getCountries(): Any

    @GET("match-preview/")
    suspend fun getMatchById(@Query("match_id") match_id: String): Any

    @GET("matches/")
    suspend fun getMatchesByLeagueId(@Query("league_id") league_id: String): Any

    @GET("standing/")
    suspend fun getScoreboardByLeagueId(@Query("league_id") league_id: String): Any

    @GET("season/")
    suspend fun getSeasonByLeagueId(@Query("league_id") league_id: String): Any

    @GET("player/")
    suspend fun getPlayerById(@Query("player_id") player_id: String): Any

    @GET("team/")
    suspend fun getTeamById(@Query("team_id") team_id: String): Any

    @GET("head-to-head/")
    suspend fun getHeadToHead(
        @Query("team_1_id") team_1_id: String,
        @Query("team_2_id") team_2_id: String
    ): Any
}