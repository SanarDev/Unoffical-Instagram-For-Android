package com.idirect.app.ui.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.datasource.model.response.InstagramFeedTimeLineResponse
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class HomeViewModel @Inject constructor(application: Application,var mUseCase: UseCase):BaseViewModel(application) {

    private val _postsLiveData = MutableLiveData<Resource<InstagramFeedTimeLineResponse>>()
    private var instagramFeedTimeLineResponse:InstagramFeedTimeLineResponse?=null

    val postsLiveData = Transformations.map(_postsLiveData){
        if(it.status == Resource.Status.SUCCESS){
            if(instagramFeedTimeLineResponse == null){
                instagramFeedTimeLineResponse = it.data
            }else{
                instagramFeedTimeLineResponse!!.feedItems.addAll(it.data!!.feedItems)
                it.data!!.feedItems = instagramFeedTimeLineResponse!!.feedItems
                instagramFeedTimeLineResponse = it.data
            }
            it.data!!.posts = ArrayList<UserPost>().toMutableList()
            for(item in it.data!!.feedItems){
                if(item.mediaOrAd != null){
                    it.data!!.posts.add(item.mediaOrAd)
                }
            }
        }
        return@map it
    }

    init {
        mUseCase.getTimelinePosts(_postsLiveData)
    }
    fun unlikePost(id: String) {
        mUseCase.unlikePost(id)
    }

    fun likePost(id: String) {
        mUseCase.likePost(id)
    }

    fun getStandardVideoSize(width: Int, height: Int): Array<Int> {
        val screenWidth = DisplayUtils.getScreenWidth()
        val screenHeight = DisplayUtils.getScreenHeight()
        var standardHeight = (height * screenWidth) / width
        if(standardHeight > width && standardHeight > screenHeight/3){
            standardHeight = screenWidth
        }
        return arrayOf(screenWidth,standardHeight)
    }

    fun loadMorePosts() {
        instagramFeedTimeLineResponse?.let {
            mUseCase.loadMoreTimelinePosts(_postsLiveData,it.nextMaxId)
        }
    }
}