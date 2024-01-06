package com.mvukosav.scoreagentsvas.match.domain.model.livescores

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