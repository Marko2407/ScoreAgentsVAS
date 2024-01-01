package com.mvukosav.scoreagentsvas.match.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MatchItem(item: UiMatch) {
    ElevatedCard(
        modifier = Modifier
            .height(110.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RectangleShape
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(text = item.startTime, fontSize = 10.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                ) {
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
                Text(text = item.fixtures, fontSize = 14.sp)
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MatchItemPreview() {
    Column {
        MatchItem(
            item = UiMatch(
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
                country = "Spain",
            )
        )
        MatchItem(
            item = UiMatch(
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
                country = "Spain",
            )
        )
        MatchItem(
            item = UiMatch(
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
                country = "Spain",
            )
        )
        MatchItem(
            item = UiMatch(
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
                country = "Spain",
            )
        )
        MatchItem(
            item = UiMatch(
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
                country = "Spain",
            )
        )
        MatchItem(
            item = UiMatch(
                startTime = "01.02.2024 03:45",
                fixtures = "Barcelona - Real Madrid",
                publicRate = "9.5",
                country = "Spain",
            )
        )
    }

}

data class UiMatch(
    val startTime: String,
    val fixtures: String,
    val publicRate: String,
    val country: String,
    val odds1: UiOdds = UiOdds(2.22, mutableStateOf(false)),
    val odds2: UiOdds = UiOdds(4.22, mutableStateOf(true)),
    val odds3: UiOdds = UiOdds(2.20, mutableStateOf(false)),
)

data class UiOdds(
    val odds: Double,
    val isSelected: MutableState<Boolean>
)