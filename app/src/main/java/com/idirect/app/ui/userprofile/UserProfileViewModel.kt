package com.idirect.app.ui.userprofile

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.idirect.app.R
import com.idirect.app.core.BaseApplication
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.APIErrors
import com.idirect.app.datasource.model.User
import com.idirect.app.datasource.model.payload.StatusResult
import com.idirect.app.datasource.model.response.InstagramPostsResponse
import com.idirect.app.datasource.model.response.InstagramUserInfo
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

open class UserProfileViewModel @Inject constructor(
    application: Application,
    var mUseCase: UseCase,
    var gson: Gson
) : BaseViewModel(application) {
    private val _userLiveData = MutableLiveData<Resource<InstagramUserInfo>>()
    var instagramPostsResponse: InstagramPostsResponse? = null
    var userId: Long = 0
    val resultUsePosts = MutableLiveData<Resource<InstagramPostsResponse>>()
    val userLiveData = Transformations.map(_userLiveData) {
        if (it.status == Resource.Status.SUCCESS) {
            userId = it.data!!.user.pk
            getUserPosts(userId)
        }
        return@map it
    }

    fun init(username: String?, userId: Long) {
        if (userId == 0.toLong()) {
            mUseCase.getUserInfoFromUsername(_userLiveData, username!!)
        } else {
            getUserProfile(userId.toLong())
        }
    }

    private fun getUserProfile(userId: Long) {
        mUseCase.getUserProfile(userId, _userLiveData)
    }

    private fun getUserPosts(userId: Long) {
        mUseCase.getUserPosts(userId).observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                instagramPostsResponse = it.data
            }
            resultUsePosts.value = it
        }
    }

    fun loadMorePosts() {
        val posts = instagramPostsResponse!!.userPosts
        val previousPostId = posts[posts.size - 1].id
        mUseCase.loadMoreUserPosts(userId, previousPostId, resultUsePosts)
    }

}