package com.mvukosav.scoreagentsvas.utils.exceptions

import com.mvukosav.scoreagentsvas.utils.fromJson
import com.mvukosav.scoreagentsvas.utils.safe
import retrofit2.Response

/**
 * Exception that can be returned from api call. Contains HTTP error code.
 */
data class ApiException(val code: Int, val response: Response<*>?) : Exception()

data class ApiError(
    val parameters: List<String>,
    val relatedEntity: String,
    val severity: String,
    val status: String,
)

fun ApiException.errors() = response?.errorBody()?.let {
    safe { it.charStream().fromJson<List<ApiError>>() }
}.orEmpty()
