package com.idirect.app.ui.direct

import com.idirect.app.realtime.commands.RealTimeCommand
import com.sanardev.instagramapijava.model.direct.Message

interface ActionListener {

    fun onNewMessage(message: Message)
    fun onChangeMessage(message: Message)
    fun onChangeMessageWithClientContext(message: Message)
    fun realTimeCommand(realTimeCommand: RealTimeCommand)
    fun removeMessage(itemId: String)
}