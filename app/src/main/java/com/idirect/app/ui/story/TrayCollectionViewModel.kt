package com.idirect.app.ui.story

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.Tray
import com.idirect.app.datasource.model.response.InstagramStoriesResponse
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.response.IGTimeLineStoryResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.ResponseBody
import javax.inject.Inject

class TrayCollectionViewModel @Inject constructor(application: Application): BaseViewModel(application) {

    private val instaClient = InstaClient.getInstanceCurrentUser(application.applicationContext)

    private val storyMediaLiveData = MutableLiveData<Resource<com.sanardev.instagramapijava.model.story.Tray>>()
    private val timeLineStories = MutableLiveData<Resource<IGTimeLineStoryResponse>>()
    val storyReactionResult = MutableLiveData<Resource<ResponseBody>>()

    val storiesData = MutableLiveData<Resource<List<com.sanardev.instagramapijava.model.story.Tray>>>()

    init {
        storyMediaLiveData.observeForever {
            if(it.status == Resource.Status.LOADING){
                storiesData.value = Resource.loading()
            }else if(it.status == Resource.Status.SUCCESS){
                storiesData.value = Resource.success(arrayOf(it.data!!).toList())
            }
        }
        timeLineStories.observeForever {
            if(it.status == Resource.Status.LOADING){
                storiesData.value = Resource.loading()
            }else if(it.status == Resource.Status.SUCCESS){
                storiesData.value = Resource.success(it.data!!.tray)
            }
        }
    }
    fun getStoryData(userId: Long,isSingle:Boolean) {
        if(isSingle){
            instaClient.storyProcessor.getStoryMedia(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    storyMediaLiveData.value = Resource.success(it.reels[userId])
                },{},{})
        }else{
            instaClient.storyProcessor.timelineStory
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    timeLineStories.value = Resource.success(it)
                },{},{})
        }
    }
}