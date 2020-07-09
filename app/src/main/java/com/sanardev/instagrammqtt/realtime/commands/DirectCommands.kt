package com.sanardev.instagrammqtt.realtime.commands

import com.google.gson.Gson
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.remote.InstagramRemote
import com.sanardev.instagrammqtt.utils.InstagramHashUtils
import com.sanardev.instagrammqtt.utils.ZlibUtis
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.handler.codec.mqtt.*
import java.util.*
import kotlin.collections.HashMap

class DirectCommands(var client: Channel, var gson: Gson = Gson()) {

    private fun sendCommand(
        action: String,
        data: HashMap<String, String>,
        threadId: String,
        clientContext: String
    ): String {

        val map = HashMap<String, String>()
        map["action"] = action
        map["thread_id"] = threadId
        map["client_context"] = clientContext
        map.putAll(data)
        val json = gson.toJson(map)

        client.writeAndFlush(
            MqttPublishMessage(
                MqttFixedHeader(
                    MqttMessageType.PUBLISH,
                    false,
                    MqttQoS.AT_LEAST_ONCE,
                    false,
                    json.length
                ),
                MqttPublishVariableHeader(
                    InstagramConstants.RealTimeTopics.SEND_MESSAGE.id.toString(),
                    1400
                ),
                Unpooled.copiedBuffer(ZlibUtis.compress(json.toByteArray()))
            )
        )
        return clientContext
    }


    fun sendItem(
        threadId: String,
        itemType: String,
        data: HashMap<String, String>,
        clientContext: String = InstagramHashUtils.getClientContext()
    ): String {
        return this.sendCommand("send_item", data.apply {
            put("item_type", itemType)
        }, threadId, clientContext ?: UUID.randomUUID().toString())

    }

    fun sendHashtag(
        text: String,
        threadId: String,
        hashtag: String,
        clientContext: String = InstagramHashUtils.getClientContext()
    ): String {
        return sendItem(threadId, "hashtag", HashMap<String, String>().apply {
            put("text", text)
            put("hashtag", hashtag)
            put("item_id", hashtag)
        }, clientContext)
    }

    fun sendLike(threadId: String, clientContext: String = InstagramHashUtils.getClientContext()): String {
        return sendItem(threadId, "like", HashMap<String, String>(), clientContext)
    }

    fun sendLocation(
        text: String,
        locationId: String,
        threadId: String,
        clientContext: String = InstagramHashUtils.getClientContext()
    ): String {
        return sendItem(
            threadId = threadId,
            itemType = "location",
            clientContext = clientContext,
            data = HashMap<String, String>().apply {
                put("text", text)
                put("venue_id", locationId)
                put("item_id", locationId)
            })
    }

    fun sendMedia(
        text: String,
        mediaId: String,
        threadId: String,
        clientContext: String = InstagramHashUtils.getClientContext()
    ): String {
        return sendItem(threadId, "media_share", HashMap<String, String>().apply {
            put("text", text)
            put("media_id", mediaId)
        }, clientContext)
    }

    fun sendProfile(
        text: String,
        userId: String,
        threadId: String,
        clientContext: String = InstagramHashUtils.getClientContext()
    ): String {
        return sendItem(threadId, "profile", HashMap<String, String>().apply {
            put("text", text)
            put("profile_user_id", userId)
            put("item_id", userId)
        }, clientContext)
    }

    fun sendReaction(
        itemId: String,
        reactionType: String,
        clientContext: String = InstagramHashUtils.getClientContext(),
        threadId: String,
        reactionStatus: String
    ): String {
        return sendItem(threadId, "reaction", HashMap<String, String>().apply {
            put("item_id", itemId)
            put("node_type", "item")
            put("reaction_type", reactionType)
            put("reaction_status", reactionStatus)
        }, clientContext)
    }

    fun sendUserStory(
        text: String,
        storyId: String,
        threadId: String,
        clientContext: String = InstagramHashUtils.getClientContext()
    ): String {
        return sendItem(threadId, "reel_share", HashMap<String, String>().apply {
            put("text", text)
            put("item_id", storyId)
            put("media_id", storyId)
        }, clientContext)
    }

    fun sendText(
        text: String,
        clientContext: String,
        threadId: String
    ): String {
        return sendItem(threadId, "text", HashMap<String, String>().apply {
            put("text", text)
        }, clientContext)
    }

    fun markAsSeen(threadId: String, itemId: String): String {
        return sendCommand("mark_seen", HashMap<String, String>().apply {
            put("item_id", itemId)
        }, threadId, InstagramHashUtils.getClientContext())
    }

    fun indicateActivity(
        threadId: String,
        isActive: Boolean,
        clientContext: String = InstagramHashUtils.getClientContext()
    ): String {
        return sendCommand("indicate_activity", HashMap<String, String>().apply {
            put("activity_status", if (isActive) "1" else "0")
        }, threadId, clientContext)
    }
}