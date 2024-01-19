package com.mvukosav.scoreagentsvas.match.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvukosav.scoreagentsvas.R
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Status

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchItem(item: UiMatch) {
    ElevatedCard(
        modifier = Modifier
            .height(110.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RectangleShape,
        onClick = item.onMatchClicked
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(text = item.startTime, fontSize = 10.sp)
                    if (item.status != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        LiveBadge(item.status)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        Modifier
                            .size(20.dp)
                            .padding(end = 5.dp),
                        tint = if (item.isFavorite) Color.Red else Color.White
                    )
                    Text(text = "Popularnost: ${item.publicRate}", fontSize = 10.sp)
                }

            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                Text(text = item.fixtures, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            }

            Column(modifier = Modifier.padding(top = 5.dp)) {
                Text(text = "Osnovna ponuda", fontSize = 10.sp)
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    OddsButton(item.odds1, "1", {}, Modifier.weight(1f))
                    OddsButton(item.odds2, "X", {}, Modifier.weight(1f))
                    OddsButton(item.odds3, "2", {}, Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
fun OddsButton(
    odds: UiOdds,
    fixture: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        if (odds.isSelected.value) Color.DarkGray else Color.Transparent

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(30.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Text(text = fixture, color = Color.LightGray)
            Text(
                text = odds.odds.toString(), color = if (!odds.isSelected.value) {
                    Color.DarkGray
                } else {
                    Color.White
                }
            )
        }
    }
}

@Composable
fun LiveBadge(text: Status?, textSize: TextUnit = 12.sp, modifier: Modifier = Modifier) {
    val background = when (text) {
        Status.LIVE -> Color.Yellow
        Status.FINISHED -> colorResource(id = R.color.teal_200)
        Status.HALFTIME -> Color.Cyan
        Status.UNKNOWN -> Color.LightGray
        Status.PREMATCH -> Color.Green
        null -> Color.Transparent
    }
    LegTextTag(
        text?.nameStatus?.toUpperCase() ?: "",
        textColor = Color.Black,
        modifier
            .background(background, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp),
        fontSize = textSize
    )
}

@Composable
fun LegTextTag(label: String, textColor: Color, modifier: Modifier, fontSize: TextUnit = 12.sp) {
    Text(
        fontSize = fontSize,
        letterSpacing = 0.48.sp,
        lineHeight = 16.sp,
        modifier = modifier,
        color = textColor,
        text = label,
        fontWeight = FontWeight.Bold
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MatchItemPreview() {
    Column {
        MatchItem(
            item = UiMatch(
                status = Status.LIVE,
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
                isFavorite = true
            )
        )
        MatchItem(
            item = UiMatch(
                status = Status.PREMATCH,
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
            )
        )
        MatchItem(
            item = UiMatch(
                status = Status.HALFTIME,
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
            )
        )
        MatchItem(
            item = UiMatch(
                status = Status.UNKNOWN,
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
            )
        )
        MatchItem(
            item = UiMatch(
                status = Status.FINISHED,
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate ="9.5",
            )
        )
        MatchItem(
            item = UiMatch(
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
            )
        )
    }
}

data class UiMatch(
    val id: String? = null,
    val startTime: String,
    val fixtures: String,
    val publicRate: String,
    val status: Status? = null,
    val isFavorite: Boolean = false,
    val odds1: UiOdds = UiOdds(2.22, mutableStateOf(false)),
    val odds2: UiOdds = UiOdds(4.22, mutableStateOf(true)),
    val odds3: UiOdds = UiOdds(2.20, mutableStateOf(false)),
    val onMatchClicked: () -> Unit = {}
)

data class UiOdds(
    val odds: Double,
    val isSelected: MutableState<Boolean>
)