package com.idirect.app.ui.forward

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramLoggedUser
import com.idirect.app.datasource.model.response.InstagramRecipients
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import java.util.logging.Handler
import javax.inject.Inject

class ForwardViewModel @Inject constructor(application: Application,var mUseCase: UseCase,var mHandler:android.os.Handler):BaseViewModel(application) {

    val recipients = MediatorLiveData<Resource<InstagramRecipients>>()
    var searchWord: String = ""
    private var lastSearchedWord: String = ""

    init {
        mUseCase.getRecipients(recipients)
        Thread {
            while (true) {
                mHandler.post {
                    if (searchWord != lastSearchedWord) {
                        lastSearchedWord = searchWord
                        mUseCase.getRecipients(recipients, searchWord)
                    }
                }
                Thread.sleep(1000)
            }
        }.start()
    }
    fun getUserData(): InstagramLoggedUser {
        return mUseCase.getUserData()!!
    }

    fun shareMediaTo(forwardBundle:ForwardBundle, selectedUsers: MutableList<String>) {
        if(forwardBundle.isStoryShare){
            mUseCase.shareStory(forwardBundle.mediaId,forwardBundle.mediaType,forwardBundle.reelId,selectedUsers)
        }else{
            mUseCase.shareMedia(forwardBundle.mediaId,forwardBundle.mediaType,selectedUsers)
        }
    }
}