package com.sanardev.instagrammqtt.service.firebasemessaging

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.sanardev.instagrammqtt.extentions.toast

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()

        toast("onCreate")
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        toast("rebind")
    }
}