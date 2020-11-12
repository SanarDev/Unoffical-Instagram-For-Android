package com.idirect.app.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.idirect.app.core.BaseViewModel
import com.idirect.app.extentions.toList
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.model.timeline.FeedItems
import com.sanardev.instagramapijava.response.IGTimeLinePostsResponse
import com.sanardev.instagramapijava.response.IGTimeLineStoryResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class HomeViewModel @Inject constructor(application: Application):BaseViewModel(application) {

    private val _postsLiveData = MutableLiveData<Resource<IGTimeLinePostsResponse>>()
    private var instagramFeedTimeLineResponse:IGTimeLinePostsResponse?=null
    private val _storiesLiveData = MutableLiveData<Resource<IGTimeLineStoryResponse>>()
    val storyMediaLiveData = MutableLiveData<Resource<com.sanardev.instagramapijava.model.story.Tray>>()
    private val instaClient = InstaClient.getInstanceCurrentUser(application.applicationContext)

    val postsLiveData = Transformations.map(_postsLiveData){
        if(it.status == Resource.Status.SUCCESS){
            if(instagramFeedTimeLineResponse == null){
                instagramFeedTimeLineResponse = it.data
            }else{
                instagramFeedTimeLineResponse!!.feedItems.addAll(it.data!!.feedItems)
                it.data!!.feedItems = instagramFeedTimeLineResponse!!.feedItems
                instagramFeedTimeLineResponse = it.data
            }
            val posts = ArrayList<FeedItems>()
            for(item in it.data!!.feedItems){
                if(item.mediaOrAd != null){
                    posts.add(item)
                }
            }
            it.data!!.feedItems.clear()
            it.data!!.feedItems.addAll(posts)
        }
        return@map it
    }
    val storiesLiveData = Transformations.map(_storiesLiveData){
        return@map it
    }

    init {
        instaClient.mediaProcessor.getTimelinePosts()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _postsLiveData.value = Resource.success(it)
            },{},{})
        instaClient.storyProcessor.getTimelineStory()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _storiesLiveData.value = Resource.success(it)
            },{},{})
    }
    fun getStoryMedia(userId:Long){
        instaClient.storyProcessor.getStoryMedia(userId.toList())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                storyMediaLiveData.value = Resource.success(it.reels[userId])
            },{
                Log.i("TEST","TEST")
            },{})
    }
    fun unlikePost(id: String) {
        instaClient.mediaProcessor.unlikePost(id).subscribe({},{},{})
    }

    fun likePost(id: String) {
        instaClient.mediaProcessor.likePost(id).subscribe({},{},{})
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
            instaClient.mediaProcessor.getTimelinePosts(it.nextMaxId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _postsLiveData.value = Resource.success(it)
                },{},{})
        }
    }

    fun likeComment(id: Long) {
        instaClient.commentProcessor.likeComment(id.toString()).subscribe({},{},{})
    }
    fun unlikeComment(id: Long) {
        instaClient.commentProcessor.unlikeComment(id.toString()).subscribe({},{},{})
    }
}