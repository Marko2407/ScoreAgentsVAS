package com.mvukosav.scoreagentsvas.match.domain.model.livescores

data class Event(
    val assist_player: AssistPlayer,
    val event_minute: String,
    val event_type: String,
    val player: Player,
    val player_in: PlayerIn,
    val player_out: PlayerOut,
    val team: String
)