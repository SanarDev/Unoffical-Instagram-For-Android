package com.sanardev.instagrammqtt.ui.main

import android.app.Application
import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.Message
import com.sanardev.instagrammqtt.datasource.model.PresenceResponse
import com.sanardev.instagrammqtt.datasource.model.Thread
import com.sanardev.instagrammqtt.datasource.model.event.MessageEvent
import com.sanardev.instagrammqtt.datasource.model.event.PresenceEvent
import com.sanardev.instagrammqtt.datasource.model.event.TypingEvent
import com.sanardev.instagrammqtt.datasource.model.event.UpdateSeenEvent
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

    private val directs = ArrayList<Thread>().toMutableList()
    private val searchedValue = ArrayList<Thread>().toMutableList()
    private val result = MediatorLiveData<Resource<InstagramDirects>>()
    private val resultPresence = MediatorLiveData<Resource<PresenceResponse>>()
    val mutableLiveData = MutableLiveData<Resource<InstagramDirects>>()
    val threadNewMessageLiveData = MutableLiveData<Pair<String, Message>>()

    val liveData = Transformations.map(result) {
        if (it.status == Resource.Status.ERROR) {
            if (it.apiError?.data != null) {
                val gson = Gson()
                val instagramInboxResult =
                    gson.fromJson(it.apiError!!.data!!.string(), InstagramDirects::class.java)
                it.data = instagramInboxResult
            }
        } else if (it.status == Resource.Status.SUCCESS) {
            if (directs.isEmpty()) {
                mUseCase.getDirectPresence(resultPresence)
            }
            directs.addAll(it.data!!.inbox.threads)
            it.data!!.inbox.threads = directs
        }
        return@map it
    }.observeForever {
        mutableLiveData.value = it
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

    fun getUser(): InstagramLoggedUser {
        return mUseCase.getUserData()!!
    }

    fun onMessageReceive(event: MessageEvent) {
        val instagramDirect = result.value!!.data
        val threads = instagramDirect!!.inbox.threads
        for (index in threads.indices) {
            val thread = threads[index]
            if (thread.threadId == event.threadId) {
                var isMessageExist = false
                for (message in thread.messages) {
                    if (message.itemId == event.message.itemId) {
                        isMessageExist = true
                    }
                }
                if (!isMessageExist) {
                    thread.messages.add(0, event.message)
                    directs.removeAt(index)
                    directs.add(0,thread)
                }
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
                            } catch (e: Exception) {

                            }
                        }
                    }
                }
                mutableLiveData.value = Resource.success(direct)
            }
        }
    }
    fun reloadDirects(){
        directs.clear()
        getDirects()
    }

    fun onSearch(s: CharSequence, start: Int, before: Int, count: Int) {
        if (directs.isEmpty()) {
            return
        }
        if (s.isBlank()) {
            searchedValue.clear()
            val instagramDirect = mutableLiveData.value!!.data!!
            instagramDirect.inbox.threads = directs
            mutableLiveData.value = Resource.success(instagramDirect)
        } else {
            searchedValue.clear()
            for (thread in directs) {
                if (!thread.isGroup && (thread.users[0].fullName.contains(s) || thread.users[0].username.contains(
                        s
                    ))
                ) {
                    searchedValue.add(thread)
                }
            }
            val instagramDirect = mutableLiveData.value!!.data!!
            instagramDirect.inbox.threads = searchedValue
            mutableLiveData.value = Resource.success(instagramDirect)
        }
    }

    fun onUpdateSeenEvent(event: UpdateSeenEvent) {
        val instagramDirect = mutableLiveData.value!!.data!!
        val threads = instagramDirect.inbox.threads
        for (thread in threads) {
            if (thread.threadId == event.threadId) {
                for (item in thread.lastSeenAt.entries) {
                    item.value.timeStamp =
                        event.seen.timeStamp
                    item.value.itemId = event.seen.itemId
                }
            }
        }
        mutableLiveData.value = Resource.success(instagramDirect)
    }
}