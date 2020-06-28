package com.sanardev.instagrammqtt.fbns.network

import android.util.Log
import com.google.gson.Gson
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.FbnsAuth
import com.sanardev.instagrammqtt.datasource.model.NotificationContentJson
import com.sanardev.instagrammqtt.datasource.model.PushNotification
import com.sanardev.instagrammqtt.service.fbns.FbnsService
import com.sanardev.instagrammqtt.utils.ZlibUtis
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.handler.codec.mqtt.*
import io.netty.util.CharsetUtil
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


class NetworkHandler(private val fbnsService: FbnsService) : ChannelInboundHandler {


    private var publishId: Int = 0

    @Throws(Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext) {
    }

    @Throws(Exception::class)
    override fun channelUnregistered(ctx: ChannelHandlerContext) {
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        Log.i(InstagramConstants.DEBUG_TAG,"Channel closed");
    }

    private fun registerMqttClient(ctx: ChannelHandlerContext) {
        val map = HashMap<String, Any>()
        map["pkg_name"] = InstagramConstants.INSTAGRAM_PACKAGE_NAME
        map["appid"] = InstagramConstants.APP_ID
        val json = Gson().toJson(map)
        val jsonByte = json.toByteArray(CharsetUtil.UTF_8)
        try {
            val payload = Unpooled.copiedBuffer(ZlibUtis.compress(jsonByte))
            val packetID = Random().nextInt(65535)
            val topicName = InstagramConstants.TopicIds.RegReq.id.toString()
//            val topicName = "/fbns_reg_req"
            val header = MqttFixedHeader(
                MqttMessageType.PUBLISH,
                false,
                MqttQoS.AT_LEAST_ONCE,
                false,
                payload.readableBytes()
            )
            val publishMessage =
                MqttPublishMessage(header, MqttPublishVariableHeader(topicName, packetID), payload)

            Log.i(InstagramConstants.DEBUG_TAG,"Publish $json on Topic $topicName with packetID $packetID");

            ctx.writeAndFlush(publishMessage)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
    }

    @Throws(Exception::class)
    override fun userEventTriggered(
        ctx: ChannelHandlerContext,
        evt: Any
    ) {
    }

    @Throws(Exception::class)
    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
    }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        val packet = msg as MqttMessage
        when (packet!!.fixedHeader().messageType()) {
            MqttMessageType.CONNACK -> {
                ctx!!.pipeline().remove("decoder")
                ctx!!.pipeline().remove("encoder")
                fbnsService.mUseCase.saveFbnsAuthData(packet.payload() as FbnsAuth)
                registerMqttClient(ctx!!)
            }
            MqttMessageType.PUBACK -> {
                Log.i(InstagramConstants.DEBUG_TAG,"PubAck message ${(packet as MqttPubAckMessage).variableHeader().messageId()}");
            }
            MqttMessageType.PINGRESP ->{
            }
            MqttMessageType.PUBLISH -> {
                val publishMessage = packet as MqttPublishMessage

                val payload = (publishMessage.payload() as ByteBuf)
                val compressedData =
                    payload.readBytes(payload.writerIndex() - payload.readerIndex()).array()
                val json = String(ZlibUtis.decompress(compressedData))
                val topicName = publishMessage.variableHeader().topicName().toInt()

                Log.i(InstagramConstants.DEBUG_TAG,"Publish $json on Topic $topicName ${publishMessage.variableHeader().packetId()}");
                when (topicName) {
                    InstagramConstants.TopicIds.RegResp.id -> {
                        onRegisterResponse(json)
                        Thread{
                            while (true){
                                Thread.sleep(60000)
                                ctx!!.writeAndFlush(MqttMessage.PINGREQ)
                            }
                        }.start()
                    }
                    InstagramConstants.TopicIds.Message.id -> {
                        val notification = Gson().fromJson(json,NotificationContentJson::class.java)
                        notification.notificationContent = Gson().fromJson(
                            notification.fbpushnotif,
                            PushNotification::class.java
                        )
                        fbnsService.mUseCase.notify(notification)

                    }
                }

                if (publishMessage.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                    ctx!!.writeAndFlush(getMqttPubackMessage(publishMessage))
                    Log.i(InstagramConstants.DEBUG_TAG,"PubAck ${publishMessage.variableHeader().packetId()}");
                }
            }
        }
    }

    fun getMqttPubackMessage(message: MqttPublishMessage): MqttPubAckMessage? {
        val fixedHeader = MqttFixedHeader(
            MqttMessageType.PUBACK,
            message.fixedHeader().isDup,
            message.fixedHeader().qosLevel(),
            message.fixedHeader().isRetain,
            message.fixedHeader().remainingLength()
        )
        return MqttPubAckMessage(
            fixedHeader,
            MqttMessageIdVariableHeader.from(message.variableHeader().packetId())
        )
    }

    private fun onRegisterResponse(json: String) {
        val map = Gson().fromJson(json,HashMap::class.java)
        val token = map["token"].toString()
        fbnsService.mUseCase.pushRegister(token)
    }

    @Throws(Exception::class)
    override fun handlerAdded(ctx: ChannelHandlerContext) {
    }

    @Throws(Exception::class)
    override fun handlerRemoved(ctx: ChannelHandlerContext) {
    }

    @Throws(Exception::class)
    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable
    ) {
        Log.i(InstagramConstants.DEBUG_TAG,"Exception ${cause.message}");
    }

}