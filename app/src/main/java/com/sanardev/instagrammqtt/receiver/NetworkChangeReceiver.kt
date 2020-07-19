package com.sanardev.instagrammqtt.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.IntRange
import com.sanardev.instagrammqtt.datasource.model.event.ConnectionStateEvent
import com.sanardev.instagrammqtt.utils.NetworkUtils
import org.greenrobot.eventbus.EventBus
import run.tripa.android.extensions.toast

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (NetworkUtils.getConnectionType(context) != NetworkUtils.NetworkType.NONE) {
            EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NETWORK_CONNECTION_RESET))
        }
    }

}