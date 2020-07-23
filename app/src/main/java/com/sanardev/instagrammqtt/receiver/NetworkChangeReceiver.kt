package com.sanardev.instagrammqtt.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sanardev.instagrammqtt.datasource.model.event.ConnectionStateEvent
import com.sanardev.instagrammqtt.utils.NetworkUtils
import org.greenrobot.eventbus.EventBus

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (NetworkUtils.getConnectionType(context) != NetworkUtils.NetworkType.NONE) {
            EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NETWORK_CONNECTION_RESET))
        }
    }

}