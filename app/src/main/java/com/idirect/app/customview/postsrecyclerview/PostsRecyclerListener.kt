package com.idirect.app.customview.postsrecyclerview

import android.view.View
import com.idirect.app.datasource.model.Location
import com.idirect.app.datasource.model.UserPost
import com.sanardev.instagramapijava.model.timeline.MediaOrAd

interface PostsRecyclerListener {

    fun requestForLoadMore()
    fun likeComment(v: View, id:Long)
    fun unlikeComment(v: View,id:Long)
    fun unlikePost(v: View,mediaId:String)
    fun likePost(v: View,mediaId:String)
    fun shareMedia(v:View,mediaId: String,mediaType:Int)
    fun showComments(v:View,post: MediaOrAd)
    fun userProfile(v:View,userId:Long,username:String)
    fun onLocationClick(v:View,location:Location)
}