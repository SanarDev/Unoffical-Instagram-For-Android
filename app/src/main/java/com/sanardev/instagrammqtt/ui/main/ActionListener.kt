package com.sanardev.instagrammqtt.ui.main

interface ActionListener {

    fun onLastActivityChange(threadId:String,lastActivityAt:Long,isActive:Boolean)
}