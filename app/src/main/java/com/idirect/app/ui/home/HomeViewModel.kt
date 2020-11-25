package com.idirect.app.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.idirect.app.core.BaseViewModel
import com.idirect.app.extentions.toList
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.model.timeline.FeedItems
import com.sanardev.instagramapijava.response.IGTimeLinePostsResponse
import com.sanardev.instagramapijava.response.IGTimeLineStoryResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

class HomeViewModel @Inject constructor(application: Application, val mUseCase: UseCase) :
    BaseViewModel(application) {

    private val _postsLiveData = MutableLiveData<Resource<IGTimeLinePostsResponse>>()
    private var instagramFeedTimeLineResponse: IGTimeLinePostsResponse? = null

    private val _storiesLiveData = MutableLiveData<Resource<IGTimeLineStoryResponse>>()
    val storiesLiveData:LiveData<Resource<IGTimeLineStoryResponse>> get() = _storiesLiveData

    val storyMediaLiveData =
        MutableLiveData<Resource<com.sanardev.instagramapijava.model.story.Tray>>()

    val postsLiveData = Transformations.map(_postsLiveData) {
        if (it.status == Resource.Status.SUCCESS) {
            if (instagramFeedTimeLineResponse == null) {
                instagramFeedTimeLineResponse = it.data
            } else {
                instagramFeedTimeLineResponse!!.feedItems.addAll(it.data!!.feedItems)
                it.data!!.feedItems = instagramFeedTimeLineResponse!!.feedItems
                instagramFeedTimeLineResponse = it.data
            }
            val posts = ArrayList<FeedItems>()
            for (item in it.data!!.feedItems) {
                if (item.mediaOrAd != null) {
                    posts.add(item)
                }
            }
            it.data!!.feedItems.clear()
            it.data!!.feedItems.addAll(posts)
        }
        return@map it
    }

    init {
        viewModelScope.launch {
            mUseCase.getTimelinePosts()
                .subscribe({
                    _postsLiveData.value = Resource.success(it)
                }, {}, {})

            mUseCase.getTimelineStory()
                .subscribe({
                    _storiesLiveData.value = Resource.success(it)
                }, {}, {})
        }
    }

    fun getStoryMedia(userId: Long) {
        viewModelScope.launch {
            mUseCase.getStoryMedia(userId)
                .subscribe({
                    storyMediaLiveData.value = Resource.success(it.reels[userId])
                }, {
                    Log.i("TEST", "TEST")
                }, {})
        }
    }

    fun unlikePost(id: String) {
        GlobalScope.launch {
            mUseCase.unlikePost(id).subscribe({}, {}, {})
        }
    }

    fun likePost(id: String) {
        GlobalScope.launch {
            mUseCase.likePost(id).subscribe({}, {}, {})
        }
    }

    fun getStandardVideoSize(width: Int, height: Int): Array<Int> {
        val screenWidth = DisplayUtils.getScreenWidth()
        val screenHeight = DisplayUtils.getScreenHeight()
        var standardHeight = (height * screenWidth) / width
        if (standardHeight > width && standardHeight > screenHeight / 3) {
            standardHeight = screenWidth
        }
        return arrayOf(screenWidth, standardHeight)
    }

    fun loadMorePosts() {
        viewModelScope.launch {
            instagramFeedTimeLineResponse?.let {
                mUseCase.loadMorePosts(it.nextMaxId)
                    .subscribe({
                        _postsLiveData.value = Resource.success(it)
                    }, {}, {})
            }
        }
    }

    fun likeComment(id: Long) {
        GlobalScope.launch {
            mUseCase.likeComment(id).subscribe({}, {}, {})
        }
    }

    fun unlikeComment(id: Long) {
        GlobalScope.launch {
            mUseCase.unlikeComment(id).subscribe({}, {}, {})
        }
    }
}