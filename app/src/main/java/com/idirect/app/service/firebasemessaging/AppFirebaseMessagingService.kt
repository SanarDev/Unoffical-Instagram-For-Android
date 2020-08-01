package com.idirect.app.service.firebasemessaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.extentions.toast
import com.idirect.app.ui.login.LoginActivity
import com.idirect.app.utils.NotificationUtils

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()

    }

    override fun onNewToken(p0: String) {
        applicationContext.getSharedPreferences(InstagramConstants.SharedPref.SETTING.name, Context.MODE_PRIVATE)
            .edit().putString("notification_token",p0).apply()
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
        }
        if (remoteMessage.notification?.body != null) {
            NotificationUtils.notifyAlert(applicationContext,remoteMessage.notification!!.title,remoteMessage.notification!!.body!!)
        }
    }

}