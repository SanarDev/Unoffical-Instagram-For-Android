package com.idirect.app.ui.forward

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramLoggedUser
import com.idirect.app.datasource.model.response.InstagramRecipients
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.model.login.IGLoggedUser
import com.sanardev.instagramapijava.response.IGRecipientsResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.logging.Handler
import javax.inject.Inject

class ForwardViewModel @Inject constructor(application: Application,var mHandler:android.os.Handler):BaseViewModel(application) {

    val instaClient = InstaClient.getInstanceCurrentUser(application.applicationContext)
    val recipients = MediatorLiveData<Resource<IGRecipientsResponse>>()
    var searchWord: String = ""
    private var lastSearchedWord: String = ""

    init {
        instaClient.directProcessor.getRecipient()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                recipients.value = Resource.success(it)
            },{},{})
        Thread {
            while (true) {
                mHandler.post {
                    if (searchWord != lastSearchedWord) {
                        lastSearchedWord = searchWord
                        instaClient.directProcessor.getRecipient(searchWord)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                recipients.value = Resource.success(it)
                            },{},{})
                    }
                }
                Thread.sleep(1000)
            }
        }.start()
    }
    fun getUserData(): IGLoggedUser {
        return instaClient.loggedUser
    }

    fun shareMediaTo(forwardBundle:ForwardBundle, selectedUsers: MutableList<String>) {
        if(forwardBundle.isStoryShare){
            instaClient.storyProcessor.shareStory(selectedUsers,forwardBundle.mediaId,forwardBundle.mediaType,forwardBundle.reelId).subscribe({},{},{})
        }else{
            instaClient.mediaProcessor.shareMedia(selectedUsers,forwardBundle.mediaId,forwardBundle.mediaType).subscribe({},{},{})
        }
    }
}