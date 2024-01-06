package com.mvukosav.scoreagentsvas.match.domain.model.matchdetails

import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Status

data class MatchDetailDto(
    val country: Country,
    val date: String,
    val events: List<Event>,
    val goals: Goals,
    val has_extra_time: Boolean,
    val has_penalties: Boolean,
    val id: Int,
    val league: League,
    val lineups: Lineups,
    val match_preview: MatchPreview,
    val minute: Int,
    val odds: Odds,
    val stadium: Stadium,
    val stage: Stage,
    var status: String,
    val teams: Teams,
    val time: String,
    val winner: String
)

data class MatchDetail(
    val id: Int,
    val startTime: String,
    val league: League,
    val homeTeam: String,
    val awayTeam: String,
    val status: Status,
    val minute: Int,
    val winner: String,
    val events: EventsUi,
    val goals: String,
    val excitementRating: String,
    val oddsHome: Double,
    val oddsAway: Double,
    val oddsDraw: Double,
    val matchPreview: MatchPreviewContent?,
    val isFavorite: Boolean = true,
)

class MatchDetailsListDTO : ArrayList<MatchDetailDto>()