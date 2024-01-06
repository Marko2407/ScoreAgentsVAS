package com.mvukosav.scoreagentsvas.match.domain.model.livescores

class LivescoresDTH : ArrayList<LivescoresItemDTH>()

data class Livescores(
    val livescoresItem: MutableList<LivescoresItem>
)