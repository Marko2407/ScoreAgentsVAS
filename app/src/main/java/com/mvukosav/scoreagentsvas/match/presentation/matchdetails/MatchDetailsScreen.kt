package com.mvukosav.scoreagentsvas.match.presentation.matchdetails

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.sharp.KeyboardArrowLeft
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mvukosav.scoreagentsvas.R
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchPreviewContentGraph
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchPreviewGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Status
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventUi
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventsEnum
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventsUi
import com.mvukosav.scoreagentsvas.match.ui.LiveBadge
import com.mvukosav.scoreagentsvas.match.ui.LoadingScreen
import com.mvukosav.scoreagentsvas.match.ui.UiOdds

@Composable
fun MatchDetailsScreen(
    navController: NavController,
    viewModel: MatchDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState(MatchDetailsScreenState.Loading)

    when (state) {
        is MatchDetailsScreenState.Data -> {
            MatchDetailsDataScreen(state as MatchDetailsScreenState.Data, navController)
        }

        is MatchDetailsScreenState.Loading -> LoadingScreen()


        is MatchDetailsScreenState.Error -> ErrorScreen(state as MatchDetailsScreenState.Error)
    }
    BackHandler {
        navController.navigate("home") {
            popUpTo("home")
        }
    }
}

@Composable
fun ErrorScreen(state: MatchDetailsScreenState.Error) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedButton(onClick = state.refresh, shape = RoundedCornerShape(4.dp)) {
            Text(state.errorMessage + " Refresh?")
        }
    }
}

@Composable
fun MatchDetailsDataScreen(
    state: MatchDetailsScreenState.Data,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.gray))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.grass_green)),
            verticalArrangement = Arrangement.Top
        ) {
            MatchHeader(state.items, navController)
        }
        MatchContent(state.items.matchPreview)
    }


}

@Composable
fun MatchHeader(
    items: UiMatchData,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = 10.dp, bottom = 5.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Sharp.KeyboardArrowLeft,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 5.dp)
                    .clickable {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Text(
                        text = "${items.homeTeam} - ${items.awayTeam}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 10.dp),
                        color = Color.White
                    )
                    Text(
                        text = items.goals.toString(),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = items.league.toString(),
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            Row(
                Modifier
                    .fillMaxWidth(1f)
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { items.onFavoriteClick() },
                    modifier = Modifier
                        .size(25.dp)
                ) {
                    Icon(
                        imageVector = if (items.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (items.isFavorite) colorResource(id = R.color.red_fav) else {
                            Color.Unspecified
                        },

                        )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(top = 10.dp, end = 10.dp, start = 10.dp)
        ) {
            LiveBadge(
                text = items.status,
                12.sp,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .clickable { items.changeMatchStatus() })
            if (items.status == Status.LIVE || items.status == Status.HALFTIME) {
                Text(text = "${items.minute}. min", fontSize = 14.sp, color = Color.White)
            }
        }
    }

    Row(
        modifier = Modifier
            .background(colorResource(id = R.color.white20))
            .fillMaxWidth()
            .height(20.dp)
            .padding(end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Row(
            modifier = Modifier
                .width(90.dp)
        ) {
            AnalyticsText(icon = painterResource(id = R.drawable.ic_sb_penalties))
            AnalyticsText(icon = painterResource(id = R.drawable.ic_sb_corner))
            AnalyticsText(icon = painterResource(id = R.drawable.ic_sb_yellowcard))
            AnalyticsText(icon = painterResource(id = R.drawable.ic_sb_redcard))
            AnalyticsText(icon = painterResource(id = R.drawable.ic_sb_changeover))
        }

    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, top = 5.dp, bottom = 8.dp)) {
            Text(
                text = items.homeTeam.toString(),
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = items.awayTeam.toString(),
                fontSize = 14.sp,
                color = Color.White
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp, top = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .width(90.dp)
                ) {
                    items.event?.home?.forEach {
                        AnalyticsText(it.number)
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .width(90.dp)
                ) {
                    items.event?.away?.forEach {
                        AnalyticsText(it.number)
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchContent(matchPreview: MatchPreviewGraphQL?) {
    if (matchPreview != null) {
        Text(
            text = "Match preview:",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 2.dp),
            fontSize = 14.sp
        )
        LazyColumn(
            Modifier
                .padding(start = 20.dp, end = 20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            contentPadding = PaddingValues(0.dp),
        ) {
            matchPreview.previewContent?.forEach {
                item {
                    var maxLines by remember { mutableIntStateOf(9) }
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        onClick = { maxLines = if (maxLines == 9) 20 else 9 }
                    ) {
                        Row {
                            Text(
                                text = "${it.name?.toUpperCase()}:",
                                modifier = Modifier.padding(5.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = it.content.toString(),
                                modifier = Modifier.padding(top = 5.dp, end = 10.dp, bottom = 5.dp),
                                fontSize = 12.sp,
                                maxLines = maxLines,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    } else {
        Column(
            Modifier
                .padding(top = 150.dp, start = 20.dp, end = 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.Gray
            )
            Text(text = "No commentators for this match", fontSize = 20.sp)
        }
    }
}

@Composable
fun AnalyticsText(text: String? = null, icon: Painter? = null) {
    if (text != null) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.width(10.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(10.dp))
    } else {
        Image(
            painter = icon ?: painterResource(id = R.drawable.ic_sb_yellowcard),
            contentDescription = null,
            modifier = Modifier
                .width(15.dp)
                .padding(top = 3.dp),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MatchPreview() {
    MatchDetailsDataScreen(
        MatchDetailsScreenState.Data(UiMatchDataPreview.data),
        rememberNavController()
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MatchPreviewWithoutPreviewContent() {
    MatchDetailsDataScreen(
        MatchDetailsScreenState.Data(UiMatchDataPreview.dataWithoutPreviewContent),
        rememberNavController(),
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MatchPreviewLoading() {
    LoadingScreen()
}


data class UiMatchData(
    val id: String? = "",
    val startTime: String? = "",
    val league: String? = "",
    val homeTeam: String? = "",
    val awayTeam: String? = "",
    val status: Status = Status.UNKNOWN,
    val minute: Int? = 0,
    val winner: String? = "unknown",
    val goals: String? = "0:0",
    val event: EventsUi? = null,
    val odds: UiOdds,
    val excitementRating: String? = "0.0",
    val isFavorite: Boolean = false,
    val matchPreview: MatchPreviewGraphQL? = null,
    val onFavoriteClick: () -> Unit = {},
    val changeMatchStatus: () -> Unit = {}
)

object UiMatchDataPreview {
    val data = UiMatchData(
        id = "1",
        startTime = "05/01/2024 23:50",
        league = "Coppa italia",
        homeTeam = "Real Madrid",
        awayTeam = "Barcelona",
        status = Status.LIVE,
        minute = 13,
        winner = "tdn",
        event = EventsUi(
            id = null,
            listOf(
                EventUi(EventsEnum.PENAL, "1"),
                EventUi(EventsEnum.CORNERS, "6"),
                EventUi(EventsEnum.YELLOW_CARD, "2"),
                EventUi(EventsEnum.RED_CARD, "0"),
                EventUi(EventsEnum.SUBS, "0")
            ),
            listOf(
                EventUi(EventsEnum.PENAL, "0"),
                EventUi(EventsEnum.CORNERS, "3"),
                EventUi(EventsEnum.YELLOW_CARD, "1"),
                EventUi(EventsEnum.RED_CARD, "0"),
                EventUi(EventsEnum.SUBS, "0")
            ),
        ),
        goals = "2:1",
        excitementRating = "",
        odds = UiOdds(0.0, mutableStateOf(true)),
        matchPreview = MatchPreviewGraphQL(
            "999",
            listOf(
                MatchPreviewContentGraph(
                    id = null,
                    "This Saturday, January 6, fans will gather",
                    "P2"
                ),
                MatchPreviewContentGraph(
                    id = null,
                    "This Saturday, January 6, fans will gather",
                    "P2"
                ),
                MatchPreviewContentGraph(
                    id = null,
                    "This Saturday, January 6, fans will gather at the Stade Du 1Er Novembre 1954 stadium in Tizi-Ouzou to watch an exciting match between Kabylie and ASO Chlef in the Algeria Ligue 1 league. Kickoff is set for 15:45 (UTC). With a forecast of moderate to heavy rain and thunder, the weather may add an extra element to the game as the temperature peaks at 49 degrees Fahrenheit. These two teams last faced off in the Division-1 on March 03, 2023, with the match ending in a 0-0 draw. Don't miss out on what is sure to be a fierce and competitive game between these skilled teams.",
                    "P1"
                ),
                MatchPreviewContentGraph(
                    id = null,
                    "This Saturday, January 6, fans will gather",
                    "P2"
                ),
                MatchPreviewContentGraph(
                    id = null,
                    "This Saturday, January 6, fans will gather",
                    "P2"
                )
            )
        )
    )

    val dataWithoutPreviewContent = UiMatchData(
        id = "1",
        startTime = "05/01/2024 23:50",
        league = "Coppa italia",
        homeTeam = "Real Madrid",
        awayTeam = "Barcelona",
        status = Status.HALFTIME,
        minute = 13,
        winner = "tdn",
        event = EventsUi(
            id = null,
            listOf(
                EventUi(EventsEnum.PENAL, "1"),
                EventUi(EventsEnum.CORNERS, "6"),
                EventUi(EventsEnum.YELLOW_CARD, "2"),
                EventUi(EventsEnum.RED_CARD, "0"),
                EventUi(EventsEnum.SUBS, "0")
            ),
            listOf(
                EventUi(EventsEnum.PENAL, "0"),
                EventUi(EventsEnum.CORNERS, "3"),
                EventUi(EventsEnum.YELLOW_CARD, "1"),
                EventUi(EventsEnum.RED_CARD, "0"),
                EventUi(EventsEnum.SUBS, "0")
            ),
        ),
        goals = "2:1",
        excitementRating = "",
        odds = UiOdds(0.0, mutableStateOf(true)),
        matchPreview = null,
    )
}