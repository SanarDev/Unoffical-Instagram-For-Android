package com.idirect.app.ui.main

import android.app.Application
import android.content.res.Resources
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseApplication
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.*
import com.idirect.app.datasource.model.event.*
import com.idirect.app.datasource.model.response.InstagramDirects
import com.idirect.app.datasource.model.response.InstagramLoggedUser
import com.idirect.app.extensions.REGEX_FIND_URL
import com.idirect.app.extentions.dpToPx
import com.idirect.app.extentions.toStringList
import com.idirect.app.manager.PlayManager
import com.idirect.app.realtime.commands.RealTime_SendLike
import com.idirect.app.realtime.commands.RealTime_SendMessage
import com.idirect.app.realtime.service.RealTimeService
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList

class ShareViewModel @Inject constructor(application: Application, var mUseCase: UseCase,var mPlayManager: PlayManager) :
    BaseViewModel(application) {

    //audio
    private var mediaRecorder: MediaRecorder = MediaRecorder()
    private var currentVoiceFileName: String? = null

    // link
    private var mPatternRegexUrl = Pattern.compile(REGEX_FIND_URL)

    // connection state
    val connectionState = MutableLiveData<ConnectionStateEvent>()

    // current thread
    var currentThreadId: String? = null
    var currentThread: Thread? = null

    //messages
    val isSeenMessageEnable = mUseCase.isSeenMessageEnable
    val messageChange = MutableLiveData<Pair<String, Message>>()
    val threadMessageRemoved = MutableLiveData<MessageRemoveEvent>()
    val threadNewMessageLiveData = MutableLiveData<Pair<String, Message>>()

    // thread
    val threadChange = MutableLiveData<String>()
    val threadsPresence = MutableLiveData<List<String>>()

    // inbox
    val directs = ArrayList<Thread>().toMutableList()
    var instagramDirect: InstagramDirects? = null
    private val result = MediatorLiveData<Resource<InstagramDirects>>()
    val mutableLiveData = MutableLiveData<Resource<InstagramDirects>>()

    private val searchedValue = ArrayList<Thread>().toMutableList()
    private val resultPresence = MediatorLiveData<Resource<PresenceResponse>>()


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
        mutableLiveData.value = (it)
    }


    val liveDataPresence = Transformations.map(resultPresence) {
        return@map it
    }

    init {
        FirebaseMessaging.getInstance().subscribeToTopic("users")

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

    fun cancelAudioRecording() {
        currentVoiceFileName = null
        stopRecording()
    }

    fun startAudioRecording() {
        currentVoiceFileName =
            mUseCase.generateFilePath(String.format("%d_voice.m4a", System.currentTimeMillis()))
//        currentVoiceFileName =
//            mUseCase.generateFilePath(String.format("%d_voice.mp4", System.currentTimeMillis()))

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            setAudioEncodingBitRate(16 * 44100);
            setAudioSamplingRate(44100);
            setMaxDuration(60000)
            setOutputFile(currentVoiceFileName)
        }

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder.stop()
            mediaRecorder.release()
        } catch (e: Exception) {

        }
        if (currentVoiceFileName != null && File(currentVoiceFileName).exists()) {
            val clCotext = InstagramHashUtils.getClientContext()
            val message = MessageGenerator.voiceMedia(
                getApplication(),
                mUseCase.getUserData()!!.pk!!,
                clCotext,
                currentVoiceFileName!!
            )
            currentThread!!.messages.add(0, message)
            threadNewMessageLiveData.value = Pair(currentThreadId!!, message)
            val users = currentThread!!.users
            mUseCase.sendMediaVoice(
                currentThread!!.threadId,
                getUsersPk(users),
                currentVoiceFileName!!,
                "audio/mp4",
                clCotext
            ).observeForever {
                if (it.status == Resource.Status.SUCCESS) {
                    messageChange.value = Pair(currentThread!!.threadId, message.apply {
                        if (mPlayManager.currentPlayerId == this.itemId) {
                            mPlayManager.currentPlayerId = it.data!!.messageMetaDatas[0].itemId
                        }
                        isDelivered = true
                        itemId = it.data!!.messageMetaDatas[0].itemId
                        timestamp = it.data!!.messageMetaDatas[0].timestamp.toLong()
                    })
                }
            }
        }
    }

    fun sendTextMessage(threadId: String, text: String) {
        val clientContext = InstagramHashUtils.getClientContext()

//        if(mThread.messages == null){
//            EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NEED_TO_REALOD_DIRECT))
//        }
        val matcher = mPatternRegexUrl.matcher(text.toLowerCase(Locale.ROOT))
        val message = if (matcher.find()) {
            val linkList = ArrayList<String>().toMutableList()
            for (index in 0..matcher.groupCount()) {
                matcher.group(index)?.let {
                    linkList.add(it)
                }
            }
            MessageGenerator.textLink(
                text,
                linkList,
                mUseCase.getUserData()!!.pk!!,
                clientContext
            )
        } else {
            MessageGenerator.text(
                text,
                mUseCase.getUserData()!!.pk!!,
                clientContext
            )
        }
        getThreadById(threadId).messages.add(0, message)
        threadNewMessageLiveData.value = Pair(threadId, message)
        sendMessageToCloud(arrayListOf(message))

    }

    private fun sendMessageToCloud(list: MutableList<Message>) {
        if (list.isEmpty()) {
            return
        }
        val msg = list[0]
        when (msg.itemType) {
            InstagramConstants.MessageType.MEDIA.type -> {
                val users = currentThread!!.users
                if (msg.media.mediaType == 1) {
                    mUseCase.sendMediaImage(
                        currentThread!!.threadId,
                        getUsersPk(users),
                        msg.media.localFilePath,
                        msg.clientContext
                    )
                        .observeForever() {
                            if (it.status == Resource.Status.SUCCESS) {
                                changeMessageDelivery(it.data!!)
                                list.removeAt(0)
                                sendMessageToCloud(list)
                            }
                        }
                } else {
                    mUseCase.sendMediaVideo(
                        currentThread!!.threadId,
                        getUsersPk(users),
                        msg.media.localFilePath,
                        msg.clientContext
                    )
                        .observeForever {
                            if (it.status == Resource.Status.SUCCESS) {
                                changeMessageDelivery(it.data!!)
                                list.removeAt(0)
                                sendMessageToCloud(list)
                            }
                        }
                }
            }
            InstagramConstants.MessageType.LINK.type -> {
                mUseCase.sendLinkMessage(
                    msg.link.text,
                    msg.link.linkList,
                    currentThread!!.threadId,
                    msg.clientContext
                ).observeForever {
                    if (it.status == Resource.Status.SUCCESS) {
                        changeMessageDelivery(it.data!!)
                        list.removeAt(0)
                        sendMessageToCloud(list)
                    }
                }
            }
            InstagramConstants.MessageType.TEXT.type -> {
                RealTimeService.run(
                    getApplication(),
                    RealTime_SendMessage(
                        currentThread!!.threadId,
                        msg.clientContext,
                        msg.text
                    )
                )
            }
        }
    }


    private fun changeMessageDelivery(messageResponse: MessageResponse) {
        if (messageResponse.payload.threadId != currentThread!!.threadId) {
            return
        }
        for (message in currentThread!!.messages) {
            if (message.clientContext == messageResponse.payload.clientContext) {
                message.isDelivered = true
                message.timestamp = messageResponse.payload.timestamp.toLong()
                message.itemId = messageResponse.payload.itemId
                messageChange.value = Pair(messageResponse.payload.threadId, message)
            }
        }
    }


    fun addMessage(threadId: String, msg: Message) {
        val thread = getThreadById(threadId)!!
        if (thread.messages.size == 0 && thread.threadId.contains("[[")) {
            return
        }
        var isMessageExist = false
        for (message in thread.messages) {
            if (message.itemId == msg.itemId || message.clientContext == msg.clientContext) {
                if (message.itemType == InstagramConstants.MessageType.MEDIA.type) {
                    message.media = msg.media
                    message.isDelivered = true
                    message.itemId = msg.itemId
                    message.timestamp = msg.timestamp
//                    mActionListener?.onChangeMessageWithClientContext(message)
                }
                isMessageExist = true
            }
        }
        if (isMessageExist) {
            return
        }
        thread.messages.add(0, msg)
//        mActionListener?.onNewMessage(msg)
    }


    fun sendReaction(itemId: String, threadId: String, clientContext: String) {
        mUseCase.sendReaction(itemId = itemId, threadId = threadId, clientContext = clientContext)
            .observeForever {
                if (it.status == Resource.Status.SUCCESS) {
                    onReactionsResponse(it.data!!.payload)
                }
            }
    }


    fun getStandardVoiceWitdh(resources: Resources, duration: Int): Int {
        val maxDuration = 60000
        val maxWidth = (DisplayUtils.getScreenWidth() * 0.2).toInt()
        val plus = (DisplayUtils.getScreenWidth() * 0.4).toInt()
        val standardWidth = ((duration * maxWidth) / maxDuration).toFloat()
        return resources.dpToPx(standardWidth) + plus
    }

    fun uploadMedias(threadId: String = currentThreadId!!, items: List<String>) {
        if (items.isEmpty()) {
            return
        }
        val thread = getThreadById(threadId)!!
        val list = ArrayList<Message>().toMutableList()
        for (item in items) {
            val clientContext = InstagramHashUtils.getClientContext()
            var mimeType = MediaUtils.getMimeType(item) ?: "image/jpeg"
            val message = when {
                mimeType!!.contains("image") -> {
                    MessageGenerator.imageMedia(thread.viewerId, clientContext, item)
                }
                mimeType!!.contains("video") -> {
                    MessageGenerator.videoMedia(thread.viewerId, clientContext, item)
                }
                else -> {
                    MessageGenerator.imageMedia(thread.viewerId, clientContext, item)
                }
            }
            threadNewMessageLiveData.value = Pair(threadId, message)
            thread.messages.add(message)
            list.add(message)
        }
        sendMessageToCloud(list)

    }


    fun onMessageResponseEvent(event: MessageResponse) {
        try {
            val thread = getThreadById(event.payload.threadId)
            if (event.action == "item_ack" && event.status == "ok") {
                for (item in thread.messages) {
                    if (item is Message && item.clientContext == event.payload.clientContext) {
                        item.timestamp = event.payload.timestamp.toLong()
                        item.isDelivered = true
                        item.itemId = event.payload.itemId
                        if (thread.threadId != event.payload.threadId) {
                            EventBus.getDefault()
                                .postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NEED_TO_REALOD_DIRECT))
                            thread.threadId = event.payload.threadId
                        }
                        messageChange.value = Pair(thread.threadId, item)
                    }
                }
            }
        }catch (e:Exception){

        }
    }

    fun markAsSeenRavenMedia(threadId: String, itemId: String, messageClientContext: String) {
        mUseCase.markAsSeenRavenMedia(threadId, messageClientContext, itemId)
    }

    fun unsendMessage(threadId: String, itemId: String, clientContext: String) {
        mUseCase.unsendMessage(threadId, itemId, clientContext).observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                deleteMessage(threadId, itemId)
            }
        }
    }

    fun markAsSeen(threadId: String, itemId: String) {
        mUseCase.markAsSeen(threadId, itemId).observeForever {

        }
    }


    fun loadMoreItem(cursor: String, threadId: String) {
        mUseCase.loadMoreChats(cursor, threadId, instagramDirect!!.seqId).observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                getThreadById(it.data!!.thread!!.threadId).messages.addAll(it.data!!.thread!!.messages)
                mutableLiveData.value = Resource.success(instagramDirect)
            }
        }
    }


    fun getStandardWidthAndHeight(width: Int, height: Int, scale: Float = 0.7f): Array<Int> {
        var standardWidth = 0
        var standardHeight = 0
        val screenWidth = DisplayUtils.getScreenWidth()
        if ((screenWidth * scale) < width) {
            val a = width - (screenWidth * scale)
            standardWidth = (width.toDouble() - a).toInt()
            standardHeight = (height - ((height.toFloat() / width.toFloat()) * a).toInt())
        } else {
            standardWidth = width
            standardHeight = height
        }
        return arrayOf(standardWidth, standardHeight)
    }

    fun getThreadProfilePic(threadId: String = currentThreadId!!): String {
        return getThreadById(threadId)!!.users[0].profilePicUrl
    }

    fun getTimeFromTimeStamps(time: Long): String {
        val netDate = Date(time)
        val sdf = SimpleDateFormat("HH:mm")
        return sdf.format(netDate)
    }


    fun getUserProfilePic(userId: Long, threadId: String = currentThreadId!!): String? {
        val thread = getThreadById(threadId)
        for (user in thread.users) {
            if (user.pk!! == userId) {
                return user.profilePicUrl
            }
        }
        return null
    }

    fun getUsername(userId: Long, threadId: String = currentThreadId!!): String? {
        val thread = getThreadById(threadId)
        for (user in thread.users) {
            if (user.pk!! == userId) {
                return user.username
            }
        }
        return null
    }

    fun onReactionsResponse(payload: Payload) {
        val thread = getThreadById(payload.threadId)!!
        for (message in thread.messages) {
            if (message.itemId == payload.itemId) {
//                mActionListener?.onChangeMessage(
//                    MessageGenerator.addLikeReactionToMessage(
//                        message,
//                        getUserProfile().pk!!,
//                        payload.timestamp.toLong(),
//                        payload.clientContext
//                    )
//                )
            }
        }
    }


    /*


    fun getProfilePic(userId: Long): String {
        if (mThread.isGroup) {
            for (user in mThread.users) {
                if (user.pk == userId)
                    return user.profilePicUrl
            }
        } else {
            return mThread.users[0].profilePicUrl
        }
        for (user in mThread.leftUsers) {
            if (user.pk == userId)
                return user.profilePicUrl
        }
        return ""
    }

    fun getUsername(userId: Long): String {
        if (mThread.isGroup) {
            for (user in mThread.users) {
                if (user.pk == userId)
                    return user.username
            }
        } else {
            return mThread.users[0].username
        }
        for (user in mThread.leftUsers) {
            if (user.pk == userId)
                return user.username
        }
        return ""

    }

    Thread().apply {
            if (directBundle.threadId != null) {
                threadId = directBundle.threadId
            } else {
                threadId = "[[${directBundle.userId}]]"
            }
            viewerId = getUserProfile().pk!!
            active = directBundle.isActive
            isGroup = directBundle.isGroup
            threadTitle = directBundle.threadTitle
            lastActivityAt = directBundle.lastActivityAt
            users = arrayListOf(User().apply {
                this.pk = directBundle.userId
                profilePicUrl = directBundle.profileImage
            })
            if (isGroup) {
                users.add(User().apply {
                    profilePicUrl = directBundle.profileImage2
                })
            }



    fun getTimeFromFloat(duration: Float): String {
        val timeDuration = duration.roundToInt()
        val h: Int = timeDuration / 3600
        val min: Int = (timeDuration - h * 3600) / 60
        val s: Int = timeDuration - (h * 3600 + min * 60)
        var strH = if (h < 10) {
            "0$h"
        } else {
            h.toString()
        }
        var strMin = if (min < 10) {
            "0$min"
        } else {
            min.toString()
        }
        var strS = if (s < 10) {
            "0$s"
        } else {
            s.toString()
        }
        return String.format("%s:%s", strMin, strS)
    }

     */

    private fun getUsersPk(users: List<User>): String {
        var pkList = ArrayList<Long>().toMutableList()
        for (i in users.indices) {
            pkList.add(users[i].pk)
        }
        return pkList.toStringList()
    }


    fun getThreadById(threadId: String = currentThreadId!!): Thread {
        for (thread in instagramDirect!!.inbox.threads) {
            if (thread.threadId == threadId) {
                return thread
            }
        }
        return Thread()
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


    fun resetUserData() {
        mUseCase.resetUserData()
    }

    fun getDirects() {
        mUseCase.getDirectInbox(result)
    }

    fun getUser(): InstagramLoggedUser {
        return mUseCase.getUserData()!!
    }

    fun onMessageReceive(event: MessageItemEvent) {
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
        val threadUpdateList = ArrayList<String>().toMutableList()
        for (thread in threads) {
            for (user in thread.users) {
                if (user.pk.toString() == event.userId) {
                    thread.lastActivityAt = event.lastActivityAtMs.toLong()
                    thread.active = event.isActive
                    threadUpdateList.add(thread.threadId)
                    break
                }
            }
        }
        threadsPresence.value = threadUpdateList
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
        threadChange.value = event.threadId
    }

    fun reloadDirects() {
//        directs.clear()
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
        threadChange.value = event.threadId
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
            mUseCase.getMoreDirectItems(
                result,
                instagramDirect!!.seqId,
                instagramDirect!!.inbox.oldestCursor
            )
        }
    }


    fun deleteMessage(threadId: String = currentThreadId!!, itemId: String) {
        val thread = getThreadById(threadId)!!
        val iterator = thread.messages.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (value.itemId == itemId) {
                iterator.remove()
            }
        }
//        mActionListener?.removeMessage(itemId)
    }

    fun deleteMessage(event: MessageRemoveEvent) {
        instagramDirect?.let {
            val threads = it.inbox.threads
            for (thread in threads) {
                if (thread.threadId == event.threadId) {
                    for (index in thread.messages.indices) {
                        if (thread.messages[index].itemId == event.itemId) {
                            thread.messages.removeAt(index)
                            break
                        }
                    }
                }
            }
        }!!
        threadMessageRemoved.value = event
    }

    fun sendLike(threadId: String) {
        val clientContext = InstagramHashUtils.getClientContext()
        RealTimeService.run(
            getApplication(),
            RealTime_SendLike(
                threadId,
                clientContext
            )
        )
        val message = MessageGenerator.like(mUseCase.getUserData()!!.pk!!, clientContext)
        getThreadById(threadId).messages.add(0, message)
        threadNewMessageLiveData.value = Pair(threadId, message)
    }

    fun setConnectionState(connectionStateEvent: ConnectionStateEvent) {
        connectionState.value = connectionStateEvent
    }

}