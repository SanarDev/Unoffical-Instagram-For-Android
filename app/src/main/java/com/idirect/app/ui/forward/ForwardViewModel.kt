package com.idirect.app.ui.forward

import android.app.Application
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.event.ConnectionStateEvent
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.model.login.IGLoggedUser
import com.sanardev.instagramapijava.response.IGRecipientsResponse
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class ForwardViewModel @Inject constructor(
    application: Application,
    val mUseCase: UseCase,
    var mHandler: android.os.Handler
) : BaseViewModel(application) {

    val recipients = MediatorLiveData<Resource<IGRecipientsResponse>>()
    var searchWord: String = ""
    private var lastSearchedWord: String = ""

    // for stop loop thread when share is done
    private var stopSelf:Boolean = false
    init {
        mUseCase.getRecipient()
            .subscribe({
                recipients.value = Resource.success(it)
            }, {}, {})
        Thread {
            while (true) {
                if(stopSelf){
                    return@Thread
                }
                mHandler.post {
                    if (searchWord != lastSearchedWord) {
                        lastSearchedWord = searchWord
                        mUseCase.getRecipient(searchWord)
                            .subscribe({
                                recipients.value = Resource.success(it)
                            }, {}, {})
                    }
                }
                Thread.sleep(1000)
            }
        }.start()
    }

    fun getUserData(): IGLoggedUser {
        return mUseCase.getLoggedUser()!!
    }

    fun shareMediaTo(forwardBundle: ForwardBundle, selectedUsers: MutableList<String>) {
        stopSelf = true
        for (user in selectedUsers) {
            if (forwardBundle.isStoryShare) {
                mUseCase.shareStory(
                    user,
                    forwardBundle.mediaId,
                    forwardBundle.mediaType,
                    forwardBundle.reelId
                ).subscribe({}, {}, {
                    EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NEED_TO_REALOD_DIRECT))
                })
            } else {
                mUseCase.shareMedia(
                    user,
                    forwardBundle.mediaId,
                    forwardBundle.mediaType
                ).subscribe({}, {
                    Log.i("TEST", "TEST")
                }, {
                    EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NEED_TO_REALOD_DIRECT))
                })
            }
        }
    }
}