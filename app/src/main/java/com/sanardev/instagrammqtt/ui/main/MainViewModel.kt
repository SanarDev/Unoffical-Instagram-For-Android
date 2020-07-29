package com.sanardev.instagrammqtt.ui.main

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.core.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.Message
import com.sanardev.instagrammqtt.datasource.model.PresenceResponse
import com.sanardev.instagrammqtt.datasource.model.Thread
import com.sanardev.instagrammqtt.datasource.model.User
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
    private var instagramDirect:InstagramDirects?=null
    private val result = MediatorLiveData<Resource<InstagramDirects>>()
    private val resultPresence = MediatorLiveData<Resource<PresenceResponse>>()
    val mutableLiveData = MutableLiveData<Resource<InstagramDirects>>()
    val threadNewMessageLiveData = MutableLiveData<Pair<String, Message>>()

    val liveData = Transformations.map(result) {
        if (it.status == Resource.Status.ERROR) {
            if (it.apiError?.data != null) {
                Log.i(InstagramConstants.DEBUG_TAG, it.apiError.message)
                val gson = Gson()
                val instagramInboxResult =
                    gson.fromJson(it.apiError!!.data!!.string(), InstagramDirects::class.java)
                it.data = instagramInboxResult
            }
        } else if (it.status == Resource.Status.SUCCESS) {
            if (directs.isEmpty()) {
                mUseCase.getDirectPresence(resultPresence)
            }
            threadValidation(it.data!!.inbox.threads)
            it.data!!.inbox.threads = directs
            instagramDirect = it.data!!
        }
        return@map it
    }.observeForever {
        mutableLiveData.postValue(it)
    }

    private fun threadValidation(threads: List<Thread>) {
        val loggedUser = getUser()
        for (thread in threads) {
            if (thread.users == null || thread.users.size == 0) {
                thread.users = ArrayList<User>().toMutableList().apply {
                    add(User().apply {
                        this.profilePicUrl = loggedUser.profilePicUrl
                        this.pk = loggedUser.pk!!
                        this.fullName = loggedUser.fullName
                        this.username = loggedUser.username
                        this.isPrivate = loggedUser.isPrivate
                    })
                }
                thread.threadTitle = loggedUser.username
            }
        }
        for (thread in threads) {
            var isThreadExist = false
            for (direct in directs) {
                if (direct.threadId == thread.threadId) {
                    isThreadExist = true
                }
            }
            if (!isThreadExist)
                directs.add(thread)
        }
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
                    directs.add(0, thread)
                }
            }
        }
        mutableLiveData.postValue(Resource.success(instagramDirect))
    }

    fun onPresenceEvent(event: PresenceEvent) {
        val threads = instagramDirect!!.inbox.threads
        for (thread in threads) {
            if (thread.users[0].pk.toString() == event.userId) {
                thread.lastActivityAt = event.lastActivityAtMs.toLong()
                thread.active = event.isActive
                break
            }
        }
        mutableLiveData.postValue(Resource.success(instagramDirect))
    }

    fun onTyping(event: TypingEvent) {
        val threads = instagramDirect!!.inbox.threads
        for (thread in threads) {
            if (thread.threadId == event.threadId) {
                thread.typing = true
                thread.typingAtMs = System.currentTimeMillis()
                break
            }
        }
        mutableLiveData.postValue(Resource.success(instagramDirect))
    }

    init {
        mUseCase.dismissAllNotification()
        getDirects()
        liveDataPresence.observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                val presence = it.data!!
                val threads = instagramDirect!!.inbox.threads
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
                mutableLiveData.postValue(Resource.success(instagramDirect!!))
            }
        }
    }

    fun reloadDirects() {
        directs.clear()
        getDirects()
    }

    fun onSearch(s: CharSequence, start: Int, before: Int, count: Int) {
        if (directs.isEmpty()) {
            return
        }
        if (s.isBlank()) {
            searchedValue.clear()
            instagramDirect!!.inbox.threads = directs
            mutableLiveData.postValue(Resource.success(instagramDirect))
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
            instagramDirect!!.inbox.threads = searchedValue
            mutableLiveData.postValue(Resource.success(instagramDirect))
        }
    }

    fun onUpdateSeenEvent(event: UpdateSeenEvent) {
        val threads = instagramDirect!!.inbox.threads
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

    fun getUsernameByUserId(threadId: String, userId: Long): String {
        val threads = instagramDirect!!.inbox.threads
        for (thread in threads) {
            if (thread.threadId == threadId) {
                if (thread.isGroup) {
                    for (user in thread.users) {
                        if (user.pk == userId)
                            return user.username
                    }
                } else {
                    return thread.users[0].username
                }
                for (user in thread.leftUsers) {
                    if (user.pk == userId)
                        return user.username
                }
            }
        }
        return ""
    }

    fun loadMoreItem() {
        if (instagramDirect!!.inbox.oldestCursor != null) {
            mUseCase.getMoreDirectItems(result, instagramDirect!!.seqId, instagramDirect!!.inbox.oldestCursor)
        }
    }
}