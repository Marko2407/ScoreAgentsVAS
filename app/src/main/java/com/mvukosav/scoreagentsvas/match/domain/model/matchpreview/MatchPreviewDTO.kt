package com.mvukosav.scoreagentsvas.match.domain.model.matchpreview

data class MatchPreviewDTO(
    val country: Country,
    val date: String,
    val id: Int,
    val league: League,
    val match_data: MatchData,
    val preview_content: List<PreviewContent>,
    val stage: Stage,
    val teams: Teams,
    val time: String,
    val word_count: Int
)

class MatchPreviewListDTO : ArrayList<MatchPreviewDTO>()
