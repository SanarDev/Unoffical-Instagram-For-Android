package com.idirect.app.ui.story

import android.view.View

interface StoryActionListener {

    fun loadNextPage()
    fun loadPreviousPage()
    fun onProfileClick(v: View, userId:Long,username:String)
    fun viewPost(mediaId:String)
    fun viewPage(userId: Long,username:String)
}