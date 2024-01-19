package com.mvukosav.scoreagentsvas.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mvukosav.scoreagentsvas.MainActivity
import com.mvukosav.scoreagentsvas.R
import com.mvukosav.scoreagentsvas.service.AgentsNotificationService
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random


class AgentsNotificationServiceImpl @Inject constructor(private val context: Context) :
    AgentsNotificationService {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun showNotification(title: String, content: String, notifId: String) {
        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context, 1, activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, AGENTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(activityPendingIntent)
            .build()
        Log.d("LOLOLO_ANOTIF", "notigikacije")
        notificationManager.notify(Random.nextInt(), notification)
    }

    override fun showNotification2(title: String, content: String, notifId: Int) {
        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context, 1, activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, AGENTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(activityPendingIntent)
            .build()

        notificationManager.notify(2, notification)
    }

    companion object {
        const val AGENTS_CHANNEL_ID = "agents_channel"
        const val AGENTSF_CHANNEL_ID = "agents2_channel"
    }
}
