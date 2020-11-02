package com.idirect.app.utils

import android.content.Context
import com.idirect.app.constants.InstagramConstants
import com.sanardev.instagramapijava.model.direct.Message
import com.sanardev.instagramapijava.model.direct.messagetype.*
import java.util.*
import kotlin.collections.HashMap

class MessageGenerator {

    companion object {
        fun like(userId: Long, threadId: String, clientContext: String): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.LIKE.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.like = "❤"
                this.isDelivered = false
                this.clientContext = clientContext
                this.bundle = HashMap<Any, Any>().apply {
                    put("threadId", threadId)
                }
            }

        fun text(text: String, userId: Long, threadId: String, clientContext: String): Message =
            Message().apply {
                this.text = text
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.TEXT.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
                this.bundle = HashMap<Any, Any>().apply {
                    put("threadId", threadId)
                }
            }

        fun voiceMedia(
            context: Context,
            userId: Long,
            threadId: String,
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
                    this.bundle =  HashMap<Any,Any>().apply {
                        put("isLocal", true)
                        put("localFilePath", localFilePath)
                        put("localDuration", MediaUtils.getMediaDuration(context, localFilePath))
                    }
                }
                this.bundle = HashMap<Any, Any>().apply {
                    put("threadId", threadId)
                }
            }

        fun imageMedia(
            userId: Long,
            threadId: String,
            clientContext: String,
            localFilePath: String
        ): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.MEDIA.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
                this.bundle = HashMap<Any, Any>().apply {
                    put("threadId", threadId)
                }
                this.media = Media().also {
                    it.mediaType = 1
                    it.bundle = HashMap<Any, Any>().apply {
                        put("isLocal", true)
                        put("localFilePath", localFilePath)
                    }
                }
            }

        fun videoMedia(
            userId: Long,
            threadId: String,
            clientContext: String,
            localFilePath: String
        ): Message =
            Message().apply {
                this.text = ""
                this.timestamp = System.currentTimeMillis()
                this.itemType = InstagramConstants.MessageType.MEDIA.type
                this.userId = userId
                this.itemId = UUID.randomUUID().toString()
                this.isDelivered = false
                this.clientContext = clientContext
                this.bundle = HashMap<Any, Any>().apply {
                    put("threadId", threadId)
                }
                this.media = Media().also {
                    it.mediaType = 2
                    it.bundle = HashMap<Any, Any>().apply {
                        put("isLocal", true)
                        put("localFilePath", localFilePath)
                    }
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
            threadId: String,
            clientContext: String
        ): Message = Message().apply {
            this.link = Link().apply {
                this.text = text
                this.mutationToken = UUID.randomUUID().toString()
                this.clientContext = clientContext
                //#comment_code
//                this.linkList = linkList
            }
            this.itemType = InstagramConstants.MessageType.LINK.type
            this.userId = userId
            this.timestamp = System.currentTimeMillis()
            this.isDelivered = false
            this.itemId = UUID.randomUUID().toString()
            this.clientContext = clientContext
            this.bundle = HashMap<Any, Any>().apply {
                put("threadId", threadId)
            }
        }
    }
}