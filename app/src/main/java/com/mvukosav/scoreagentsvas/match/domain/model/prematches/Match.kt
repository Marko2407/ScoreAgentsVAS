package com.mvukosav.scoreagentsvas.match.domain.model.prematches

import androidx.compose.runtime.Immutable

@Immutable
data class Match(
    val count: Int,
    val results: List<MatchResponse>,
    val updated_at: String
)