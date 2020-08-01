package com.idirect.app.ui.main

interface ActionListener {

    fun onLastActivityChange(threadId:String,lastActivityAt:Long,isActive:Boolean)
}