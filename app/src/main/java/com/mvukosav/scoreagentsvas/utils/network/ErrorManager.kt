package com.mvukosav.scoreagentsvas.utils.network

import com.apollographql.apollo3.exception.ApolloNetworkException
import okio.IOException

object ErrorManager {

    fun isErrorApi(errorList: List<com.apollographql.apollo3.api.Error>?): Boolean {
        return errorList != null
    }

    fun <T> errorHandler(e: Throwable): Response<T> {
        return if (isNetworkError(e)) {
            Response.NetworkError()
        } else {
            Response.Error(-1, ErrorCode.UNKNOWN_ERROR.name)
        }
    }

    fun <T> parserApiError(errorList: List<com.apollographql.apollo3.api.Error>?): Response.ErrorApi<T> {
        errorList?.find {
            it.message == "Error fetching Token"
        }?.let {
            return Response.ErrorApi(ErrorCode.AUTHORIZATION_ERROR)
        }
        return Response.ErrorApi(ErrorCode.UNKNOWN_ERROR)
    }

    private fun isNetworkError(e: Throwable): Boolean {
        return e is IOException || e is ApolloNetworkException
    }
}

enum class ErrorCode {
    NOT_FOUND,
    INSUFFICIENT_PERMISSION,
    FORBIDDEN,
    AUTHORIZATION_ERROR,
    UNKNOWN_ERROR,
    NETWORK_ERROR,
    USER_NOT_FOUND
}

sealed class Response<T> {
    data class Result<T>(val result: T) : Response<T>()
    data class Error<T>(val statusCode: Int, val message: String) : Response<T>()
    data class ErrorApi<T>(val error: ErrorCode) : Response<T>()
    class NetworkError<T> : Response<T>()
}