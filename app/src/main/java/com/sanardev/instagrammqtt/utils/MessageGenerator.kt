package com.sanardev.instagrammqtt.utils

import android.content.Context
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.*
import java.util.*

class MessageGenerator {

    companion object {
        fun like(userId: Long, clientContext: String): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.LIKE.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.like = "❤"
                this.isDelivered = false
                this.clientContext = clientContext
            }

        fun text(text: String, userId: Long, clientContext: String): Message =
            Message().apply {
                this.text = text
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.TEXT.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
            }

        fun voiceMedia(context:Context,userId: Long,clientContext: String,localFilePath:String): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.VOICE_MEDIA.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
                this.voiceMediaData = MediaData().apply {
                    this.isLocal = true
                    this.localFilePath = localFilePath
                    this.localDuration = MediaUtils.getMediaDuration(context,localFilePath)
                }
            }

        fun imageMedia(userId: Long,clientContext: String,localFilePath:String): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.MEDIA.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
                this.media = Media().apply {
                    this.isLocal = true
                    this.localFilePath = localFilePath
                    this.mediaType = 1
                }
            }
        fun videoMedia(userId: Long,clientContext: String,localFilePath:String): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.MEDIA.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
                this.media = Media().apply {
                    this.isLocal = true
                    this.localFilePath = localFilePath
                    this.mediaType = 2
                }
            }

        fun addLikeReactionToMessage(
            message: Message,
            userId: Long,
            timestamp: Long,
            clientContext: String
        ): Message {
            if (message.reactions == null) {
                message.reactions = DirectReactions().apply {
                    likes = ArrayList<DirectLikeReactions>().toMutableList()
                }
            } else {
                for (like in message.reactions.likes) {
                    if (like.senderId == userId) {
                        return message
                    }
                }
            }
            message.reactions.apply {
                likesCount += 1
                likes.apply {
                    add(DirectLikeReactions(userId, timestamp, clientContext))
                }
            }

            return message
        }


    }
}