package com.sanardev.instagrammqtt.ui.main

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.PresenceResponse
import com.sanardev.instagrammqtt.datasource.model.event.MessageEvent
import com.sanardev.instagrammqtt.datasource.model.event.PresenceEvent
import com.sanardev.instagrammqtt.datasource.model.event.TypingEvent
import com.sanardev.instagrammqtt.datasource.model.response.InstagramDirects
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.Resource
import javax.inject.Inject

class MainViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    fun getUsername(): String {
        val user = mUseCase.getUserData()
        return user!!.username!!
    }

    fun getPassword(): String {
        val user = mUseCase.getUserData()
        return user!!.password!!
    }

    private val result = MediatorLiveData<Resource<InstagramDirects>>()
    private val resultPresence = MediatorLiveData<Resource<PresenceResponse>>()
    val mutableLiveData = MutableLiveData<Resource<InstagramDirects>>()
    val liveData = Transformations.map(result) {
        if (it.status == Resource.Status.ERROR) {
            if (it.apiError?.data != null) {
                val gson = Gson()
                val instagramInboxResult =
                    gson.fromJson(it.apiError!!.data!!.string(), InstagramDirects::class.java)
                it.data = instagramInboxResult
            }
        } else if (it.status == Resource.Status.SUCCESS) {
            mUseCase.getDirectPresence(resultPresence)
        }
        mutableLiveData.value = it
        return@map it
    }

    val liveDataPresence = Transformations.map(resultPresence) {
        return@map it
    }

    fun resetUserData() {
        mUseCase.resetUserData()
    }

    fun getDirects() {
        mUseCase.getDirectInbox(result)
    }

    fun convertTimeStampToData(lastActivityAt: Long): String {
        if (lastActivityAt.toString().length == 16) {
            return mUseCase.getDifferentTimeString(lastActivityAt / 1000, false)
        } else {
            return mUseCase.getDifferentTimeString(lastActivityAt, false)
        }
    }

    fun getUser(): InstagramLoggedUser {
        return mUseCase.getUserData()!!
    }

    fun onMessageReceive(event: MessageEvent) {
        val instagramDirect = result.value!!.data
        val threads = instagramDirect!!.inbox.threads
        for (thread in threads) {
            if (thread.threadId == event.threadId) {
                var isMessageExist = false
                for (message in thread.messages) {
                    if (message.clientContext == event.message.clientContext) {
                        isMessageExist = true
                    }
                }
                if (!isMessageExist)
                    thread.messages.add(0, event.message)
            }
        }
        mutableLiveData.value = Resource.success(instagramDirect)
    }

    fun onPresenceEvent(event: PresenceEvent) {
        val instagramDirect = result.value!!.data
        val threads = instagramDirect!!.inbox.threads
        for (thread in threads) {
            if (thread.users[0].pk.toString() == event.userId) {
                thread.lastActivityAt = event.lastActivityAtMs.toLong()
                thread.active = event.isActive
                break
            }
        }
        mutableLiveData.value = Resource.success(instagramDirect)
    }

    fun onTyping(event: TypingEvent) {
        val instagramDirect = result.value!!.data
        val threads = instagramDirect!!.inbox.threads
        for (thread in threads) {
            if (thread.threadId == event.threadId) {
                thread.typing = true
                thread.typingAtMs = System.currentTimeMillis()
                break
            }
        }
        mutableLiveData.value = Resource.success(instagramDirect)
    }

    init {
        getDirects()
        liveDataPresence.observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                val presence = it.data!!
                val direct = mutableLiveData.value!!.data!!
                val threads = direct.inbox.threads
                for (thread in threads) {
                    for (item in presence.userPresence) {
                        if (thread.users[0].pk.toString() == item.key) {
                            try {
                                thread.active = item.value["is_active"] as Boolean
                                thread.lastActivityAt =
                                    (item.value["last_activity_at_ms"] as Double).toLong()
                                break
                            }catch (e:Exception){

                            }
                        }
                    }
                }
                mutableLiveData.value = Resource.success(direct)
            }
        }
    }
}