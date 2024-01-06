package com.mvukosav.scoreagentsvas.match.domain.model.livescores

data class Goals(
    val away_et_goals: Int,
    val away_ft_goals: Int,
    val away_ht_goals: Int,
    val away_pen_goals: Int,
    val home_et_goals: Int,
    val home_ft_goals: Int,
    val home_ht_goals: Int,
    val home_pen_goals: Int
)
// -1 is null!