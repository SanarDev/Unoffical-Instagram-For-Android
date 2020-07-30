package com.sanardev.instagrammqtt.ui.direct

import com.sanardev.instagrammqtt.datasource.model.Message
import com.sanardev.instagrammqtt.realtime.commands.RealTimeCommand

interface ActionListener {

    fun onNewMessage(message: Message)
    fun onChangeMessage(message: Message)
    fun onChangeMessageWithClientContext(message: Message)
    fun realTimeCommand(realTimeCommand: RealTimeCommand)
    fun removeMessage(itemId: String)
}