package com.mvukosav.scoreagentsvas.match.domain.model.livescores

data class Matche(
    val date: String,
    val events: List<Event>,
    val goals: Goals,
    val has_extra_time: Boolean,
    val has_penalties: Boolean,
    val id: Int,
    val lineups: Lineups,
    val match_preview: MatchPreview,
    val minute: Int,
    val odds: Odds,
    val stadium: Stadium,
    var status: String,
    val teams: Teams,
    val time: String,
    val winner: String
)

data class MatchLiveScore(
    val date: String?,
    val goals: Goals?,
    val has_extra_time: Boolean?,
    val has_penalties: Boolean?,
    val id: Int?,
    val match_preview: MatchPreview?,
    val minute: Int?,
    val odds: Odds?,
    val stadium: Stadium?,
    var status: String?,
    val teams: Teams?,
    val time: String?,
    val winner: String?,
    var isFavorite: Boolean = false
)

enum class Status(val nameStatus: String) {
    LIVE("live"), FINISHED("finished"), HALFTIME("halftime"), UNKNOWN("unknown"), PREMATCH("prematch");

    companion object {
        fun fromName(name: String?): Status {
            return entries.firstOrNull { it.nameStatus.equals(name, ignoreCase = true) } ?: UNKNOWN
        }
    }
}