package com.mvukosav.scoreagentsvas.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader

@Suppress("SwallowedException", "TooGenericExceptionCaught")
inline fun <T> safe(eval: () -> T): T? {
    return try {
        eval()
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T> Reader.fromJson(): T {
    val type = object : TypeToken<List<T>>() {}.type
    return Gson().fromJson(this, type)
}
