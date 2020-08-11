package com.idirect.app.ui.posts

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.idirect.app.core.BaseApplication
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramPostsResponse
import com.idirect.app.ui.userprofile.UserProfileViewModel
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import javax.inject.Inject

class PostsViewModel @Inject constructor(application: Application,var mUseCase: UseCase): BaseViewModel(application) {

    val resultUsePosts = MutableLiveData<Resource<InstagramPostsResponse>>()
    var instagramPostsResponse:InstagramPostsResponse?=null
    var userId:Long = 0

    fun init(userId: String){
        this.userId = userId.toLong()
        getUserPosts()
    }

    private fun getUserPosts(){
        mUseCase.getUserPosts(userId).observeForever {
            if(it.status == Resource.Status.SUCCESS){
                instagramPostsResponse = it.data
            }
            resultUsePosts.value = it
        }
    }

    fun loadMorePosts(){
        val posts = instagramPostsResponse!!.userPosts
        val previousPostId = posts[posts.size -1].id
        mUseCase.loadMoreUserPosts(userId,previousPostId,resultUsePosts)
    }


    fun unlikePost(id: String) {
        mUseCase.unlikePost(id)
    }

    fun likePost(id: String) {
        mUseCase.likePost(id)
    }

    fun unlikeComment(mediaId: Long) {
        mUseCase.unlikeComment(mediaId.toString())
    }
    fun likeComment(mediaId: Long) {
        mUseCase.likeComment(mediaId.toString())
    }


}