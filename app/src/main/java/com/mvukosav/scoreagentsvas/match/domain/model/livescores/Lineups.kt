package com.mvukosav.scoreagentsvas.match.domain.model.livescores

data class Lineups(
    val bench: Bench,
    val formation: Formation,
    val lineup_type: String,
    val lineups: LineupsX,
    val sidelined: Sidelined
)