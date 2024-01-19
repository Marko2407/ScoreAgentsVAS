package com.mvukosav.scoreagentsvas.match.data.repository

import com.mvukosav.scoreagentsvas.CurrentOfferQuery
import com.mvukosav.scoreagentsvas.MatchDetailsQuery
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchPreviewContentGraph
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventUi
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventsEnum

fun mapHomeEventMatch(homeEvents: List<MatchDetailsQuery.HomeEvent?>?): List<EventUi>? {
    return homeEvents?.map { event ->
        EventUi(
            name = EventsEnum.fromName(event?.name?.name),
            number = event?.number ?: "0"
        )
    }
}

fun mapAwayEventMatch(awayEvents: List<MatchDetailsQuery.AwayEvent?>?): List<EventUi>? {
    return awayEvents?.map { event ->
        EventUi(
            name = EventsEnum.fromName(event?.name?.name),
            number = event?.number ?: "0"
        )
    }
}

fun mapPreviewContentMatch(previewContent: List<MatchDetailsQuery.PreviewContent?>?): List<MatchPreviewContentGraph>? {
    return previewContent?.map { preview ->
        MatchPreviewContentGraph(
            id = preview?._id,
            content = preview?.content,
            name = preview?.name
        )
    }
}

fun mapHomeEvent(homeEvents: List<CurrentOfferQuery.HomeEvent?>?): List<EventUi>? {
    return homeEvents?.map { event ->
        EventUi(
            name = EventsEnum.fromName(event?.name?.name),
            number = event?.number ?: "0"
        )
    }
}

fun mapAwayEvent(awayEvents: List<CurrentOfferQuery.AwayEvent?>?): List<EventUi>? {
    return awayEvents?.map { event ->
        EventUi(
            name = EventsEnum.fromName(event?.name?.name),
            number = event?.number ?: "0"
        )
    }
}

fun mapPreviewContent(previewContent: List<CurrentOfferQuery.PreviewContent?>?): List<MatchPreviewContentGraph>? {
    return previewContent?.map { preview ->
        MatchPreviewContentGraph(
            id = preview?._id,
            content = preview?.content,
            name = preview?.name
        )
    }
}