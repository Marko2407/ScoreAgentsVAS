package com.mvukosav.scoreagentsvas.match.domain.model.livescores

data class Odds(
    val handicap: Handicap,
    val last_modified_timestamp: Int,
    val match_winner: MatchWinner,
    val over_under: OverUnder
)