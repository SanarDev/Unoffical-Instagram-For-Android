package com.idirect.app.ui.story

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.Tray
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.response.BaseResponse
import com.sanardev.instagramapijava.response.IGSendStoryReactionResponse
import com.sanardev.instagramapijava.response.IGStoryMediaResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.ResponseBody
import javax.inject.Inject

class StoryViewModel @Inject constructor(application: Application) :
    BaseViewModel(application) {

    private val instaClient = InstaClient.getInstanceCurrentUser(application.applicationContext)

    val storyMediaLiveData =
        MutableLiveData<Resource<com.sanardev.instagramapijava.model.story.Tray>>()
    val storyReactionResult = MutableLiveData<Resource<BaseResponse>>()

    fun getStoryData(userId: Long) {
        instaClient.storyProcessor.getStoryMedia(userId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                storyMediaLiveData.value = Resource.success(it.reels[userId])
            }, {}, {})
    }

    fun sendStoryReaction(threadId: String, mediaId: String, reaction: String, reelId: Long) {
        instaClient.storyProcessor.sendStoryReaction(threadId, mediaId, reaction, reelId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                storyReactionResult.value = Resource.success(it)
            }, {}, {})
    }

    fun replyStory(threadId: String, mediaId: String, mediaType: Int, text: String, reelId: Long) {
        instaClient.storyProcessor.sendStoryReply(threadId, mediaId,text, reelId,mediaType)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                storyReactionResult.value = Resource.success(it)
            },{},{})
    }

    @SuppressLint("CheckResult")
    fun voteSlider(storyPosition:Int,id: String, sliderId: Long, vote: Float) {
        instaClient.storyProcessor.voteSlider(vote,sliderId,id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
            val tray = storyMediaLiveData.value!!.data!!
            tray.items[storyPosition] = it.story
            storyMediaLiveData.value = Resource.success(tray)
        },{
            Log.i("TEST","TEST")
        },{

        })
    }

    fun markStoryAsSeen(id: String, takenAt: Long) {
        instaClient.storyProcessor.markStoryAsSeen(id,takenAt)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(InstagramConstants.DEBUG_TAG,"Seen Success")
            },{
                Log.i(InstagramConstants.DEBUG_TAG,"Seen Fail")
            },{})
    }

    fun sendStoryQuestionResponse(id:String,questionId:Long,response: String) {
        instaClient.storyProcessor.storyQuestionResponse(id,questionId,response)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i("TEST","TEST")
            },{
                Log.i("TEST","TEST")
            },{})
    }
}