package com.sanardev.instagrammqtt.ui.direct

import android.app.Application
import android.media.MediaRecorder
import androidx.databinding.ObservableField
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.DirectDate
import com.sanardev.instagrammqtt.datasource.model.Message
import com.sanardev.instagrammqtt.datasource.model.Payload
import com.sanardev.instagrammqtt.datasource.model.User
import com.sanardev.instagrammqtt.datasource.model.event.MessageEvent
import com.sanardev.instagrammqtt.datasource.model.response.InstagramChats
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.DisplayUtils
import com.sanardev.instagrammqtt.utils.MessageGenerator
import com.sanardev.instagrammqtt.utils.Resource
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DirectViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    private var mediaRecorder: MediaRecorder = MediaRecorder()
    private var currentVoiceFileName: String? = null
    val isEnableSendButton = ObservableField<Boolean>(false)


    private val messages = ArrayList<Message>().toMutableList()
    private var threadId: String = ""

    private val result = MediatorLiveData<Resource<InstagramChats>>()
    val fileLiveData = MutableLiveData<File>()
    val mutableLiveData = MutableLiveData<Resource<InstagramChats>>()
    val mutableLiveDataAddMessage = MutableLiveData<Message>()
    val messageChangeLiveData = MutableLiveData<Message>()
    val sendMediaLiveData = MutableLiveData<String>()

    private val liveData = Transformations.map(result) {
        if (it.status == Resource.Status.SUCCESS) {
            threadId = it.data!!.thread!!.threadId
            messages.addAll(it.data!!.thread!!.messages)
            it!!.data!!.thread!!.releasesMessage = releaseMessages(messages)
        }
        return@map it
    }.observeForever {
        mutableLiveData.postValue(it)
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
                            mUseCase.getDifferentTimeString(convertToStandardTimeStamp(message.timestamp))
                        )
                    )
                }
                releasesMessage.add(message)
            } else {
                releasesMessage.add(
                    DirectDate(
                        convertToStandardTimeStamp(message.timestamp),
                        mUseCase.getDifferentTimeString(convertToStandardTimeStamp(message.timestamp))
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

    fun init(threadId: String, seqID: Int) {
        mUseCase.getChats(result, 20, threadId, seqID)
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
        File(currentVoiceFileName).delete()
        currentVoiceFileName = null
        stopRecording()
    }

    fun startAudioRecording() {
//        currentVoiceFileName = mUseCase.generateFilePath(String.format("%d_voice.m4a", System.currentTimeMillis()))
        currentVoiceFileName =
            mUseCase.generateFilePath(String.format("%d_voice.mp4", System.currentTimeMillis()))

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
        mediaRecorder.stop()
        mediaRecorder.release()
        if (currentVoiceFileName != null) {
            val users = mutableLiveData.value!!.data!!.thread!!.users
            mUseCase.sendMediaVoice(
                threadId,
                getUsersPk(users),
                currentVoiceFileName!!,
                "audio/mp4"
            ).observeForever {
                if (it.status == Resource.Status.SUCCESS) {

                }
            }
        }
    }

    private fun getUsersPk(users: List<User>): String {
        var str = "["
        for (i in users.indices) {
            val user = users[i]
            if (i < users.size - 1) {
                str += user.pk.toString() + ","
            } else {
                str += user.pk.toString()
            }
        }
        str += "]"
        return str
    }
    /*
     var standardHeight = 0
        val screenHeight = DisplayUtils.getScreenHeight()/1.8
        if((height.toFloat() / width.toFloat()) > 1.5){
            if ((screenHeight) < height) {
                standardHeight = (height * ((screenHeight) / height)).toInt()
            } else {
                standardHeight = height.toInt()
            }
        }else {
            if ((screenHeight * 0.7) < height) {
                standardHeight = (height * ((screenHeight * 0.7) / height)).toInt()
            } else {
                standardHeight = height.toInt()
            }
        }
        return standardHeight
     */

    fun getStandardWidthAndHeight(width: Int, height: Int): Array<Int> {
        var standardWidth = 0
        var standardHeight = 0
        val screenWidth = DisplayUtils.getScreenWidth()
        if ((screenWidth * 0.7) < width) {
            val a = width - (screenWidth * 0.7)
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

    fun onMessageReceive(event: MessageEvent) {
        addMessage(event.message)
    }

    fun addMessage(msg: Message) {
        if (messages.size == 0) {
            return
        }
        var isMessageExist = false
        for (message in messages) {
            if (message.itemId == msg.itemId) {
                isMessageExist = true
            }
        }
        if (isMessageExist) {
            return
        }
        messages.add(0, msg)
        mutableLiveDataAddMessage.postValue(msg)
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
                messageChangeLiveData.value = MessageGenerator.addLikeReactionToMessage(
                    message,
                    getUserProfile().pk!!,
                    payload.timestamp.toLong(),
                    payload.clientContext
                )
            }
        }
    }

    fun markAsSeen(threadId: String, itemId: String) {
        mUseCase.markAsSeen(threadId, itemId).observeForever {

        }
    }


}