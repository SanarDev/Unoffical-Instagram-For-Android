package com.idirect.app.ui.userprofile

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.User
import com.idirect.app.datasource.model.response.InstagramPostsResponse
import com.idirect.app.datasource.model.response.InstagramUserInfo
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(application: Application,var mUseCase: UseCase):BaseViewModel(application)
{
    val userLiveData = MutableLiveData<Resource<InstagramUserInfo>>()
    private val userPosts = MutableLiveData<Resource<InstagramPostsResponse>>()
    var instagramPostsResponse:InstagramPostsResponse?=null
    var userId:Long = 0

    val resultUsePosts = Transformations.map(userPosts){
        if(it.status == Resource.Status.SUCCESS){
            if(instagramPostsResponse == null){
                instagramPostsResponse = it.data
            }else{
                instagramPostsResponse!!.userPosts.addAll(it.data!!.userPosts)
                it.data!!.userPosts = instagramPostsResponse!!.userPosts
            }
        }
        return@map it
    }
    fun init(userId: Long){
        this@UserProfileViewModel.userId = userId
        getUserProfile()
        getUserPosts()
    }
    private fun getUserProfile(){
        mUseCase.getUserProfile(userId,userLiveData)
    }

    private fun getUserPosts(){
        mUseCase.getUserPosts(userId,userPosts)
    }

    fun loadMorePosts(){
        val posts = instagramPostsResponse!!.userPosts
        val previousPostId = posts[posts.size -1].id
        mUseCase.loadMoreUserPosts(userId,previousPostId,userPosts)
    }
}