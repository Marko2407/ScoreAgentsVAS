package com.mvukosav.scoreagentsvas.match.domain.model.matchpreview

data class MatchData(
    val excitement_rating: Double,
    val prediction: Prediction,
    val weather: Weather
)