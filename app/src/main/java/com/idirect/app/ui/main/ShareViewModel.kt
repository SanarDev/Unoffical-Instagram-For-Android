package com.idirect.app.ui.main

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.Resources
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.UploadMedia
import com.idirect.app.datasource.model.event.*
import com.idirect.app.extensions.REGEX_FIND_URL
import com.idirect.app.extentions.SizeExtention.dpToPx
import com.idirect.app.manager.PlayManager
import com.idirect.app.realtime.commands.RealTime_SendLike
import com.idirect.app.realtime.commands.RealTime_SendMessage
import com.idirect.app.realtime.service.RealTimeService
import com.idirect.app.ui.direct.DirectBundle
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.*
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.model.direct.IGThread
import com.sanardev.instagramapijava.model.direct.Message
import com.sanardev.instagramapijava.model.direct.Payload
import com.sanardev.instagramapijava.model.direct.ThreadUser
import com.sanardev.instagramapijava.model.direct.messagetype.Media
import com.sanardev.instagramapijava.model.login.IGLoggedUser
import com.sanardev.instagramapijava.response.IGDirectActionResponse
import com.sanardev.instagramapijava.response.IGDirectsResponse
import com.sanardev.instagramapijava.response.IGPresenceResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ShareViewModel @Inject constructor(
    application: Application,
    val mUseCase:UseCase,
    var mPlayManager: PlayManager
) :
    BaseViewModel(application) {

    //audio
    private var mediaRecorder: MediaRecorder = MediaRecorder()
    private var currentVoiceFileName: String? = null

    // link
    private var mPatternRegexUrl = Pattern.compile(REGEX_FIND_URL)

    // connection state
    val connectionState = MutableLiveData<ConnectionStateEvent>()

    // current thread
    var currentIGThread: IGThread? = null

    //messages
    val isSeenMessageEnable = mUseCase.isSeenMessageEnable
    val messageChange = MutableLiveData<Pair<String, Message>>()
    val threadMessageRemoved = MutableLiveData<MessageRemoveEvent>()
    val threadNewMessageLiveData = MutableLiveData<Pair<String, Message>>()

    // thread
    val threadChange = MutableLiveData<String>()
    val threadsPresence = MutableLiveData<List<String>>()

    // inbox
    val directs = ArrayList<IGThread>().toMutableList()
    var instagramDirect: IGDirectsResponse? = null
    private val result = MediatorLiveData<Resource<IGDirectsResponse>>()
    val mutableLiveData = MutableLiveData<Resource<IGDirectsResponse>>()

    private val searchedValue = ArrayList<IGThread>().toMutableList()
    private val resultPresence = MediatorLiveData<Resource<IGPresenceResponse>>()


    @SuppressLint("CheckResult")
    val liveData = Transformations.map(result) {
        if (it.status == Resource.Status.ERROR) {
            if (it.apiError?.data != null) {
                val gson = Gson()
                val instagramInboxResult =
                    gson.fromJson(it.apiError.data!!.string(), IGDirectsResponse::class.java)
                it.data = instagramInboxResult
            }
        } else if (it.status == Resource.Status.SUCCESS) {
            if (directs.isEmpty()) {
                resultPresence.value = Resource.loading()
                mUseCase.getDirectPresence()
                    .subscribe({
                        resultPresence.value = Resource.success(it)
                    }, {
                        resultPresence.value = Resource.error()
                    }, {

                    })
            }
            threadValidation(it.data!!.inbox.igThreads)
            it.data!!.inbox.igThreads = directs
            instagramDirect = it.data!!
        }
        return@map it
    }.observeForever {
        mutableLiveData.value = it
    }

    init {
        FirebaseMessaging.getInstance().subscribeToTopic("users")
        mUseCase.dismissAllNotification()
        getDirects()
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
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncodingBitRate(16 * 44100)
            setAudioSamplingRate(44100)
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

    @SuppressLint("CheckResult")
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
                mUseCase.getLoggedUser()!!.pk,
                currentIGThread!!.threadId,
                clCotext,
                currentVoiceFileName!!
            )
            currentIGThread!!.messages.add(0, message)
            threadNewMessageLiveData.value = Pair(currentIGThread!!.threadId!!, message)
            val users = currentIGThread!!.users
            mUseCase.sendMediaVoice(
                currentIGThread!!.threadId,
                getUsersPk(users),
                currentVoiceFileName!!,
                clCotext
            )
                .subscribe({
                    messageChange.value = Pair(currentIGThread!!.threadId, message.apply {
                        if (mPlayManager.currentPlayerId == this.itemId) {
                            mPlayManager.currentPlayerId = it.messageMetaData[0].itemId
                        }
                        isDelivered = true
                        itemId = it.messageMetaData[0].itemId
                        timestamp = it.messageMetaData[0].timestamp.toLong()
                    })
                }, {
                    Log.i("TEST","TEST")
                }, {})
        }
    }

    fun sendTextMessage(threadIds:List<String>,text: String){
        for(thread in threadIds){
            sendTextMessage(thread,text)
        }
    }
    fun sendTextMessage(threadId: String, text: String) {
        val clientContext = InstagramHashUtils.getClientContext()

//        if(mThread.messages == null){
//            EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NEED_TO_REALOD_DIRECT))
//        }
        val matcher = mPatternRegexUrl.matcher(text.lowercase(Locale.ROOT))
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
                mUseCase.getLoggedUser()!!.pk,
                threadId,
                clientContext
            )
        } else {
            MessageGenerator.text(
                text,
                mUseCase.getLoggedUser()!!.pk,
                threadId,
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
                val users = currentIGThread!!.users
                if (msg.media.mediaType == 1) {
                    mUseCase.sendMediaImage(
                        msg.bundle["threadId"] as String,
                        getUsersPk(users),
                        msg.media.bundle["localFilePath"] as String,
                        msg.clientContext
                    ).observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            changeMessageDelivery(it)
                            list.removeAt(0)
                            sendMessageToCloud(list)
                        }, {
                        }, {})
                } else {
                   mUseCase.sendMediaVideo(
                        msg.bundle["threadId"] as String,
                        getUsersPk(users),
                        msg.media.bundle["localFilePath"] as String,
                        msg.clientContext
                    ).subscribe({
                            changeMessageDelivery(it)
                            list.removeAt(0)
                            sendMessageToCloud(list)
                        },{
                        },{})
                }
            }
            InstagramConstants.MessageType.LINK.type -> {
//                mUseCase.sendLinkMessage(
//                    msg.link.text,
//                    msg.link.linkList,
//                    msg.threadId,
//                    msg.clientContext
//                ).observeForever {
//                    if (it.status == Resource.Status.SUCCESS) {
//                        changeMessageDelivery(it.data!!)
//                        list.removeAt(0)
//                        sendMessageToCloud(list)
//                    }
//                }
            }
            InstagramConstants.MessageType.TEXT.type -> {
                RealTimeService.run(
                    getApplication(),
                    RealTime_SendMessage(
                        msg.bundle["threadId"] as String,
                        msg.clientContext,
                        msg.text
                    )
                )
            }
        }
    }


    private fun changeMessageDelivery(messageResponse: IGDirectActionResponse) {
        if (messageResponse.payload.threadId != currentIGThread!!.threadId) {
            return
        }
        for (message in currentIGThread!!.messages) {
            if (message.clientContext == messageResponse.payload.clientContext) {
                message.isDelivered = true
                message.timestamp = messageResponse.payload.timestamp.toLong()
                message.itemId = messageResponse.payload.itemId
                messageChange.value = Pair(messageResponse.payload.threadId, message as Message)
            }
        }
    }


    fun addMessage(threadId: String, msg: Message) {
        val thread = getThreadById(threadId)
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
        mUseCase.sendLikeReaction(itemId,threadId,clientContext)
            .subscribe({
                onReactionsResponse(it.payload)
            },{},{})
    }


    fun getStandardVoiceWitdh(resources: Resources, duration: Int): Int {
        val maxDuration = 60000
        val maxWidth = (DisplayUtils.getScreenWidth() * 0.2).toInt()
        val plus = (DisplayUtils.getScreenWidth() * 0.4).toInt()
        val standardWidth = ((duration * maxWidth) / maxDuration).toFloat()
        return resources.dpToPx(standardWidth) + plus
    }

    fun generateUploadMediaModelFromPath(
        threadId: String,
        paths: List<String>
    ): MutableList<UploadMedia> {
        val uploadMedias = ArrayList<UploadMedia>().toMutableList()
        val user = mUseCase.getLoggedUser()
        for (item in paths) {
            val clientContext = InstagramHashUtils.getClientContext()
            uploadMedias.add(UploadMedia().apply {
                this.clientContext = clientContext
                this.localPath = item
                this.threadId = threadId
                this.senderId = user!!.pk
            })
        }
        return uploadMedias
    }

    fun uploadMedias(items: List<UploadMedia>) {
        if (items.isEmpty()) {
            return
        }
        val list = ArrayList<Message>().toMutableList()
        for (item in items) {
            var mimeType = MediaUtils.getMimeType(item.localPath) ?: "image/jpeg"
            val message = when {
                mimeType.contains("image") -> {
                    MessageGenerator.imageMedia(
                        item.senderId,
                        item.threadId,
                        item.clientContext,
                        item.localPath
                    )
                }
                mimeType.contains("video") -> {
                    MessageGenerator.videoMedia(
                        item.senderId,
                        item.threadId,
                        item.clientContext,
                        item.localPath
                    )
                }
                else -> {
                    MessageGenerator.imageMedia(
                        item.senderId,
                        item.threadId,
                        item.clientContext,
                        item.localPath
                    )
                }
            }
            val thread = getThreadById(item.threadId)
            threadNewMessageLiveData.value = Pair(thread.threadId, message)
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
        } catch (e: Exception) {

        }
    }

    fun markAsSeenRavenMedia(threadId: String, itemId: String, messageClientContext: String) {
        mUseCase.markAsSeenRavenMedia(threadId, itemId,messageClientContext).subscribe()
    }

    @SuppressLint("CheckResult")
    fun unsendMessage(threadId: String, itemId: String, clientContext: String) {
        mUseCase.unsendMessage(threadId, itemId, clientContext)
            .subscribe({
                deleteMessage(MessageRemoveEvent(threadId, itemId))
            },{
                Log.i("TEST","TEST")
            },{})
    }

    fun markAsSeen(threadId: String, itemId: String) {
        mUseCase.markAsSeenMessage(threadId, itemId).subscribe({},{},{})
    }

    fun loadMoreItem(cursor: String, threadId: String) {
        mUseCase.getDirectMoreChats(threadId, instagramDirect!!.seqId,cursor)
            .subscribe({
                getThreadById(it.igThread.threadId).messages.addAll(it.igThread.messages)
                mutableLiveData.value = Resource.success(instagramDirect)
            },{},{})
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

    fun getThreadProfilePic(threadId: String = currentIGThread!!.threadId!!): String {
        return getThreadById(threadId).users[0].profilePicUrl
    }

    fun getTimeFromTimeStamps(time: Long): String {
        val netDate = Date(time)
        val sdf = SimpleDateFormat("HH:mm")
        return sdf.format(netDate)
    }


    fun getUserProfilePic(userId: Long, threadId: String = currentIGThread!!.threadId!!): String? {
        val thread = getThreadById(threadId)
        for (user in thread.users) {
            if (user.pk == userId) {
                return user.profilePicUrl
            }
        }
        return null
    }

    fun getUsername(userId: Long, threadId: String = currentIGThread!!.threadId!!): String? {
        val thread = getThreadById(threadId)
        for (user in thread.users) {
            if (user.pk == userId) {
                return user.username
            }
        }
        return null
    }

    fun onReactionsResponse(payload: Payload) {
        val thread = getThreadById(payload.threadId)
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

    private fun getUsersPk(users: List<ThreadUser>): List<Long> {
        var pkList = ArrayList<Long>().toMutableList()
        for (i in users.indices) {
            pkList.add(users[i].pk)
        }
        return pkList
    }


    fun getThreadById(threadId: String = currentIGThread!!.threadId!!): IGThread {
        if (currentIGThread != null && threadId == currentIGThread!!.threadId) {
            return currentIGThread!!
        }
        for (thread in instagramDirect!!.inbox.igThreads) {
            if (thread.threadId == threadId) {
                return thread
            }
        }
        return IGThread()
    }

    private fun threadValidation(IGThreads: List<IGThread>) {
        val loggedUser = getUser()
        for (thread in IGThreads) {
            if (thread.users == null || thread.users.size == 0) {
                thread.users = ArrayList<ThreadUser>().toMutableList().apply {
                    add(ThreadUser().apply {
                        this.profilePicUrl = loggedUser.profilePicUrl
                        this.pk = loggedUser.pk
                        this.fullName = loggedUser.fullName
                        this.username = loggedUser.username
                        this.isPrivate = loggedUser.isPrivate.toString()
                    })
                }
                thread.threadTitle = loggedUser.username
            }
            thread.bundle = HashMap<Any,Any>().apply {
                put("active",false)
                put("typing",false)
            }
        }
        for (thread in IGThreads) {
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


    // call then need to remove user login data
    fun resetUserData() {
        mUseCase.logout().subscribe()
    }

    fun getDirects() {
        mUseCase
            .getDirectInbox(20,20)
            .subscribe({
                result.value = Resource.success(it)
            },{},{})
    }

    fun getUser(): IGLoggedUser {
        return mUseCase.getLoggedUser()!!
    }

    fun onMessageReceive(event: MessageItemEvent) {
        val threads = instagramDirect!!.inbox.igThreads
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
        val threads = instagramDirect!!.inbox.igThreads
        val threadUpdateList = ArrayList<String>().toMutableList()
        for (thread in threads) {
            for (user in thread.users) {
                if (user.pk.toString() == event.userId) {
                    thread.lastActivityAt = event.lastActivityAtMs.toLong()
                    thread.bundle["active"] = event.isActive
                    threadUpdateList.add(thread.threadId)
                    break
                }
            }
        }
        threadsPresence.value = threadUpdateList
    }

    fun onTyping(event: TypingEvent) {
        val threads = instagramDirect!!.inbox.igThreads
        for (thread in threads) {
            if (thread.threadId == event.threadId) {
                thread.bundle["typing"] = true
                thread.bundle["typingAtMs"] = System.currentTimeMillis()
                break
            }
        }
        threadChange.value = event.threadId
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
            instagramDirect!!.inbox.igThreads = directs
            mutableLiveData.postValue(Resource.success(instagramDirect))
        } else {
            searchedValue.clear()
            for (thread in directs) {
                if (!thread.group && (thread.users[0].fullName.contains(s) || thread.users[0].username.contains(
                        s
                    ))
                ) {
                    searchedValue.add(thread)
                }
            }
            instagramDirect!!.inbox.igThreads = searchedValue
            mutableLiveData.postValue(Resource.success(instagramDirect))
        }
    }

    fun onUpdateSeenEvent(event: UpdateSeenEvent) {
        val threads = instagramDirect!!.inbox.igThreads
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
        val threads = instagramDirect!!.inbox.igThreads
        for (thread in threads) {
            if (thread.threadId == threadId) {
                if (thread.group) {
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
            mUseCase.getMoreDirectInbox(
                instagramDirect!!.seqId,
                instagramDirect!!.inbox.oldestCursor
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    result.value = Resource.success(it)
                },{},{})
        }
    }


    fun deleteMessage(event: MessageRemoveEvent) {
        // this thread if != null will must be remove
        var threadRemovable:IGThread?=null

        instagramDirect?.let {
            val threads = it.inbox.igThreads
            for (thread in threads) {
                if (thread.threadId == event.threadId) {
                    for (index in thread.messages.indices) {
                        if (thread.messages[index].itemId == event.itemId) {
                            thread.messages.removeAt(index)
                            if(thread.messages.size == 0){
                                threadRemovable = thread
                            }
                            break
                        }
                    }
                }
            }
            if(threadRemovable != null){
                it.inbox.igThreads.remove(threadRemovable)
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
        val message = MessageGenerator.like(getUser().pk, threadId, clientContext)
        getThreadById(threadId).messages.add(0, message)
        threadNewMessageLiveData.value = Pair(threadId, message)
    }

    fun setConnectionState(connectionStateEvent: ConnectionStateEvent) {
        connectionState.value = connectionStateEvent
    }

    fun getThreadIdByUserId(pk: Long): String {
        for (thread in instagramDirect!!.inbox.igThreads) {
            if (!thread.group && thread.users[0].pk == pk) {
                return thread.threadId
            }
        }
        return "[[$pk]]"
    }

    @SuppressLint("CheckResult")
    fun getThreadByUserId(directBundle: DirectBundle): LiveData<Resource<IGThread>> {
        val result = MutableLiveData<Resource<IGThread>>()
        for (thread in instagramDirect!!.inbox.igThreads) {
            if (!thread.group && thread.users[0].pk == directBundle.userId) {
                result.value = Resource.success(thread)
            }
        }
        //#comment_code
        mUseCase.getThreadByParticipants(directBundle.userId, instagramDirect!!.seqId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.thread == null){
                    result.value = Resource.success(mUseCase.createFakeThread(directBundle.userId,directBundle.threadTitle,directBundle.profileImage))
                }else{
                    result.value = Resource.success(it.thread)
                }
            },{
                Log.i("TEST","TEST")
            },{})
        return result
    }

}