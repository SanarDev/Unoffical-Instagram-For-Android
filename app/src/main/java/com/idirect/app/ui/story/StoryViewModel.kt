package com.idirect.app.ui.story

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseViewModel
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.response.BaseResponse
import javax.inject.Inject

class StoryViewModel @Inject constructor(application: Application, val mUseCase: UseCase) :
    BaseViewModel(application) {

    val storyMediaLiveData =
        MutableLiveData<Resource<com.sanardev.instagramapijava.model.story.Tray>>()
    val storyReactionResult = MutableLiveData<Resource<BaseResponse>>()

    fun getStoryData(userId: Long) {
        mUseCase.getStoryMedia(userId)
            .subscribe({
                calculateCurrentPosition(it.reels[userId]!!)
                storyMediaLiveData.value = Resource.success(it.reels[userId])
            }, {
                Log.i("TEST","TEST")
            }, {})
    }

    private fun calculateCurrentPosition(tray: com.sanardev.instagramapijava.model.story.Tray) {
        tray.items?.let {
            for (index in 0..it.lastIndex) {
                val item = it[index]
                if (item.takenAt <= tray.seen) {
                    tray.bundle["current_position"] = index + 1
                }
            }
            if (tray.bundle["current_position"] == null) {
                tray.bundle["current_position"] = 0
            } else
                if (tray.bundle["current_position"] as Int > it.lastIndex) {
                    tray.bundle["current_position"] = 0
                }
            Log.i(
                InstagramConstants.DEBUG_TAG,
                "tray title: " + tray.user.username + "tray current position: " + tray.bundle["current_position"]
            )
        }
    }

    fun sendStoryReaction(threadId: String, mediaId: String, reaction: String, reelId: Long) {
        mUseCase.sendStoryReaction(threadId, mediaId, reaction, reelId)
            .subscribe({
                storyReactionResult.value = Resource.success(it)
            }, {}, {})
    }

    fun replyStory(threadId: String, mediaId: String, mediaType: Int, text: String, reelId: Long) {
        mUseCase.replyStory(threadId, mediaId, mediaType, text, reelId)
            .subscribe({
                storyReactionResult.value = Resource.success(it)
            }, {}, {})
    }

    @SuppressLint("CheckResult")
    fun voteSlider(storyPosition: Int, id: String, sliderId: Long, vote: Float) {
        mUseCase.voteSlider(vote, sliderId, id)
            .subscribe({
                val tray = storyMediaLiveData.value!!.data!!
                tray.items[storyPosition] = it.story
                storyMediaLiveData.value = Resource.success(tray)
            }, {
                Log.i("TEST", "TEST")
            }, {

            })
    }

    fun markStoryAsSeen(trayId: Long, id: String, takenAt: Long) {
        mUseCase.markStoryAsSeen(trayId, id, takenAt)
            .subscribe({
                Log.i(InstagramConstants.DEBUG_TAG, "Seen Success")
            }, {
                Log.i(InstagramConstants.DEBUG_TAG, "Seen Fail")
            }, {})
    }

    fun sendStoryQuestionResponse(id: String, questionId: Long, response: String) {
        mUseCase.sendStoryQuestionResponse(id, questionId, response)
            .subscribe({
                Log.i("TEST", "TEST")
            }, {
                Log.i("TEST", "TEST")
            }, {})
    }

    fun storyQuizAnswer(mediaId: String, quizId: Long, index: Int) {
        mUseCase.storyQuizAnswer(mediaId,quizId,index)
            .subscribe({
                Log.i("TEST", "TEST")
            }, {
                Log.i("TEST", "TEST")
            }, {})
    }


}