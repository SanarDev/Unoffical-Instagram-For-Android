package com.idirect.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.idirect.app.constants.InstagramConstants

class DismissNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationDataPref = context!!.getSharedPreferences(InstagramConstants.SharedPref.NOTIFICATION_DATA.name,Context.MODE_PRIVATE)
        val channelName = intent!!.extras!!.getString("channel_name")!!
        val keyMessages = channelName.replace(" ","_")+"_Messages"
        val keyNotificationId= channelName.replace(" ","_")+"_NotificationId"

        notificationDataPref
            .edit()
            .putString(keyMessages,"")
            .putInt(keyNotificationId,0).apply()
    }
}