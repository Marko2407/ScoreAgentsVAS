package com.mvukosav.scoreagentsvas.utils

import com.google.gson.Gson


fun objectToJson(myObject: Notification): String {
    val gson = Gson()
    return gson.toJson(myObject)
}

fun jsonToObject(jsonString: String): Notification {
    val gson = Gson()
    return gson.fromJson(jsonString, Notification::class.java)
}

data class Notification(
    val title: String,
    val content: String,
    val id: String
)