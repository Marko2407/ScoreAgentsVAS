package com.mvukosav.scoreagentsvas.match.domain.model.prematches

import androidx.compose.runtime.Immutable

@Immutable
data class MatchResponse(
    val country: Country,
    val is_cup: Boolean,
    val league_id: Int,
    val league_name: String,
    val match_previews: List<MatchPreview>
)