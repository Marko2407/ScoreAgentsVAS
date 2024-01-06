package com.mvukosav.scoreagentsvas.match.domain.model.matchdetails

import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Status

data class Event(
    val assist_player: Any,
    val event_minute: String,
    val event_type: String,
    val player: Player,
    val player_in: PlayerIn,
    val player_out: PlayerOut,
    val team: String
)

data class EventUi(
    val name: EventsEnum,
    val number: String = "-"
)

enum class EventsEnum(val nameEvent: String) {
    RED_CARD("red_card"), YELLOW_CARD("yellow_card"), SUBS("substitution"), PENAL("penalty_goal"), CORNERS("corners"), UNKNOWN(""), HOME(nameEvent = "home"), AWAY("away");

    companion object {
        fun fromName(name: String): EventsEnum {
            return EventsEnum.entries.firstOrNull { it.nameEvent.equals(name, ignoreCase = true) }
                ?: EventsEnum.UNKNOWN
        }
    }
}

data class EventsUi(
    val home: List<EventUi>,
    val away: List<EventUi>
)