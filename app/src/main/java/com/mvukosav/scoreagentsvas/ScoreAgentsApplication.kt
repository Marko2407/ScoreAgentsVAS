package com.mvukosav.scoreagentsvas

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.mvukosav.scoreagentsvas.utils.AgentsNotificationServiceImpl
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScoreAgentsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
//        createNotificationChannel2()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AgentsNotificationServiceImpl.AGENTS_CHANNEL_ID,
            "AgentsNotification",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "this is agents notification "

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotificationChannel2() {
        val channel = NotificationChannel(
            AgentsNotificationServiceImpl.AGENTSF_CHANNEL_ID,
            "Agents2Notification",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "this is agents 2 notification "

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
