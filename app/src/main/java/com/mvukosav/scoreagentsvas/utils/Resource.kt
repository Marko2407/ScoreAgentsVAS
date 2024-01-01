package com.mvukosav.scoreagentsvas.utils

import androidx.lifecycle.LiveData
import com.mvukosav.scoreagentsvas.utils.Resource.Companion.STATE_EMPTY
import com.mvukosav.scoreagentsvas.utils.Resource.Companion.STATE_ERROR
import com.mvukosav.scoreagentsvas.utils.Resource.Companion.STATE_LOADING
import com.mvukosav.scoreagentsvas.utils.Resource.Companion.STATE_SUCCESS


/**
 * The Resource is used to propagate some specific data and their state to the view through [LiveData].
 * The data can have some "state" for example in case of Rest API calls. During the request the data should be in "loading" state.
 * If the request is successful it should be in "success" state or "error" if it fails.
 *
 * @property data The data that has been requested
 * @property state The value representing the state. It can be one of
 * * [STATE_SUCCESS]
 * * [STATE_LOADING]
 * * [STATE_ERROR]
 * * [STATE_EMPTY]
 * @property message The error message can be attached if the resource is in error state.
 *
 */
sealed class Resource<T : Any?>(
    @Deprecated(
        "`Data` is filled only for Resource.Data instances, for others is null. " +
            "This field DOES NOT reflect correct nullability, use resource.doOnData { t: T -> ...} or check for " +
            "specific instance `if (resource is Resource.Data) { val data = resource.content }`",
    )
    val data: T? = null,
    val message: String? = null,
) {

    companion object {
        const val STATE_EMPTY = 2
        const val STATE_SUCCESS = 1
        const val STATE_LOADING = 0
        const val STATE_ERROR = -1

        fun <T> createLoading(): Resource<T> = Loading()
        fun <T> createError(message: String? = null, httpStatus: Int? = null): Resource<T> =
            Error(null, message, httpStatus)

        fun <T> createError(t: Throwable): Resource<T> = Error(t, null, null)
        fun <T> createData(data: T): Resource<T> = Data(data)
        fun <T> createEmpty(): Resource<T> = Empty()
    }

    class Empty<T> : Resource<T>()
    class Loading<T> : Resource<T>()
    data class Data<T>(val content: T) : Resource<T>(data = content)
    data class Error<T>(val t: Throwable?, val msg: String?, val httpStatus: Int?) : Resource<T>(message = msg)
}

/**
 * Takes body of [Resource] and invokes mapping function which can return different model.
 * Use when you want to keep error, loading or empty state and just map 'success' model from [T] to [R].
 */
@Suppress("UNCHECKED_CAST")
inline fun <T, R> Resource<T>.mapData(body: (T) -> R): Resource<R> = when (this) {
    is Resource.Data -> {
        Resource.createData(content.let(body))
    }

    else -> this as Resource<R> // will always succeed since only Resource.Data holds <T>
}

/**
 * Invokes [body] if [this] is [Resource.Data] with data as callback function parameter
 * Note: this assumes data needs to be non-null in order to invoke callback!
 */
inline fun <T> Resource<T>.doOnData(body: (T) -> Unit): Resource<T> {
    if (this is Resource.Data) content.let(body)
    return this
}

/**
 * Invokes [body] if [this] is [Resource.Loading]
 */
inline fun <T> Resource<T>.doOnLoading(body: () -> Unit): Resource<T> {
    if (this is Resource.Loading) body()
    return this
}

/**
 * Invokes [body] if [this] is [Resource.Empty]
 */
inline fun <T> Resource<T>.doOnEmpty(body: () -> Unit): Resource<T> {
    if (this is Resource.Empty) body()
    return this
}

/**
 * Invokes [body] if [this] is [Resource.Error] with error message as callback function parameter
 */
inline fun <T> Resource<T>.doOnError(body: (String?) -> Unit): Resource<T> {
    if (this is Resource.Error) body(message)
    return this
}
