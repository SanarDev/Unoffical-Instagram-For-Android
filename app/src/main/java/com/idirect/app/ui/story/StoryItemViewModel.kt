package com.idirect.app.ui.story

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.Tray
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class StoryItemViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    val storyMediaLiveData = MutableLiveData<Resource<Tray>>()
    val storyReactionResult = MutableLiveData<Resource<ResponseBody>>()

    fun getStoryData(userId: Long) {
        mUseCase.getStoryMedia(storyMediaLiveData, userId)
    }

    fun sendStoryReaction(threadId: String, mediaId: String, reaction: String, reelId: Long) {
        mUseCase.sendStoryReaction(threadId, mediaId, reaction, reelId).observeForever {
            storyReactionResult.value = it
        }
    }

    fun replyStory(threadId: String,mediaId: String,mediaType:Int,text:String,reelId:Long){
        mUseCase.sendStoryReply(threadId,mediaId,mediaType,reelId,text).observeForever {
            storyReactionResult.value = it
        }
    }

    fun loadNextPageStory(userId: Long) {
        mUseCase.getNextPageStory(storyMediaLiveData, userId)
    }
}