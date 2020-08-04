package com.idirect.app.ui.inbox

interface ActionListener {

    fun onLastActivityChange(threadId:String,lastActivityAt:Long,isActive:Boolean)
}