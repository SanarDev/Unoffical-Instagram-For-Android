package com.idirect.app.utils

import android.content.Context
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.datasource.model.*
import java.util.*

class MessageGenerator {

    companion object {
        fun like(userId: Long, threadId:String,clientContext: String): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.LIKE.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.like = "❤"
                this.isDelivered = false
                this.clientContext = clientContext
                this.threadId = threadId
            }

        fun text(text: String, userId: Long, threadId:String,clientContext: String): Message =
            Message().apply {
                this.text = text
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.TEXT.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
                this.threadId = threadId
            }

        fun voiceMedia(
            context: Context,
            userId: Long,
            threadId:String,
            clientContext: String,
            localFilePath: String
        ): Message =
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
                    this.localDuration = MediaUtils.getMediaDuration(context, localFilePath)
                }
                this.threadId = threadId
            }

        fun imageMedia(userId: Long,threadId:String, clientContext: String, localFilePath: String): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.MEDIA.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
                this.threadId = threadId
                this.media = Media().apply {
                    this.isLocal = true
                    this.localFilePath = localFilePath
                    this.mediaType = 1
                }
            }

        fun videoMedia(userId: Long, threadId:String,clientContext: String, localFilePath: String): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.MEDIA.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
                this.threadId = threadId
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

        fun textLink(
            text: String,
            linkList: MutableList<String>,
            userId: Long,
            threadId:String,
            clientContext: String
        ): Message = Message().apply {
            this.link = Link().apply {
                this.text = text
                this.mutationToken = UUID.randomUUID().toString()
                this.clientContext = clientContext
                this.linkList = linkList
            }
            this.itemType = InstagramConstants.MessageType.LINK.type
            this.userId = userId
            this.timestamp = System.currentTimeMillis()
            this.isDelivered = false
            this.itemId = UUID.randomUUID().toString()
            this.clientContext = clientContext
            this.threadId = threadId
        }
    }
}