package com.hatchmate.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hatchmate.app.R

object NotificationHelper {
    private const val CHANNEL_ID = "hatchmate_eggs_channel"
    private const val NOTIFICATION_ID = 101

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HatchMate Telur Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifikasi untuk pemutaran telur dan monitoring"
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showTurnEggsNotification(context: Context) {
        createNotificationChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Saatnya Putar Telur!")
            .setContentText("Waktunya memutarkan telur di inkubator Anda")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
