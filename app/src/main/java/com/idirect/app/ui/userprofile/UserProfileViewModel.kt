package com.idirect.app.ui.userprofile

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.idirect.app.core.BaseViewModel
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.response.IGPostsResponse
import com.sanardev.instagramapijava.response.IGUserInfoResponse
import javax.inject.Inject

open class UserProfileViewModel @Inject constructor(
    application: Application,
    val mUseCase: UseCase,
    var gson: Gson
) : BaseViewModel(application) {

    private val instaClient = InstaClient.getInstanceCurrentUser(application.applicationContext)
    private val _userLiveData = MutableLiveData<Resource<IGUserInfoResponse>>()
    var instagramPostsResponse: IGPostsResponse? = null
    var userId: Long = 0
    val resultUsePosts = MutableLiveData<Resource<IGPostsResponse>>()
    val userLiveData = Transformations.map(_userLiveData) {
        if (it.status == Resource.Status.SUCCESS) {
            userId = it.data!!.user.pk
            getUserPosts(userId)
        }
        return@map it
    }

    fun init(username: String?, userId: Long) {
        if (userId == 0.toLong()) {
            mUseCase.getUserInfoByUsername(username!!)
                .subscribe({
                    _userLiveData.value = Resource.success(it)
                },{
                    Log.i("TEST","TEST")
                },{})
        } else {
            getUserProfile(userId.toLong())
        }
    }

    private fun getUserProfile(userId: Long) {
        mUseCase.getUserInfo(userId)
            .subscribe({
                _userLiveData.value = Resource.success(it)
            },{
                Log.i("TEST","TEST")
            },{})
    }

    private fun getUserPosts(userId: Long) {
        mUseCase.getPosts(userId)
            .subscribe({
                instagramPostsResponse = it
                resultUsePosts.value = Resource.success(it)
            },{
                Log.i("TEST","TEST")
            },{})
    }

    fun loadMorePosts() {
        val posts = instagramPostsResponse!!.posts
        val previousPostId = posts[posts.size - 1].id
        mUseCase.getMorePosts(userId, previousPostId)
            .subscribe({
                instagramPostsResponse!!.numResults += it.numResults
                instagramPostsResponse!!.posts.addAll(it.posts)
                resultUsePosts.value = Resource.success(instagramPostsResponse)
            },{},{})
    }

}