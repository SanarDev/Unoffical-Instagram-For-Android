package com.sanardev.instagrammqtt.ui.direct

import com.sanardev.instagrammqtt.datasource.model.Message

interface ActionListener {

    fun onNewMessage(message: Message)
    fun onChangeMessage(message: Message)
    fun onChangeMessageWithClientContext(message: Message)
}