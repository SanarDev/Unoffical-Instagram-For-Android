package com.idirect.app.customview.postsrecyclerview

interface PostsRecyclerListener {

    fun requestForLoadMore()
    fun likeComment(id:Long)
    fun unlikeComment(id:Long)

    fun unlikePost(mediaId:String)
    fun likePost(mediaId:String)
}