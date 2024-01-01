package com.mvukosav.scoreagentsvas.match.data.repository

import com.mvukosav.scoreagentsvas.match.domain.model.Match
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import com.mvukosav.scoreagentsvas.service.ScoreServices
import com.mvukosav.scoreagentsvas.utils.Resource
import com.mvukosav.scoreagentsvas.utils.apiCall
import javax.inject.Inject

class MatchesRepositoryImpl @Inject constructor(private val api: ScoreServices) :
    MatchesRepository {
    override suspend fun getAllPreMatches(): Match? {
        return try {
            val response = apiCall { api.getAllPreMatches() }
            (response as? Resource.Data)?.content
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