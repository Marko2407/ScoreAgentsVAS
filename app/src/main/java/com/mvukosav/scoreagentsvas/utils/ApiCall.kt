package com.mvukosav.scoreagentsvas.utils

import android.util.Log
import com.mvukosav.scoreagentsvas.utils.exceptions.ApiException
import com.mvukosav.scoreagentsvas.utils.exceptions.NoInternetException
import okhttp3.ResponseBody
import retrofit2.HttpException

/**
 * This is just coroutine-friendly bridge for [AsyncServiceCallback] that separates multiple concerns involved in
 * previous class.
 *
 * A few changes compared to previous implementation:
 *  - If you are willing to perform data mapping, do afterwards using [cz.etnetera.fortuna.services.mapData] function
 *  - No thread switching is done, result is delivered on the same dispatchers and not in UI thread, handle it on
 *  your own if needed
 *  - An instance of [Resource] is always returned, no unexpected exceptions!
 *    - Adjust [onError] and [onException] if needed, their default impl sticks to commonly-used impl in the app
 */
@Suppress("TooGenericExceptionCaught")
suspend fun <T> apiCall(
    onException: suspend (t: Exception) -> Resource<T> =
        { e -> Resource.createError(e) },
    onError: suspend (httpStatus: Int?, errorBody: ResponseBody?, message: String?) -> Resource<T> =
        { httpStatus, _, message ->
            Resource.createError(
                message = message,
                httpStatus = httpStatus
            )
        },
    body: suspend () -> T,
): Resource<T> = try {
    body.invoke().let { Resource.createData(it) }
} catch (th: Throwable) {
    Log.d("APICALL", th.toString())
    when (th) {
        is NoInternetException -> onError(null, null, "No internet connection")
        is ApiException -> onError(th.code, th.response?.errorBody(), th.response?.message())
        is HttpException -> onError(th.code(), th.response()?.errorBody(), th.response()?.message())
        else -> {
            Log.e("APICALL", th.toString())
            onException(Exception(th))
        }
    }
}
