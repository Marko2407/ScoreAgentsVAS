package com.mvukosav.scoreagentsvas.match.domain.model.livescores

data class StageDTH(
    val matches: List<Matche>,
    val stage_id: Int,
    val stage_name: String
)

data class Stage(
    val matches: List<MatchLiveScore?>,
    val stage_id: Int,
    val stage_name: String
)