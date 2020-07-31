package com.sanardev.instagrammqtt.ui.direct

import android.app.Application
import android.content.res.Resources
import android.media.MediaRecorder
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.core.BaseApplication
import com.sanardev.instagrammqtt.core.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.*
import com.sanardev.instagrammqtt.datasource.model.event.ConnectionStateEvent
import com.sanardev.instagrammqtt.datasource.model.event.MessageItemEvent
import com.sanardev.instagrammqtt.datasource.model.event.MessageResponse
import com.sanardev.instagrammqtt.datasource.model.response.InstagramChats
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.extensions.REGEX_FIND_URL
import com.sanardev.instagrammqtt.extentions.dpToPx
import com.sanardev.instagrammqtt.extentions.toStringList
import com.sanardev.instagrammqtt.realtime.commands.RealTime_SendMessage
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DirectViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    private var mediaRecorder: MediaRecorder = MediaRecorder()
    private var currentVoiceFileName: String? = null
    val isEnableSendButton = ObservableField<Boolean>(false)
    var isSeenMessageEnable = mUseCase.isSeenMessageEnable


    val messages = ArrayList<Message>().toMutableList()
    lateinit var mThread: Thread
    var seqId: Int = 0

    private val result = MediatorLiveData<Resource<InstagramChats>>()
    val fileLiveData = MutableLiveData<File>()
    val mutableLiveData = MutableLiveData<Resource<InstagramChats>>()
    var mActionListener: ActionListener? = null
    val sendMediaLiveData = MutableLiveData<String>()
    val messageText = ObservableField<String>()
    private var mPatternRegexUrl = Pattern.compile(REGEX_FIND_URL)

    private val liveData = Transformations.map(result) {
        if (it.status == Resource.Status.SUCCESS) {
            mThread = it.data!!.thread!!.apply {
                this.active = mThread.active
                this.lastActivityAt = mThread.lastActivityAt
            }
            messages.addAll(it.data!!.thread!!.messages)
            it!!.data!!.thread!!.releasesMessage = releaseMessages(messages)
        }
        return@map it
    }.observeForever {
        mutableLiveData.postValue(it)
    }

    fun init(directBundle: DirectBundle) {
        if (directBundle.threadId != null) {
            mUseCase.getChats(result, 20, directBundle.threadId, directBundle.seqId)
        }
        this.mThread = Thread().apply {
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
        }
        seqId = directBundle.seqId
    }

    private fun releaseMessages(it: List<Message>): List<Any> {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var oldMessage: Message? = null
        val messagesReverse = it.reversed()
        val releasesMessage = ArrayList<Any>().toMutableList()
        for (message in messagesReverse) {
            if (oldMessage != null) {
                val oldMessageDate = Date(convertToStandardTimeStamp(oldMessage.timestamp))
                val messageDate = Date(convertToStandardTimeStamp(message.timestamp))
                val oldMessageTime = sdf.format(oldMessageDate)
                val messageTime = sdf.format(messageDate)
                if (oldMessageTime != messageTime) {
                    releasesMessage.add(
                        DirectDate(
                            convertToStandardTimeStamp(message.timestamp),
                            TimeUtils.convertTimestampToDate(getApplication(), message.timestamp)
                        )
                    )
                }
                releasesMessage.add(message)
            } else {
                releasesMessage.add(
                    DirectDate(
                        convertToStandardTimeStamp(message.timestamp),
                        TimeUtils.convertTimestampToDate(getApplication(), message.timestamp)
                    )
                )
                releasesMessage.add(message)
            }
            oldMessage = message
        }
        return releasesMessage.reversed()
    }

    fun convertToStandardTimeStamp(timeStamp: Long): Long {
        return if (timeStamp.toString().length == 16)
            timeStamp / 1000
        else
            timeStamp
    }

    fun edtMessageChange(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isBlank()) {
            isEnableSendButton.set(false)
        } else {
            isEnableSendButton.set(true)
        }

    }

    fun getUserProfile(): InstagramLoggedUser {
        return mUseCase.getUserData()!!
    }

    fun getTimeFromTimeStamps(time: Long): String {
        val netDate = Date(time)
        val sdf = SimpleDateFormat("HH:mm")
        return sdf.format(netDate)
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

    fun getFile(url: String, id: String) {
        mUseCase.getFile(fileLiveData, url, id)
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
                mThread.viewerId,
                clCotext,
                currentVoiceFileName!!
            )
            messages.add(0, message)
            mActionListener?.onNewMessage(message)
            val users = mutableLiveData.value!!.data!!.thread!!.users
            mUseCase.sendMediaVoice(
                mThread.threadId,
                getUsersPk(users),
                currentVoiceFileName!!,
                "audio/mp4",
                clCotext
            ).observeForever {
                if (it.status == Resource.Status.SUCCESS) {
                    mActionListener?.onChangeMessageWithClientContext(message.apply {
                        if (BaseApplication.currentPlayerId == this.itemId) {
                            BaseApplication.currentPlayerId = it.data!!.messageMetaDatas[0].itemId
                        }
                        isDelivered = true
                        itemId = it.data!!.messageMetaDatas[0].itemId
                        timestamp = it.data!!.messageMetaDatas[0].timestamp.toLong()
                    })
                }
            }
        }
    }

    private fun getUsersPk(users: List<User>): String {
        var pkList = ArrayList<Long>().toMutableList()
        for (i in users.indices) {
            pkList.add(users[i].pk)
        }
        return pkList.toStringList()
    }

    fun onSendMessageClick(v: View) {
        val clientContext = InstagramHashUtils.getClientContext()
        mActionListener?.realTimeCommand(
            RealTime_SendMessage(
                mThread.threadId,
                clientContext,
                messageText.get()
            )
        )
        val matcher = mPatternRegexUrl.matcher(messageText.get()!!.toLowerCase(Locale.ROOT))
        val message = if (matcher.find()) {
            val linkList = ArrayList<String>().toMutableList()
            for (index in 0..matcher.groupCount()) {
                matcher.group(index)?.let {
                    linkList.add(it)
                }
            }
            MessageGenerator.textLink(
                messageText.get()!!,
                linkList,
                mThread.viewerId,
                clientContext
            )
        } else {
            MessageGenerator.text(
                messageText.get()!!,
                mThread.viewerId,
                clientContext
            )
        }
        sendMessageToCloud(arrayListOf(message))
        EventBus.getDefault()
            .postSticky(arrayListOf(
                MessageItemEvent(
                    mThread.threadId,
                    message
                )
            ))
        messageText.set("")
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
    /*
    var standardWidth = 0
        val screenWidth = DisplayUtils.getScreenWidth()
        if((screenWidth * 0.7) < width){
            standardWidth =  (width * ((screenWidth * 0.7) / width)).toInt()
        }else{
            standardWidth = width
        }
        return standardWidth
     */

    fun loadMoreItem(cursor: String, threadId: String, seqId: Int) {
        mUseCase.loadMoreChats(result, cursor, threadId, seqId)
    }

    fun onMessageReceive(event: MessageItemEvent) {
        addMessage(event.message)
    }

    private fun changeMessageDelivery(messageResponse: MessageResponse) {
        if (messageResponse.payload.threadId != mThread.threadId) {
            return
        }
        for (message in messages) {
            if (message.clientContext == messageResponse.payload.clientContext) {
                message.isDelivered = true
                message.timestamp = messageResponse.payload.timestamp.toLong()
                message.itemId = messageResponse.payload.itemId
                mActionListener?.onChangeMessageWithClientContext(message)
            }
        }
    }

    fun addMessage(msg: Message) {
        if (messages.size == 0 && !mThread.threadId.contains("[[")) {
            return
        }
        var isMessageExist = false
        for (message in messages) {
            if (message.itemId == msg.itemId || message.clientContext == msg.clientContext) {
                if (message.itemType == InstagramConstants.MessageType.MEDIA.type) {
                    message.media = msg.media
                    message.isDelivered = true
                    message.itemId = msg.itemId
                    message.timestamp = msg.timestamp
                    mActionListener?.onChangeMessageWithClientContext(message)
                }
                isMessageExist = true
            }
        }
        if (isMessageExist) {
            return
        }
        messages.add(0, msg)
        mActionListener?.onNewMessage(msg)
    }

    fun sendReaction(itemId: String, threadId: String, clientContext: String) {
        mUseCase.sendReaction(itemId = itemId, threadId = threadId, clientContext = clientContext)
            .observeForever {
                if (it.status == Resource.Status.SUCCESS) {
                    onReactionsResponse(it.data!!.payload)
                }
            }
    }

    fun onReactionsResponse(payload: Payload) {
        for (message in messages) {
            if (message.itemId == payload.itemId) {
                mActionListener?.onChangeMessage(
                    MessageGenerator.addLikeReactionToMessage(
                        message,
                        getUserProfile().pk!!,
                        payload.timestamp.toLong(),
                        payload.clientContext
                    )
                )
            }
        }
    }

    fun markAsSeen(threadId: String, itemId: String) {
        mUseCase.markAsSeen(threadId, itemId).observeForever {

        }
    }

    fun getStandardVoiceWitdh(resources: Resources, duration: Int): Int {
        val maxDuration = 60000
        val maxWidth = (DisplayUtils.getScreenWidth() * 0.2).toInt()
        val plus = (DisplayUtils.getScreenWidth() * 0.4).toInt()
        val standardWidth = ((duration * maxWidth) / maxDuration).toFloat()
        return resources.dpToPx(standardWidth) + plus
    }

    fun uploadMedias(items: List<String>) {
        if (items.isEmpty()) {
            return
        }
        val list = ArrayList<Message>().toMutableList()
        for (item in items) {
            val clientContext = InstagramHashUtils.getClientContext()
            var mimeType = MediaUtils.getMimeType(item) ?: "image/jpeg"
            val message = when {
                mimeType!!.contains("image") -> {
                    MessageGenerator.imageMedia(mThread.viewerId, clientContext, item)
                }
                mimeType!!.contains("video") -> {
                    MessageGenerator.videoMedia(mThread.viewerId, clientContext, item)
                }
                else -> {
                    MessageGenerator.imageMedia(mThread.viewerId, clientContext, item)
                }
            }
            mActionListener?.onNewMessage(message)
            messages.add(message)
            list.add(message)
        }
        sendMessageToCloud(list)

    }


    private fun sendMessageToCloud(list: MutableList<Message>) {
        if (list.isEmpty()) {
            return
        }
        val msg = list[0]
        when (msg.itemType) {
            InstagramConstants.MessageType.MEDIA.type -> {
                val users = mThread.users
                if (msg.media.mediaType == 1) {
                    mUseCase.sendMediaImage(
                        mThread.threadId,
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
                        mThread.threadId,
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
                    mThread.threadId,
                    msg.clientContext
                ).observeForever {
                    if (it.status == Resource.Status.SUCCESS) {
                        changeMessageDelivery(it.data!!)
                        list.removeAt(0)
                        sendMessageToCloud(list)
                    }
                }
            }
        }
    }

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

    fun onMessageResponseEvent(event: MessageResponse) {
        if (event.action == "item_ack" && event.status == "ok") {
            for (item in messages) {
                if (item is Message && item.clientContext == event.payload.clientContext) {
                    item.timestamp = event.payload.timestamp.toLong()
                    item.isDelivered = true
                    item.itemId = event.payload.itemId
                    if (mThread.threadId != event.payload.threadId) {
                        EventBus.getDefault()
                            .postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NEED_TO_REALOD_DIRECT))
                        mThread.threadId = event.payload.threadId
                    }
                    mActionListener?.onChangeMessageWithClientContext(item)
                }
            }
        }
    }

    fun markAsSeenRavenMedia(itemId: String, messageClientContext: String) {
        mUseCase.markAsSeenRavenMedia(mThread.threadId, messageClientContext, itemId)
    }

    fun unsendMessage(itemId: String, clientContext: String) {
        mUseCase.unsendMessage(mThread.threadId, itemId, clientContext).observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                deleteMessage(itemId)
            }
        }
    }

    fun deleteMessage(itemId: String) {
        val iterator = messages.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (value.itemId == itemId) {
                iterator.remove()
            }
        }
        mActionListener?.removeMessage(itemId)
    }

}