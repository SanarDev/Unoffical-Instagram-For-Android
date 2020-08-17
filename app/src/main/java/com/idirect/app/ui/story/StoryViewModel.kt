package com.idirect.app.ui.story

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.Tray
import com.idirect.app.datasource.model.response.InstagramStoriesResponse
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class StoryViewModel @Inject constructor(application: Application,var mUseCase: UseCase): BaseViewModel(application) {


    private val storyMediaLiveData = MutableLiveData<Resource<Tray>>()
    private val timeLineStories = MutableLiveData<Resource<InstagramStoriesResponse>>()
    val storyReactionResult = MutableLiveData<Resource<ResponseBody>>()

    val storiesData = MutableLiveData<Resource<List<Tray>>>()

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
            mUseCase.getStoryMedia(storyMediaLiveData,userId)
        }else{
            mUseCase.getTimelineStories(timeLineStories)
        }
    }
}