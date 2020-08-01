package com.idirect.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.datasource.model.event.ConnectionStateEvent
import com.idirect.app.service.fbns.FbnsIntent
import com.idirect.app.service.fbns.FbnsService
import com.idirect.app.utils.NetworkUtils
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
        }else{
            EventBus.getDefault()
                .postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NETWORK_DISCONNECTED))
        }
    }

}