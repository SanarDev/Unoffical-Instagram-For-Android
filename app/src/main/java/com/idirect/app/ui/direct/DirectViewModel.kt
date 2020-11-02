package com.idirect.app.ui.direct

import android.app.Application
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.DirectDate
import com.idirect.app.datasource.model.Message
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class DirectViewModel @Inject constructor(application: Application):BaseViewModel(application) {


    fun releaseMessages(it: List<com.sanardev.instagramapijava.model.direct.Message>): List<Any> {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var oldMessage: com.sanardev.instagramapijava.model.direct.Message? = null
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
                            TimeUtils.convertTimestampToDate(getApplication(), message.timestamp,true)
                        )
                    )
                }
                releasesMessage.add(message)
            } else {
                releasesMessage.add(
                    DirectDate(
                        convertToStandardTimeStamp(message.timestamp),
                        TimeUtils.convertTimestampToDate(getApplication(), message.timestamp,true)
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


}