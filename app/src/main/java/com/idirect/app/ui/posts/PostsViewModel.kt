package com.idirect.app.ui.posts

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.idirect.app.R
import com.idirect.app.core.BaseViewModel
import com.idirect.app.customview.toast.CustomToast
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.response.IGPostsResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class PostsViewModel @Inject constructor(application: Application): BaseViewModel(application) {

    val instaClient = InstaClient.getInstanceCurrentUser(application.applicationContext)
    val resultUsePosts = MutableLiveData<Resource<IGPostsResponse>>()
    var instagramPostsResponse:IGPostsResponse?=null
    var userId:Long = 0

    fun init(userId: String){
        this.userId = userId.toLong()
        getUserPosts()
    }

    private fun getUserPosts(){
        instaClient.userProcessor.getPosts(userId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                instagramPostsResponse = it
                resultUsePosts.value = Resource.success(it)
            },{
                Log.i("TEST","TEST")
            },{})
    }

    fun loadMorePosts(){
        val posts = instagramPostsResponse!!.posts
        val previousPostId = posts[posts.size -1].id
        instaClient.userProcessor.getMorePosts(userId,previousPostId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                resultUsePosts.value = Resource.success(it)
            },{},{})
    }


    fun unlikePost(id: String) {
        val context = getApplication<Application>().applicationContext
        instaClient.mediaProcessor.unlikePost(id).subscribe({},{
            CustomToast.show(context,context.getString(R.string.error_in_unlikepost),Toast.LENGTH_SHORT)
        },{})
    }

    fun likePost(id: String) {
        val context = getApplication<Application>().applicationContext
        instaClient.mediaProcessor.likePost(id).subscribe({},{
            CustomToast.show(context,context.getString(R.string.error_in_like_post),Toast.LENGTH_SHORT)
        },{})
    }

    fun unlikeComment(mediaId: Long) {
        val context = getApplication<Application>().applicationContext
        instaClient.commentProcessor.unlikeComment(mediaId.toString()).subscribe({},{
            CustomToast.show(context,context.getString(R.string.error_in_unlike_comment),Toast.LENGTH_SHORT)
        },{})
    }
    fun likeComment(mediaId: Long) {
        val context = getApplication<Application>().applicationContext
        instaClient.commentProcessor.likeComment(mediaId.toString()).subscribe({},{
            CustomToast.show(context,context.getString(R.string.error_in_like_comment),Toast.LENGTH_SHORT)
        },{})
    }


}