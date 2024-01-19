package com.mvukosav.scoreagentsvas.match.data.repository

import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Livescores
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.LivescoresDTH
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.LivescoresItem
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchLiveScore
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Stage
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Status
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.Event
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventUi
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventsEnum
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventsUi
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetail
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetailDto
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchPreviewContent
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.PreviewContent
import com.mvukosav.scoreagentsvas.match.domain.model.matchpreview.MatchPreviewDTO
import javax.inject.Inject
import kotlin.random.Random

fun mapEvents(events: List<Event>): EventsUi {
    val yellowHomeCard =
        events.filter { it.event_type == EventsEnum.YELLOW_CARD.nameEvent && it.team == EventsEnum.HOME.nameEvent }
    val yellowAwayCard =
        events.filter { it.event_type == EventsEnum.YELLOW_CARD.nameEvent && it.team == EventsEnum.AWAY.nameEvent }

    val redHomeCard =
        events.filter { it.event_type == EventsEnum.RED_CARD.nameEvent || it.event_type == "yellow_red_card" && it.team == EventsEnum.HOME.nameEvent }
    val redAwayCard =
        events.filter { it.event_type == EventsEnum.RED_CARD.nameEvent || it.event_type == "yellow_red_card" && it.team == EventsEnum.AWAY.nameEvent }

    val penaltyHomeGoals =
        events.filter { it.event_type == EventsEnum.PENAL.nameEvent && it.team == EventsEnum.HOME.nameEvent }
    val penaltyAwayGoals =
        events.filter { it.event_type == EventsEnum.PENAL.nameEvent && it.team == EventsEnum.AWAY.nameEvent }

    val subsHome =
        events.filter { it.event_type == EventsEnum.SUBS.nameEvent && it.team == EventsEnum.HOME.nameEvent }
    val subsAway =
        events.filter { it.event_type == EventsEnum.SUBS.nameEvent && it.team == EventsEnum.AWAY.nameEvent }

    val cornersHome = Random.nextInt(0, 10).toString()
    val cornersAway = Random.nextInt(0, 10).toString()

    return EventsUi(
        home = listOf(
            EventUi(EventsEnum.PENAL, penaltyHomeGoals.size.toString()),
            EventUi(EventsEnum.CORNERS, cornersHome),
            EventUi(EventsEnum.YELLOW_CARD, yellowHomeCard.size.toString()),
            EventUi(EventsEnum.RED_CARD, redHomeCard.size.toString()),
            EventUi(EventsEnum.SUBS, subsHome.size.toString())
        ),
        away = listOf(
            EventUi(EventsEnum.PENAL, penaltyAwayGoals.size.toString()),
            EventUi(EventsEnum.CORNERS, cornersAway),
            EventUi(EventsEnum.YELLOW_CARD, yellowAwayCard.size.toString()),
            EventUi(EventsEnum.RED_CARD, redAwayCard.size.toString()),
            EventUi(EventsEnum.SUBS, subsAway.size.toString())
        ),
    )
}

fun mapMatchPreview(matchPreviewDTO: MatchPreviewDTO?): MatchPreviewContent? {
    return if (matchPreviewDTO != null) {
        MatchPreviewContent(
            id = matchPreviewDTO.id,
            preview_content = matchPreviewDTO.preview_content.map {
                PreviewContent(content = it.content, name = it.name)
            })
    } else null
}



class DataToDomainLiveScores @Inject constructor() {
    operator fun invoke(data: LivescoresDTH?, listOfFavorite: List<Int>): Livescores {
        val liveScoresItem = data?.map { liveScoresItem ->
            LivescoresItem(
                country = liveScoresItem.country,
                league_id = liveScoresItem.league_id,
                league_name = liveScoresItem.league_name,
                stage = liveScoresItem.stage.map { stage ->
                    Stage(
                        matches = stage.matches.filter { it.match_preview.excitement_rating > 6.0 }
                            .map { match ->
                                MatchLiveScore(
                                    date = match.date,
                                    goals = match.goals,
                                    has_extra_time = match.has_extra_time,
                                    has_penalties = match.has_penalties,
                                    id = match.id,
                                    match_preview = match.match_preview,
                                    minute = match.minute,
                                    odds = match.odds,
                                    stadium = match.stadium,
                                    status = match.status,
                                    teams = match.teams,
                                    time = match.time,
                                    winner = match.winner,
                                    isFavorite = listOfFavorite.contains(match.id)
                                )
                            },
                        stage_id = stage.stage_id,
                        stage_name = stage.stage_name
                    )
                }
            )
        }
        return Livescores(liveScoresItem?.toMutableList() ?: mutableListOf())
    }
}

class DataToDomainMatchDetails @Inject constructor() {
    operator fun invoke(
        data: MatchDetailDto?,
        listOfFavorite: List<Int>,
        matchPreviewDTO: MatchPreviewDTO?
    ): MatchDetail? {
        return if (data != null) {
            MatchDetail(
                id = data.id,
                startTime = "${data.date} ${data.time}",
                league = data.league,
                homeTeam = data.teams.home.name,
                awayTeam = data.teams.away.name,
                status = Status.fromName(data.status),
                minute = data.minute,
                winner = data.winner,
                goals = "${data.goals.home_ft_goals}:${data.goals.away_ft_goals}",
                events = mapEvents(data.events),
                oddsHome = data.odds.match_winner.home,
                oddsAway = data.odds.match_winner.away,
                oddsDraw = data.odds.match_winner.draw,
                excitementRating = data.match_preview.excitement_rating.toString(),
                isFavorite = listOfFavorite.contains(data.id),
                matchPreview = mapMatchPreview(matchPreviewDTO)
            )
        } else null
    }
}