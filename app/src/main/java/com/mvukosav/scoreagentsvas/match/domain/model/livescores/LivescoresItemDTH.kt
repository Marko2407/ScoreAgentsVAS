package com.mvukosav.scoreagentsvas.match.domain.model.livescores

import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventsUi
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchPreview

data class LivescoresItemDTH(
    val country: Country,
    val is_cup: Boolean,
    val league_id: Int,
    val league_name: String,
    val stage: List<StageDTH>
)

data class LivescoresItem(
    val country: Country,
    val league_id: Int,
    val league_name: String,
    val stage: List<Stage?>
)

data class CurrentOfferGraphQL(
    val date: String?,
    val league_name: String?,
    val matches: List<MatchLiveScoreGraphQL>?
)


data class MatchLiveScoreGraphQL(
    val id: String? = "-1",
    val startTime: String?,
    val leagueName: String?,
    val homeTeam: String?,
    val awayTeam: String?,
    var status: Status? = Status.UNKNOWN,
    val minute: Int? = 0,
    val winner: String?,
    val events: EventsUi?,
    val goals: String? = "0:0",
    val excitementRating: String?,
    val oddsHome: Double? = 0.0,
    val oddsAway: Double? = 0.0,
    val oddsDraw: Double? = 0.0,
    val matchPreview: MatchPreviewGraphQL?,
    var isFavorite: Boolean = false
)

data class MatchPreviewGraphQL(
    val id: String?,
    val previewContent: List<MatchPreviewContentGraph>?
)

data class MatchPreviewContentGraph(
    val id: String? = null,
    val content: String?,
    val name: String?
)