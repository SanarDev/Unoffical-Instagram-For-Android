package com.sanardev.instagrammqtt.ui.direct

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.DirectDate
import com.sanardev.instagrammqtt.datasource.model.Message
import com.sanardev.instagrammqtt.datasource.model.Thread
import com.sanardev.instagrammqtt.datasource.model.response.InstagramChats
import com.sanardev.instagrammqtt.datasource.model.response.InstagramInbox
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.Resource
import okhttp3.ResponseBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

class DirectViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    val isEnableSendButton = ObservableField<Boolean>(false)

    private val result = MediatorLiveData<Resource<InstagramChats>>()
    val fileLiveData = MutableLiveData<File>()
    val liveData = Transformations.map(result) {
        if (it.status == Resource.Status.SUCCESS) {
            releaseMessages(it.data!!)
        }
        return@map it
    }

    private fun releaseMessages(it: InstagramChats) {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var oldMessage: Message? = null
        val messagesReverse = it.thread!!.messages.reversed()
        val releasesMessage = ArrayList<Any>().toMutableList()
        for (message in messagesReverse) {
            if (oldMessage != null) {
                val oldMessageDate = Date(oldMessage.timestamp / 1000)
                val messageDate = Date(message.timestamp / 1000)
                val oldMessageTime = sdf.format(oldMessageDate)
                val messageTime = sdf.format(messageDate)
                if (oldMessageTime != messageTime) {
                    releasesMessage.add(
                        DirectDate(
                            message.timestamp / 1000,
                            mUseCase.getDifferentTimeString(message.timestamp / 1000)
                        )
                    )
                }
                releasesMessage.add(message)
            } else {
                releasesMessage.add(
                    DirectDate(
                        message.timestamp / 1000,
                        mUseCase.getDifferentTimeString(message.timestamp / 1000)
                    )
                )
                releasesMessage.add(message)
            }
            oldMessage = message
        }
        it.thread!!.releasesMessage = releasesMessage.reversed()
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

    fun getTimeFromFloat(duration:Float): String {
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
        return String.format("%s:%s",strMin,strS)
    }

    fun getFile(url: String, id: String) {
        mUseCase.getFile(fileLiveData,url,id)
    }

}