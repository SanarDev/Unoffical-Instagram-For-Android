package com.idirect.app.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.receiver.DismissNotificationReceiver
import com.idirect.app.ui.login.LoginActivity
import kotlin.random.Random


class NotificationUtils {

    companion object {

        private val KEY_TEXT_REPLY = "key_text_reply"
        val GROUP_KEY_DIRECT = "com.idirect.app.DIRECT_MESSAGE"

        fun dismissAllNotification(application: Application) {
            val notificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }

        fun notifyAlert(context: Context,title:String?,message: String){
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            val channelId = InstagramConstants.ALERT_NOTIFICATION_CHANNEL_ID
            val channelName = InstagramConstants.ALERT_NOTIFICATION_CHANNEL_ID
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_send)
                .setContentTitle(title ?: context.getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(Random.nextInt() /* ID of notification */, notificationBuilder.build())
        }

        fun notify(
            application: Application,
            channelId: String,
            notificationId: Int,
            channelName: String,
            title: String,
            message: String,
            oldMessage: List<String> = emptyList(),
            photoUrl: String? = null,
            isHighLevel: Boolean = true
        ) {

//            var replyLabel: String = application.getString(R.string.reply_label)
//            val remoteInput = androidx.core.app.RemoteInput.Builder(KEY_TEXT_REPLY).run {
//                setLabel(replyLabel)
//                build()
//            }
//
            val intent = Intent(application, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                application, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            val deleteIntent = Intent(application, DismissNotificationReceiver::class.java)
                .putExtra("channel_name",channelName)
            val uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()
            val broadcastIntent =
                PendingIntent.getBroadcast(application.getApplicationContext(), uniqueInt, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

//
//            val action: NotificationCompat.Action =
//                NotificationCompat.Action.Builder(R.drawable.ic_replay,
//                    application.getString(R.string.label), pendingIntent)
//                    .addRemoteInput(remoteInput)
//                    .build()

            val futureTarget = Glide.with(application)
                .asBitmap()
                .load(photoUrl)
                .circleCrop()
                .submit()

            val bitmap = futureTarget.get()

            val notificationLevel = if (isHighLevel) {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }
            val notificationChannelLevel = if (isHighLevel) {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(application, channelId)
                .setSmallIcon(R.drawable.ic_send)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(notificationLevel)
                .setContentIntent(pendingIntent)

            if (oldMessage.isNotEmpty()) {
                val inboxStyle =
                    NotificationCompat.InboxStyle()
                inboxStyle.setBigContentTitle(title)
                inboxStyle.setSummaryText(String.format("You have %d notifications",oldMessage.size)) // chon yeki az item ha empty e

                for (i in oldMessage.indices) {
                    inboxStyle.addLine(oldMessage[i])
                }
                notificationBuilder.setStyle(inboxStyle)
            }

            val notificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    notificationChannelLevel
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationBuilder.setDeleteIntent(broadcastIntent);
            notificationManager.notify(
                notificationId /* ID of notification */,
                notificationBuilder.build()
            )
        }
    }
}