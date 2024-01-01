package com.mvukosav.scoreagentsvas.match.domain.model

data class MatchPreview(
    val date: String,
    val excitement_rating: Double,
    val id: Int,
    val teams: Teams,
    val time: String,
    val word_count: Int
)