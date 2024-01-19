package com.mvukosav.scoreagentsvas.match.data.repository

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.google.gson.Gson
import com.mvukosav.scoreagentsvas.AddToFavoriteMutation
import com.mvukosav.scoreagentsvas.CurrentOfferQuery
import com.mvukosav.scoreagentsvas.GetFavoriteMatchesQuery
import com.mvukosav.scoreagentsvas.MatchDetailsQuery
import com.mvukosav.scoreagentsvas.R
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.addToFavorite
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.livescores
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.repositoryScope
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.CurrentOfferGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Livescores
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.LivescoresDTH
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.LivescoresItem
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchLiveScore
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchLiveScoreGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchPreviewContentGraph
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchPreviewGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Stage
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Status
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.Event
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventUi
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventsEnum
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventsUi
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetail
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetailDto
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchDetailsListDTO
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.MatchPreviewContent
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.PreviewContent
import com.mvukosav.scoreagentsvas.match.domain.model.matchpreview.MatchPreviewDTO
import com.mvukosav.scoreagentsvas.match.domain.model.matchpreview.MatchPreviewListDTO
import com.mvukosav.scoreagentsvas.match.domain.model.prematches.Match
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import com.mvukosav.scoreagentsvas.match.domain.usecase.AddFavoriteMatches
import com.mvukosav.scoreagentsvas.match.domain.usecase.RefreshLivescores
import com.mvukosav.scoreagentsvas.service.AgentsNotificationService
import com.mvukosav.scoreagentsvas.utils.Notification
import com.mvukosav.scoreagentsvas.utils.getTime
import com.mvukosav.scoreagentsvas.utils.jsonToObject
import com.mvukosav.scoreagentsvas.utils.network.GraphQlManager
import com.mvukosav.scoreagentsvas.utils.objectToJson
import com.mvukosav.scoreagentsvas.utils.parseTimeFormattedString
import jade.core.AID
import jade.core.Agent
import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.core.behaviours.CyclicBehaviour
import jade.core.behaviours.TickerBehaviour
import jade.lang.acl.ACLMessage
import jade.wrapper.ContainerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class MatchesRepositoryImpl @Inject constructor(
    private val context: Context,
    agentsNotificationService: AgentsNotificationService,
) : MatchesRepository {

    private val _livescoresMutableFlow: MutableStateFlow<List<CurrentOfferGraphQL?>?> =
        MutableStateFlow(listOf(null))
    override val livescoresFlow: StateFlow<List<CurrentOfferGraphQL?>?> = _livescoresMutableFlow

    private val _favoriteMatchesMutableFlow: MutableStateFlow<MutableList<String>> =
        MutableStateFlow(mutableListOf())
    override val favoriteMatchesMutableFlow: StateFlow<List<String?>> = _favoriteMatchesMutableFlow

    private val _matchDetailsMutableFlow: MutableStateFlow<MatchLiveScoreGraphQL?> =
        MutableStateFlow(null)
    override val matchDetailsMutableFlow: StateFlow<MatchLiveScoreGraphQL?> =
        _matchDetailsMutableFlow

    private var matchDetailsDto: MatchDetailsListDTO? = null
    private var matchDetailsPreviewDto: MatchPreviewListDTO? = null

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var container: ContainerController? = null


    init {
        repositoryScope.launch(Dispatchers.Default) {
            _livescoresMutableFlow.value = getLiveScores()
        }
        SharedMatchesRepository.livescores = _livescoresMutableFlow
        SharedMatchesRepository.getLivescore = RefreshLivescores(this)
        SharedMatchesRepository.notificationService = agentsNotificationService
        SharedMatchesRepository.repositoryScope = repositoryScope
        SharedMatchesRepository.addToFavorite = AddFavoriteMatches(this)
        SharedMatchesRepository.favoriteMatchesMutableFlow = _favoriteMatchesMutableFlow
        startAgents()
    }

    override suspend fun getLiveScores(): List<CurrentOfferGraphQL?>? {
        val responseFlow: Flow<List<CurrentOfferGraphQL?>?> =
            GraphQlManager.apolloClient().query(CurrentOfferQuery())
                .toFlow()
                .map { response ->
                    dataToDomainCurrentOffer(response.data?.currentOffer).also {
                        Log.d("LOLOLO_LIVE_REPO", it.toString())
                        if (it?.isEmpty() == true) {
                            _livescoresMutableFlow.emit(null)
                        } else {
                            _livescoresMutableFlow.emit(it)
                        }
                    }
                }
                .catch { e ->
                    emit(null)
                    _livescoresMutableFlow.emit(null)
                }
        val favList = mutableListOf<String>()

        val r =
            GraphQlManager.apolloClient().query((GetFavoriteMatchesQuery("test"))).toFlow().first()
        if (r.data?.favoriteMatches?.match?.isNotEmpty() == true) {

            r.data?.favoriteMatches?.match?.map {
                favList.add(it?._id.toString())
            }
            _favoriteMatchesMutableFlow.emit(favList)
        }


        var result: List<CurrentOfferGraphQL?>? = null
        responseFlow.collect {
            result = it
            _livescoresMutableFlow.emit(it)
        }

        return result
    }


    private fun dataToDomainCurrentOffer(currentOffer: List<CurrentOfferQuery.CurrentOffer?>?): List<CurrentOfferGraphQL?>? {
        return currentOffer?.map { current ->
            CurrentOfferGraphQL(
                date = current?.date,
                league_name = current?.leagueName,
                matches = current?.matches?.map { match ->
                    MatchLiveScoreGraphQL(
                        id = match?._id,
                        startTime = match?.startTime,
                        leagueName = match?.league,
                        homeTeam = match?.homeTeam,
                        awayTeam = match?.awayTeam,
                        status = Status.fromName(match?.status?.name),
                        minute = match?.minute,
                        winner = match?.winner,
                        goals = match?.goals,
                        excitementRating = match?.excitementRating,
                        oddsDraw = match?.oddsDraw,
                        oddsHome = match?.oddsHome,
                        oddsAway = match?.oddsAway,
                        isFavorite = match?.isFavorite ?: false,
                        events = EventsUi(
                            id = match?.events?._id,
                            home = mapHomeEvent(match?.events?.homeEvents),
                            away = mapAwayEvent(match?.events?.awayEvents),
                        ),
                        matchPreview = MatchPreviewGraphQL(
                            id = match?.matchPreview?._id,
                            previewContent = mapPreviewContent(match?.matchPreview?.previewContent)
                        )
                    )
                }
            )
        }
    }

    private fun mapHomeEvent(homeEvents: List<CurrentOfferQuery.HomeEvent?>?): List<EventUi>? {
        return homeEvents?.map { event ->
            EventUi(
                name = EventsEnum.fromName(event?.name?.name),
                number = event?.number ?: "0"
            )
        }
    }

    private fun mapAwayEvent(awayEvents: List<CurrentOfferQuery.AwayEvent?>?): List<EventUi>? {
        return awayEvents?.map { event ->
            EventUi(
                name = EventsEnum.fromName(event?.name?.name),
                number = event?.number ?: "0"
            )
        }
    }

    private fun mapPreviewContent(previewContent: List<CurrentOfferQuery.PreviewContent?>?): List<MatchPreviewContentGraph>? {
        return previewContent?.map { preview ->
            MatchPreviewContentGraph(
                id = preview?._id,
                content = preview?.content,
                name = preview?.name
            )
        }
    }

    override suspend fun getLeague(): Any? {
        TODO("Not yet implemented")
    }

    override suspend fun setAsFavorite(matchId: String) {
        val oldFavorites = _favoriteMatchesMutableFlow.value.toMutableList()

        val currentMatchDetails = _matchDetailsMutableFlow.value
        if (currentMatchDetails != null) {
            val updatedMatchDetails =
                currentMatchDetails.copy(isFavorite = !currentMatchDetails.isFavorite)
            _matchDetailsMutableFlow.emit(updatedMatchDetails)
        }
        try {
            val responseFlow = GraphQlManager.apolloClient()
                .mutation(
                    AddToFavoriteMutation(
                        username = Optional.present("test"),
                        matchId = Optional.present(matchId)
                    )
                )
                .toFlow().first()

            if (!responseFlow.hasErrors()) {
                val currentLiveScore = getLiveScores()
                _livescoresMutableFlow.emit(currentLiveScore)
                val newMatchDetails =
                    getMatchDetails(matchId = matchId)
                _matchDetailsMutableFlow.emit(newMatchDetails)

                if (oldFavorites.contains(matchId)) {
                    oldFavorites.remove(matchId)
                } else {
                    oldFavorites.add(matchId)
                }
                _favoriteMatchesMutableFlow.emit(oldFavorites)
            } else {
                _matchDetailsMutableFlow.emit(currentMatchDetails)
            }
        } catch (e: Exception) {
            _matchDetailsMutableFlow.emit(currentMatchDetails)
            _favoriteMatchesMutableFlow.emit(oldFavorites)
        }
    }

    override suspend fun getMatchDetails(matchId: String): MatchLiveScoreGraphQL? {
        return try {

            val response =
                GraphQlManager.apolloClient().query(MatchDetailsQuery(matchId = matchId))
                    .toFlow()
                    .first()
            val result = response.data?.matchDetails?.let { match ->
                MatchLiveScoreGraphQL(
                    id = match._id,
                    startTime = match.startTime,
                    leagueName = match.league,
                    homeTeam = match.homeTeam,
                    awayTeam = match.awayTeam,
                    status = Status.fromName(match.status.name),
                    minute = match.minute,
                    winner = match.winner,
                    goals = match.goals,
                    excitementRating = match.excitementRating,
                    oddsDraw = match.oddsDraw,
                    oddsHome = match.oddsHome,
                    oddsAway = match.oddsAway,
                    isFavorite = match.isFavorite ?: false,
                    events = EventsUi(
                        id = match.events?._id,
                        home = mapHomeEventMatch(match.events?.homeEvents),
                        away = mapAwayEventMatch(match.events?.awayEvents),
                    ),
                    matchPreview = MatchPreviewGraphQL(
                        id = match.matchPreview?._id,
                        previewContent = mapPreviewContentMatch(match.matchPreview?.previewContent)
                    )
                )
            }
            _matchDetailsMutableFlow.emit(result)
            return result
        } catch (e: Exception) {
            null
        }
    }

    private fun mapHomeEventMatch(homeEvents: List<MatchDetailsQuery.HomeEvent?>?): List<EventUi>? {
        return homeEvents?.map { event ->
            EventUi(
                name = EventsEnum.fromName(event?.name?.name),
                number = event?.number ?: "0"
            )
        }
    }

    private fun mapAwayEventMatch(awayEvents: List<MatchDetailsQuery.AwayEvent?>?): List<EventUi>? {
        return awayEvents?.map { event ->
            EventUi(
                name = EventsEnum.fromName(event?.name?.name),
                number = event?.number ?: "0"
            )
        }
    }

    private fun mapPreviewContentMatch(previewContent: List<MatchDetailsQuery.PreviewContent?>?): List<MatchPreviewContentGraph>? {
        return previewContent?.map { preview ->
            MatchPreviewContentGraph(
                id = preview?._id,
                content = preview?.content,
                name = preview?.name
            )
        }
    }

    override suspend fun changeMatchStatus(matchId: String) {
    }


    override fun clearAgent() {
        stopAgent()
    }

    private fun stopAgent() {
        repositoryScope.launch(Dispatchers.IO) {
            try {
                container?.kill()
                repositoryScope.cancel()
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private fun startAgents() {
        repositoryScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val runtime = Runtime.instance()
                    val writableDir = context.filesDir.path
                    val profile = ProfileImpl().apply {
                        setParameter(Profile.FILE_DIR, writableDir)
                    }

                    container = runtime.createMainContainer(profile)

                    val agents = listOf(
                        Pair(
                            "LiveScoresDataFetchAgent",
                            LiveScoresDataFetchAgent::class.java.name
                        ),// agent za dohvacanje livescore
                        Pair(
                            "NotificationsAgent",
                            NotificationsAgent::class.java.name
                        ), // agent za notifikacije
                        Pair(
                            "FavoriteMatchObserverAgent",
                            FavoriteMatchObserverAgent::class.java.name
                        )  // agent za favorite matches
                    )

                    // Create and start each agent
                    agents.forEach { (agentName, agentClassName) ->
                        val agentController = container?.createNewAgent(
                            agentName,
                            agentClassName,
                            null
                        )
                        agentController?.start()
                    }

                } catch (e: Exception) {
                    Log.e("JADE", "Exception in agent setup", e)
                }
            }
        }
    }
}

class LiveScoresDataFetchAgent : Agent() {
    override fun setup() {
        addBehaviour(object : TickerBehaviour(this, 60000) {
            override fun onTick() {
                repositoryScope.launch {
                    try {
                        val matches = SharedMatchesRepository.getLivescore()
                        livescores.emit(matches)
                        Log.d("LOLOLOLO_AGENT", matches.toString())
                        sendMessageToPrematchesDataFetchAgent("Bok refreshao sam LIVESCORE ")
                    } catch (e: Exception) {
                        Log.e("LiveScoresDataFetchAgent", "Error fetching livescores", e)
                    }
                }
            }
        })

        addBehaviour(object : CyclicBehaviour(this) {
            override fun action() {
                val receivedMsg = receive()
                if (receivedMsg != null) {
                    onMessageReceived(receivedMsg)
                } else {
                    block()
                }
            }
        })
    }

    override fun takeDown() {
        println("Agent is being destroyed")
    }

    private fun onMessageReceived(message: ACLMessage) {
        println("Received message: ${message.content}")
    }

    private fun sendMessageToPrematchesDataFetchAgent(content: String) {
        val message = ACLMessage(ACLMessage.INFORM)
        message.addReceiver(AID("PrematchesDataFetchAgent", AID.ISLOCALNAME))
        message.content = content
        send(message)
    }
}

class FavoriteMatchObserverAgent : Agent() {
    val favoriteList = mutableListOf<String>()
    private var notificationCurrentList = mutableListOf<NotificationFavoriteMatch>()
    private var notificationBeginningList = mutableListOf<FavoriteMatch>()
    override fun setup() {
        addBehaviour(object : TickerBehaviour(this, 5000) {
            override fun onTick() {
                repositoryScope.launch {
                    try {
                        val newlist = SharedMatchesRepository.favoriteMatchesMutableFlow.value
                        Log.d("LOLOLO_AGENT_NOTIF", "list $newlist")
                        if (!favoriteList.containsAll(newlist)) {
                            matchNotifyObserver()
                            favoriteList.addAll(newlist)
                        }
                        Log.d("LOLOLO_AGENT_NOTIF", "favoriteList $favoriteList")
                    } catch (e: Exception) {
                        Log.e("LiveScoresDataFetchAgent", "Error fetching livescores", e)
                    }
                }
            }
        })

        addBehaviour(object : TickerBehaviour(this, 20000) {
            override fun onTick() {
                matchNotifyObserver()
            }
        })
    }

    private fun matchNotifyObserver() {
        repositoryScope.launch {
            try {
                val matches = findMatchesById()

                notificationCurrentList = notificationCurrentList.map { notificationFavoriteMatch ->
                    val updatedMatch =
                        matches.find { it.matchId == notificationFavoriteMatch.match.matchId }
                    if (updatedMatch != null) {
                        notificationFavoriteMatch.copy(match = updatedMatch)
                    } else {
                        notificationFavoriteMatch
                    }
                }.toMutableList()

                if (matches.isEmpty()) return@launch
                val finishedMatch = matches.filter { it.status == Status.FINISHED }
                val startedMatch =
                    matches.filter { getTime(it.startTime) != "" }

                finishedMatch.forEach {
                    val n =
                        notificationCurrentList.filter { n -> it.matchId == n.match.matchId }
                    if (n.isEmpty()) {
                        val winner = if (it.winner?.contains("home") == true) {
                            "za " + it.home
                        } else if (it.winner?.contains("away") == true) {
                            "za " + it.away
                        } else {
                            ""
                        }

                        val notification = Notification(
                            id = it.matchId,
                            title = "Utakmica ${it.home} - ${it.away} je zavrsila",
                            content = "Rezultat je ${it.goals} $winner"
                        )
                        val message = objectToJson(notification)
                        sendMessageToNotificationAgent(message)
                        notificationCurrentList.add(
                            NotificationFavoriteMatch(
                                it,
                                getTime(it.startTime)
                            )
                        )
                        addToFavorite(it.matchId)
                    }
                }
                if (startedMatch.isNotEmpty()) {
                    startedMatch.forEach {
                        val n =
                            notificationCurrentList.filter { n -> it.matchId == n.match.matchId }
                        if (n.isEmpty()) {
                            val notification = Notification(
                                id = it.matchId,
                                title = "Utakmica ${it.home} - ${it.away}",
                                content = "Pocinje za ${getTime(it.startTime)}"
                            )
                            sendMessageToNotificationAgent(objectToJson(notification))

                            notificationCurrentList.add(
                                NotificationFavoriteMatch(
                                    it,
                                    getTime(it.startTime)
                                )
                            )
                        } else {
                            //notifikacija je vec poslana
                            Log.d("LOLOLO_N", "n je $n")
                            val remainingToMatchTime =
                                parseTimeFormattedString(getTime(n[0].match.startTime)) // time until matc
                            val notificationTime =
                                parseTimeFormattedString(n[0].notificationTime)  // time until match started

                            if (remainingToMatchTime.second in 1..10 && notificationTime.second > 10) {
                                val notification = Notification(
                                    id = it.matchId,
                                    title = "Utakmica ${it.home} - ${it.away}",
                                    content = "Pocinje za manje od ${getTime(it.startTime)}"
                                )
                                sendMessageToNotificationAgent(objectToJson(notification))
                                n[0].notificationTime = getTime(n[0].match.startTime)

                            } else if (remainingToMatchTime.second == 0) {
                                if (!notificationBeginningList.contains(it)) {
                                    val notification = Notification(
                                        id = it.matchId,
                                        title = "Utakmica ${it.home} - ${it.away}",
                                        content = "Utakmica ce poceti za koji trenutak"
                                    )
                                    val message = objectToJson(notification)
                                    sendMessageToNotificationAgent(message)
                                    notificationBeginningList.add(it)
                                }
                                n[0].notificationTime = getTime(n[0].match.startTime)
                            } else {

                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LiveScoresDataFetchAgent", "Error fetching livescores", e)
            }
        }
    }

    override fun takeDown() {
        println("Agent is being destroyed")
    }

    private fun onMessageReceived(message: ACLMessage) {
        println("Received message: ${message.content}")
    }

    private fun sendMessageToNotificationAgent(content: String) {
        val message = ACLMessage(ACLMessage.INFORM)
        Log.d("LOLOLO_ANOMSG", content)
        message.addReceiver(AID("NotificationsAgent", AID.ISLOCALNAME))
        message.content = content
        send(message)
    }

    private fun findMatchesById(): List<FavoriteMatch> {
        val ids = SharedMatchesRepository.favoriteMatchesMutableFlow.value
        val listOfMatches = mutableListOf<FavoriteMatch>()

        livescores.value?.forEach { m ->
            m?.matches?.forEach { match ->
                if (ids.contains(match.id)) {
                    listOfMatches.add(
                        FavoriteMatch(
                            matchId = match.id ?: "0",
                            startTime = match.startTime ?: "",
                            home = match.homeTeam ?: "",
                            away = match.awayTeam ?: "",
                            status = match.status ?: Status.UNKNOWN,
                            winner = match.winner,
                            goals = match.goals ?: "0:0"
                        )
                    )
                }
            }

        }
        return listOfMatches
    }
}

// Agent za notifikacije
class NotificationsAgent : Agent() {
    val notificationList = mutableListOf<NotificationFavoriteMatch>()

    override fun setup() {
        Log.d("MARKOAGENT", " SLOZEN je $this")
        addBehaviour(object : CyclicBehaviour(this) {
            override fun action() {
                val receivedMsg = receive()
                Log.d("LOLOLO_ANO", "poruka $receivedMsg")
                if (receivedMsg != null) {
                    Log.d("LOLOLO_ANO", "ime ${receivedMsg.sender.name} ")
                    if (receivedMsg.sender.name.contains("FavoriteMatchObserverAgent")) {
                        val data = jsonToObject(receivedMsg.content)
                        SharedMatchesRepository.notificationService.showNotification(
                            data.title,
                            data.content,
                            data.id
                        )
                    }

                } else {
                    block()
                }
            }
        })
    }


    override fun takeDown() {
        println("Agent is being destroyed")
    }
}

object SharedMatchesRepository {
    lateinit var repositoryScope: CoroutineScope
    lateinit var matches: MutableStateFlow<Match?>

    lateinit var getLivescore: RefreshLivescores
    lateinit var livescores: MutableStateFlow<List<CurrentOfferGraphQL?>?>

    lateinit var addToFavorite: AddFavoriteMatches
    lateinit var notificationService: AgentsNotificationService
    lateinit var favoriteMatchesMutableFlow: MutableStateFlow<MutableList<String>>
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

    private fun mapEvents(events: List<Event>): EventsUi {
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

    private fun mapMatchPreview(matchPreviewDTO: MatchPreviewDTO?): MatchPreviewContent? {
        return if (matchPreviewDTO != null) {
            MatchPreviewContent(
                id = matchPreviewDTO.id,
                preview_content = matchPreviewDTO.preview_content.map {
                    PreviewContent(content = it.content, name = it.name)
                })
        } else null
    }
}

/**
Agent za provjeru favorit matcheva - Pocetak, kraj tekme, ukoliko se desi kraj onda mora izbrisati iz liste favorit match (done), salje poruku za notifikacije
Agent za obavijesti. Prima obavijesti u obliku naslova i opisa te poziva prikaz obavijesti (done)
Primjer:           SharedMatchesRepository.notificationService.showNotification(
"Utakmica uskoro pocinje",
"03.01.2024 18:00 Real Madrid - Barcelona"
)

Agent za pracenje promjena tecajeva odds? onda treba i bazu mjenjat.
Agent koji ce primati poruke i aktivirati druge agente?

svi agenti zive koliko i aplikacija


za napravit:  prikaz liveScorea, detaljni prikaz matcha (basic prikaz samo i gumb za favoriziranje)
Mockanje podataka koristit mock liste i s njima se igrat (done)
ostale Agente

Trenutni agent:
za dohvacanje liveScorea svakih 25s,za dohvacanje prematcha svakih 10s
svaki ima jedno periodno i ciklicko ponasanje.

Novi:
AgentFavoriteMatchesObserver Provjera popracenih tekmi (najava,pocetak, kraj) (done)
AgentNotification: agent za prikaz notifikacija (done)
Agent za pracenje mreze? ako nestane mreza pojavi se prazan ekran sa natpisom nema mreze? (dolaskom mreze dojavljuje se glavnom agentu koji ce ovisno o ekrano pozvati akciju)
Glavni agent koji ce aktivirati ostale agente odnosno neki dogadaj ce aktivirati njihovo ponasanje
 */

data class FavoriteMatch(
    val matchId: String,
    val home: String,
    val away: String,
    val startTime: String,
    val status: Status,
    val winner: String?,
    val goals: String,
)

data class NotificationFavoriteMatch(
    val match: FavoriteMatch,
    var notificationTime: String
)