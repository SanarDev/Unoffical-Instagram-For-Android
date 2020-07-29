package com.sanardev.instagrammqtt.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.event.ConnectionStateEvent
import com.sanardev.instagrammqtt.extentions.toast
import com.sanardev.instagrammqtt.service.fbns.FbnsIntent
import com.sanardev.instagrammqtt.service.fbns.FbnsService
import com.sanardev.instagrammqtt.utils.NetworkUtils
import org.greenrobot.eventbus.EventBus

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (NetworkUtils.getConnectionType(context) != NetworkUtils.NetworkType.NONE) {
            EventBus.getDefault()
                .postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NETWORK_CONNECTION_RESET))

            context.getSharedPreferences(InstagramConstants.SharedPref.USER.name,Context.MODE_PRIVATE).getBoolean("is_logged",false).also {
                if(it){
                    FbnsService.run(context, FbnsIntent.ACTION_CONNECT_SESSION)
                }
            }
        }
    }

}