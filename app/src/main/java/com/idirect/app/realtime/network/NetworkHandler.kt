package com.idirect.app.realtime.network

import android.util.Log
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.datasource.model.event.ConnectionStateEvent
import com.idirect.app.realtime.commands.Commands
import com.idirect.app.service.realtime.RealTimeService
import com.idirect.app.utils.NetworkUtils
import com.idirect.app.utils.ZlibUtis
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.handler.codec.mqtt.*
import org.greenrobot.eventbus.EventBus


class NetworkHandler(private val realTimeService: RealTimeService) : ChannelInboundHandler {


    private var publishId: Int = 0

    @Throws(Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext) {
    }

    @Throws(Exception::class)
    override fun channelUnregistered(ctx: ChannelHandlerContext) {
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.CONNECTED))
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        if(NetworkUtils.getConnectionType(context = realTimeService.application) == NetworkUtils.NetworkType.NONE){
            EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.NETWORK_DISCONNECTED))
        }else{
            EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.CHANNEL_DISCONNECTED))
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
                EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.CONNECTED))
                Log.i(InstagramConstants.DEBUG_TAG, "RealTime ConnAck");
                ctx!!.pipeline().remove("encoder")
                realTimeService.onConnAck()
                Thread{
                    while (true){
                        Thread.sleep(20000)
                        ctx!!.writeAndFlush(MqttMessage.PINGREQ)
                    }
                }.start()
            }
            MqttMessageType.PUBACK -> {
                Log.i(
                    InstagramConstants.DEBUG_TAG,
                    "RealTime PubAck message ${(packet as MqttPubAckMessage).variableHeader()
                        .messageId()}"
                );
            }
            MqttMessageType.PINGRESP -> {
                Log.i(InstagramConstants.DEBUG_TAG, "RealTime PingResp");
            }
            MqttMessageType.PUBLISH -> {
                val publishMessage = packet as MqttPublishMessage

                val payload = (publishMessage.payload() as ByteBuf)
                val compressedData =
                    payload.readBytes(payload.writerIndex() - payload.readerIndex()).array()
                val json = (ZlibUtis.decompress(compressedData))
                val topicName = publishMessage.variableHeader().topicName().toInt()

                Log.i(
                    InstagramConstants.DEBUG_TAG,
                    "RealTime Publish ${String(json)} on Topic $topicName ${publishMessage.variableHeader()
                        .packetId()}"
                );
                when (topicName) {
                    InstagramConstants.RealTimeTopics.REGION_HINT.id->{
//                        ctx!!.writeAndFlush(getMqttPubackMessage(publishMessage))
//                        realTimeService.sendForegroundState(
//                            inForegroundApp = false,
//                            inForegroundDevice = false,
//                            keepAliveTimeout = 900
//                        )
                    }
                    InstagramConstants.RealTimeTopics.PUBSUB.id ->{
                        realTimeService.onMessageEvent(Commands.parseData(json))
                    }
                    InstagramConstants.RealTimeTopics.REALTIME_SUB.id ->{
                        realTimeService.onActivityEvent(Commands.parseData(json))
                    }

                    InstagramConstants.RealTimeTopics.MESSAGE_SYNC.id ->{
                        Commands.parseData(json)
                    }
                    InstagramConstants.RealTimeTopics.SEND_MESSAGE_RESPONSE.id -> {
                        realTimeService.onSendMessageResponse(String(json))
                    }
                }

                if (publishMessage.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                    ctx!!.writeAndFlush(getMqttPubackMessage(publishMessage))
                    Log.i(
                        InstagramConstants.DEBUG_TAG,
                        "RealTime PubAck ${publishMessage.variableHeader().packetId()}"
                    );
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
        Log.i(InstagramConstants.DEBUG_TAG, "RealTime Exception ${cause.message}");
    }

}