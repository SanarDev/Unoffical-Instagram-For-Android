package com.sanardev.instagrammqtt.utils

import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.DirectLikeReactions
import com.sanardev.instagrammqtt.datasource.model.DirectReactions
import com.sanardev.instagrammqtt.datasource.model.Message
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
            }
            message.reactions.apply {
                likesCount += 1
                likes.apply {
                    add(DirectLikeReactions(userId,timestamp,clientContext))
                }
            }

            return message
        }
    }
}