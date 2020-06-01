package com.sanardev.instagrammqtt.receiver

import android.content.Context
import net.igenius.mqttservice.MQTTServiceReceiver


class MQTTReceiver : MQTTServiceReceiver() {

    override fun onPublishSuccessful(
        context: Context, requestId: String,
        topic: String
    ) {
        // called when a message has been successfully published
    }

    override fun onSubscriptionSuccessful(
        context: Context, requestId: String,
        topic: String
    ) {
        // called when a subscription is successful
    }

    override fun onSubscriptionError(
        context: Context, requestId: String,
        topic: String, exception: Exception
    ) {
        // called when a subscription is not successful.
        // This usually happens when the broker does not give permissions
        // for the requested topic
    }

    override fun onMessageArrived(
        context: Context, topic: String,
        payload: ByteArray
    ) {
        // called when a new message arrives on any topic
    }

    override fun onConnectionSuccessful(context: Context, requestId: String) {
        // called when the connection is successful
    }

    override fun onException(
        context: Context, requestId: String,
        exception: Exception
    ) {
        // called when an error happens
    }

    override fun onConnectionStatus(context: Context, connected: Boolean) {
        // called when connection status is requested or changes
    }
}