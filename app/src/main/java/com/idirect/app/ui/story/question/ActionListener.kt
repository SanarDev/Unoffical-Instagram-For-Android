package com.idirect.app.ui.story.question

import java.io.Serializable

interface ActionListener :Serializable {

    fun onSendResponse(response:String)
    fun onDismiss()
}