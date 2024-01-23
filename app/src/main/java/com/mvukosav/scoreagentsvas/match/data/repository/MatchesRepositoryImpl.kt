package com.mvukosav.scoreagentsvas.match.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.mvukosav.scoreagentsvas.AddToFavoriteMutation
import com.mvukosav.scoreagentsvas.CurrentOfferQuery
import com.mvukosav.scoreagentsvas.GetFavoriteMatchesQuery
import com.mvukosav.scoreagentsvas.MatchDetailsQuery
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.addToFavorite
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.connectivityManager
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.getLivescore
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.lastKnownMatchDetailId
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.livescores
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.matchDetailsMutableFlow
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.notificationService
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.repositoryScope
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.CurrentOfferGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchLiveScoreGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.MatchPreviewGraphQL
import com.mvukosav.scoreagentsvas.match.domain.model.livescores.Status
import com.mvukosav.scoreagentsvas.match.domain.model.matchdetails.EventsUi
import com.mvukosav.scoreagentsvas.match.domain.model.prematches.Match
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import com.mvukosav.scoreagentsvas.match.domain.usecase.AddFavoriteMatches
import com.mvukosav.scoreagentsvas.match.domain.usecase.GetMatchDetails
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MatchesRepositoryImpl @Inject constructor(
    private val context: Context,
    agentsNotificationService: AgentsNotificationService,
) : MatchesRepository {

    private val _livescoresMutableFlow: MutableStateFlow<List<CurrentOfferGraphQL?>?> =
        MutableStateFlow(listOf(null))
    override val livescoresFlow: StateFlow<List<CurrentOfferGraphQL?>?> = _livescoresMutableFlow

    private val _favoriteMatchesMutableFlow: MutableStateFlow<MutableList<String?>> =
        MutableStateFlow(mutableListOf())
    override val favoriteMatchesMutableFlow: StateFlow<List<String?>> = _favoriteMatchesMutableFlow

    private val _matchDetailsMutableFlow: MutableStateFlow<MatchLiveScoreGraphQL?> =
        MutableStateFlow(null)
    override val matchDetailsMutableFlow: StateFlow<MatchLiveScoreGraphQL?> =
        _matchDetailsMutableFlow

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var container: ContainerController? = null
    private var matchDetailcontainer: ContainerController? = null

    init {
        repositoryScope.launch(Dispatchers.Default) {
            _livescoresMutableFlow.value = getLiveScores()
        }
        livescores = _livescoresMutableFlow
        notificationService = agentsNotificationService
        getLivescore = RefreshLivescores(this)
        addToFavorite = AddFavoriteMatches(this)
        SharedMatchesRepository.repositoryScope = repositoryScope
        SharedMatchesRepository.favoriteMatchesMutableFlow = _favoriteMatchesMutableFlow
        SharedMatchesRepository.connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        SharedMatchesRepository.getMatchDetails = GetMatchDetails(this)
        SharedMatchesRepository.matchDetailsMutableFlow = _matchDetailsMutableFlow

        startAgents()
    }

    override suspend fun getLiveScores(): List<CurrentOfferGraphQL?>? {
        val responseFlow: Flow<List<CurrentOfferGraphQL?>?> =
            GraphQlManager.apolloClient().query(CurrentOfferQuery())
                .toFlow()
                .map { response ->
                    dataToDomainCurrentOffer(response.data?.currentOffer).also {
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
        val favList = mutableListOf<String?>()

        try {
            val r =
                GraphQlManager.apolloClient().query((GetFavoriteMatchesQuery("test"))).toFlow()
                    .first()
            if (r.hasErrors()) {
                _favoriteMatchesMutableFlow.emit(mutableListOf(null))
            } else {
                if (r.data?.favoriteMatches != null) {
                    r.data?.favoriteMatches?.match?.map {
                        favList.add(it?._id.toString())
                    }
                    _favoriteMatchesMutableFlow.emit(favList)
                } else {
                    _favoriteMatchesMutableFlow.emit(favList)
                }
            }
        } catch (e: Exception) {
            println(e.message)
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

    override fun clearAgent() {
        stopAgent()
        stopMatchDetailAgent()
    }

    override fun startMatchDetailsAgent(matchId: String) {
        startPrematchAgents(matchDetail = matchId)
    }

    override fun stopMatchDetailsAgent() {
        stopMatchDetailAgent()
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

    private fun stopMatchDetailAgent() {
        repositoryScope.launch(Dispatchers.IO) {
            try {
                matchDetailcontainer?.kill()
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
                        ),  // agent za favorite matches
                        Pair(
                            "NetworkObserverAgent",
                            NetworkAgent::class.java.name
                        ),  // agent za network
                        Pair(
                            "GeneralManagerAgent",
                            GeneralManagerAgent::class.java.name
                        )  // General agent
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

    private fun startPrematchAgents(matchDetail: String) {
        SharedMatchesRepository.lastKnownMatchDetailId = matchDetail
        repositoryScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val runtime = Runtime.instance()
                    val writableDir = context.filesDir.path
                    val profile = ProfileImpl().apply {
                        setParameter(Profile.FILE_DIR, writableDir)
                    }

                    matchDetailcontainer = runtime.createMainContainer(profile)

                    val agentController = container?.createNewAgent(
                        "MatchDetailDataFetchAgent",
                        MatchDetailDataFetchAgent::class.java.name,
                        null
                    )
                    agentController?.start()

                } catch (e: Exception) {
                    Log.e("JADE", "Exception in agent setup", e)
                }
            }
        }
    }
}

class LiveScoresDataFetchAgent : Agent() {
    override fun setup() {
        addBehaviour(object : TickerBehaviour(this, 30000) {
            override fun onTick() {
                repositoryScope.launch {
                    try {
                        val matches = getLivescore()
                        livescores.emit(matches)
                        Log.d("AGENT_LiveScoresDataFetchAgent", matches.toString())
                        sendMessageToPrematchesDataFetchAgent("Bok refreshao sam LIVESCORE ")
                    } catch (e: Exception) {
                        Log.e("AGENT_LiveScoresDataFetchAgent", "Error fetching livescores", e)
                    }
                }
            }
        })
        addBehaviour(object : CyclicBehaviour(this) {
            override fun action() {
                val receivedMsg = receive()
                if (receivedMsg != null) {
                    onMessageReceived(receivedMsg)
                    if (receivedMsg.sender.name.contains("GeneralManagerAgent") && receivedMsg.content == "Re-fetch data") {
                        repositoryScope.launch {
                            try {
                                Log.d("LiveScoresDataFetchAgent", "Get order from GM")
                                val matches = getLivescore()
                                livescores.emit(matches)
                            } catch (e: Exception) {
                                Log.e("LiveScoresDataFetchAgent", "Error fetching livescores", e)
                            }
                        }
                    } else if (receivedMsg.sender.name.contains("MatchDetailDataFetchAgent")) {
                        repositoryScope.launch {
                            try {
                                Log.d("LiveScoresDataFetchAgent", "Get order from match details")
                                val matches = getLivescore()
                                livescores.emit(matches)
                            } catch (e: Exception) {
                                Log.e("LiveScoresDataFetchAgent", "Error fetching livescores", e)
                            }
                        }
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

    private fun onMessageReceived(message: ACLMessage) {
        println("Received message: ${message.content}")
    }

    private fun sendMessageToPrematchesDataFetchAgent(content: String) {
        val message = ACLMessage(ACLMessage.INFORM)
        message.content = content
        send(message)
    }
}

class MatchDetailDataFetchAgent : Agent() {
    private var lastEmittedMatchDetails: MatchLiveScoreGraphQL? = null
    override fun setup() {
        addBehaviour(object : TickerBehaviour(this, 30000) {
            override fun onTick() {
                repositoryScope.launch {
                    try {
                        val matches =
                            SharedMatchesRepository.getMatchDetails(lastKnownMatchDetailId)
                        matchDetailsMutableFlow.emit(matches)
                        Log.d("MatchDetailDataFetchAgent", matches.toString())
                        if (matches != lastEmittedMatchDetails) {
                            matchDetailsMutableFlow.emit(matches)
                            lastEmittedMatchDetails = matches
                            sendMessage(
                                "Bok refreshao sam match details",
                                "LiveScoresDataFetchAgent"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("MatchDetailDataFetchAgent", "Error fetching livescores", e)
                    }
                }
            }
        })
        addBehaviour(object : CyclicBehaviour(this) {
            override fun action() {
                val receivedMsg = receive()
                if (receivedMsg != null) {
                    onMessageReceived(receivedMsg)
                    if (receivedMsg.sender.name.contains("GeneralManagerAgent") && receivedMsg.content == "Re-fetch data") {
                        repositoryScope.launch {
                            try {
                                Log.d("MatchDetailDataFetchAgent", "Get order from GM")
                                val matches =
                                    SharedMatchesRepository.getMatchDetails(lastKnownMatchDetailId)
                                matchDetailsMutableFlow.emit(matches)
                            } catch (e: Exception) {
                                Log.e("MatchDetailDataFetchAgent", "Error fetching livescores", e)
                            }
                        }
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

    private fun onMessageReceived(message: ACLMessage) {
        println("Received message: ${message.content}")
    }

    private fun sendMessage(content: String, agentName: String) {
        val message = ACLMessage(ACLMessage.INFORM)
        message.addReceiver(AID(agentName, AID.ISLOCALNAME))
        message.content = content
        send(message)
    }
}

class FavoriteMatchObserverAgent : Agent() {
    val favoriteList = mutableListOf<String?>()
    private var notificationCurrentList = mutableListOf<NotificationFavoriteMatch>()
    private var notificationBeginningList = mutableListOf<FavoriteMatch>()
    private var notificationFinishedList = mutableListOf<FavoriteMatch>()
    override fun setup() {
        addBehaviour(object : TickerBehaviour(this, 5000) {
            override fun onTick() {
                repositoryScope.launch {
                    try {
                        val newlist = SharedMatchesRepository.favoriteMatchesMutableFlow.value
                        // Ako favorit lista ne sadrzi sve isto kao i new list znaci da se desila promjena te observaj
                        if (!favoriteList.containsAll(newlist)) {
                            matchNotifyObserver()
                            favoriteList.addAll(newlist)
                        }
                    } catch (e: Exception) {
                        Log.e("LiveScoresDataFetchAgent", "Error fetching livescores", e)
                    }
                }
            }
        })

        addBehaviour(object : TickerBehaviour(this, 10000) {
            override fun onTick() {
                matchNotifyObserver()
            }
        })

        addBehaviour(object : CyclicBehaviour(this) {
            override fun action() {
                val receivedMsg = receive()
                if (receivedMsg != null) {
                    onMessageReceived(receivedMsg)
                    if (receivedMsg.sender.name.contains("GeneralManagerAgent") && receivedMsg.content == "Re-fetch data") {
                        repositoryScope.launch {
                            try {
                                Log.d("matchNotifyObserver", "Get order from GM")
                                matchNotifyObserver()
                            } catch (e: Exception) {
                                Log.e(
                                    "matchNotifyObserver",
                                    "Error observing matchNotifyObserver",
                                    e
                                )
                            }
                        }
                    }
                } else {
                    block()
                }
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
                val prematch = matches.filter { getTime(it.startTime) != "" }

                finishedMatch.forEach { favMatch ->
                    val n =
                        notificationFinishedList.filter { n -> favMatch == n }
                    if (n.isEmpty()) {
                        val winner = if (favMatch.winner?.contains("home") == true) {
                            "za " + favMatch.home
                        } else if (favMatch.winner?.contains("away") == true) {
                            "za " + favMatch.away
                        } else {
                            ""
                        }

                        val notification = Notification(
                            id = favMatch.matchId,
                            title = "Utakmica ${favMatch.home} - ${favMatch.away} je zavrsila",
                            content = "Rezultat je ${favMatch.goals} $winner"
                        )
                        val message = objectToJson(notification)
                        sendMessageToNotificationAgent(message)
                        notificationFinishedList.add(favMatch)
                        // ako je utakmica prosla
                        if (favMatch.isFavorite) {
                            addToFavorite(favMatch.matchId)
                            notificationFinishedList.removeIf { it == favMatch }
                            notificationBeginningList.removeIf { it == favMatch }
                        }
                    }
                }
                if (prematch.isNotEmpty()) {
                    prematch.forEach {
                        val n =
                            notificationCurrentList.filter { n -> it.matchId == n.match.matchId }

                        if (n.isEmpty()) { // notifikacija nikad nije bila
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
        Log.d("AGENT_NOTIFICATION_AGENT", content)
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
                            goals = match.goals ?: "0:0",
                            isFavorite = match.isFavorite
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
    override fun setup() {
        addBehaviour(object : CyclicBehaviour(this) {
            override fun action() {
                val receivedMsg = receive()
                if (receivedMsg != null) {
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

class NetworkAgent : Agent() {
    var isAvailable = false
    var hasGone = false
    override fun setup() {
        addBehaviour(object : TickerBehaviour(this, 3000) {
            override fun onTick() {
                val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

                connectivityManager.registerNetworkCallback(
                    networkRequest,
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)
                            if (isAvailable) {
                                //Pozovi GM agenta
                                sendMessageToAgent("fetch", "GeneralManagerAgent")
                                Log.d("NETWOKR_AVAI_D", network.toString())
                                isAvailable = false
                            }
                            hasGone = false
                        }

                        override fun onLost(network: Network) {
                            super.onLost(network)
                            if (!hasGone) {
                                //Pokazi prazan skreen sa por.
                                repositoryScope.launch {
                                    matchDetailsMutableFlow.emit(null)
                                    livescores.emit(null)
                                }
                                Log.d("NETWOKR_AVAI_N", network.toString())
                                hasGone = true
                            }
                            isAvailable = true
                        }
                    })
            }
        })
    }

    override fun takeDown() {
        println("Agent is being destroyed")
    }

    private fun sendMessageToAgent(content: String, agentName: String) {
        val message = ACLMessage(ACLMessage.INFORM)
        message.addReceiver(AID(agentName, AID.ISLOCALNAME))
        message.content = content
        send(message)
    }
}

class GeneralManagerAgent : Agent() {
    override fun setup() {
        addBehaviour(object : CyclicBehaviour(this) {
            override fun action() {
                val receivedMsg = receive()
                if (receivedMsg != null) {
                    if (receivedMsg.content == "fetch") {
                        sendMessageToNotificationAgent(
                            content = "Re-fetch data",
                            "LiveScoresDataFetchAgent"
                        )
                        sendMessageToNotificationAgent(
                            content = "Re-fetch data",
                            "FavoriteMatchObserverAgent"
                        )
                        sendMessageToNotificationAgent(
                            content = "Re-fetch data",
                            "MatchDetailDataFetchAgent"
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

    private fun sendMessageToNotificationAgent(content: String, agentName: String) {
        val message = ACLMessage(ACLMessage.INFORM)
        message.addReceiver(AID(agentName, AID.ISLOCALNAME))
        message.content = content
        send(message)
    }
}


object SharedMatchesRepository {
    lateinit var repositoryScope: CoroutineScope
    lateinit var matches: MutableStateFlow<Match?>

    lateinit var getLivescore: RefreshLivescores
    lateinit var livescores: MutableStateFlow<List<CurrentOfferGraphQL?>?>

    lateinit var getMatchDetails: GetMatchDetails
    lateinit var matchDetailsMutableFlow: MutableStateFlow<MatchLiveScoreGraphQL?>

    lateinit var addToFavorite: AddFavoriteMatches
    lateinit var notificationService: AgentsNotificationService
    lateinit var favoriteMatchesMutableFlow: MutableStateFlow<MutableList<String?>>
    lateinit var connectivityManager: ConnectivityManager
    var lastKnownMatchDetailId: String = "-"
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
    val isFavorite: Boolean = false
)

data class NotificationFavoriteMatch(
    val match: FavoriteMatch,
    var notificationTime: String
)