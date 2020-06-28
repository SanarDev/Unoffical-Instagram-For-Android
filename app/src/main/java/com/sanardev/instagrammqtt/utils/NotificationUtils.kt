package com.sanardev.instagrammqtt.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.ui.main.MainActivity
import kotlin.random.Random


class NotificationUtils {

    companion object {

        fun notify(
            application: Application,
            channelId: String,
            channelName: String,
            title: String,
            message: String
        ) {
            val intent = Intent(application, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                application, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(application, channelId)
                .setSmallIcon(R.drawable.ic_note)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(
                0 /* ID of notification */,
                notificationBuilder.build()
            )
        }
    }
}