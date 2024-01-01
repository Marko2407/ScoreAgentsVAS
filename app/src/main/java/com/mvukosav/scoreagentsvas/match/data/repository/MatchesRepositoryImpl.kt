package com.mvukosav.scoreagentsvas.match.data.repository

import android.content.Context
import com.google.gson.Gson
import com.mvukosav.scoreagentsvas.R
import com.mvukosav.scoreagentsvas.match.domain.model.Match
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import com.mvukosav.scoreagentsvas.service.ScoreServices
import kotlinx.coroutines.delay
import javax.inject.Inject

class MatchesRepositoryImpl @Inject constructor(
    private val api: ScoreServices,
    private val context: Context
) :
    MatchesRepository {
    override suspend fun getAllPreMatches(): Match? {
        return try {
            //Mock response
            delay(1000)
            val inputStream = context.resources.openRawResource(R.raw.matches_mock)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            return Gson().fromJson(jsonString, Match::class.java)

            // val response = apiCall { api.getAllPreMatches() }
            //(response as? Resource.Data)?.content
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getLiveScores(): Any? {
        TODO("Not yet implemented")
    }

    override suspend fun getLeague(): Any? {
        TODO("Not yet implemented")
    }

}