package com.sanardev.instagrammqtt.realtime.network

import android.util.Log
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.realtime.PayloadProcessor
import com.sanardev.instagrammqtt.realtime.packethelper.ForegroundStateConfig
import com.sanardev.instagrammqtt.service.realtime.RealTimeService
import com.sanardev.instagrammqtt.utils.ZlibUtis
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.handler.codec.mqtt.*
import org.apache.thrift.protocol.*
import org.apache.thrift.transport.TMemoryBuffer
import java.util.*


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
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        Log.i(InstagramConstants.DEBUG_TAG, "RealTime Channel closed");
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
                Log.i(InstagramConstants.DEBUG_TAG, "RealTime ConnAck");
                ctx!!.pipeline().remove("encoder")
                realTimeService.onConnAck()
                sendForegroundState(ctx!!,true,true,90)
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
//                        sendForegroundState(ctx!!,
//                            inForegroundApp = true,
//                            inForegroundDevice = true,
//                            keepAliveTimeout = 900
//                        )
                    }
                    InstagramConstants.RealTimeTopics.REALTIME_SUB.id ->{
                        val a = TMemoryBuffer(json.size)
                        a.write(json)
                        val iprot = TCompactProtocol(a)
                        var topic:String
                        var payload:String
                        try{
                            var field :TField
                            while (true){
                                field = iprot.readFieldBegin()
                                if(field.type == TType.STOP){
                                    break
                                }
                                when(field.id.toInt()){
                                    1 ->{
                                        if(field.type == TType.STRING){
                                            topic = iprot.readString()
                                        }else{
                                            TProtocolUtil.skip(iprot,field.type)
                                        }
                                    }
                                    2 ->{
                                        if(field.type == TType.STRING){
                                            payload = iprot.readString()
                                        }else{
                                            TProtocolUtil.skip(iprot,field.type)
                                        }
                                    }
                                    else ->{
                                        TProtocolUtil.skip(iprot,field.type)
                                    }
                                }
                                iprot.readFieldEnd()
                            }
                        }finally {
                            Log.i("TEST","TEST")
                        }

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


    private fun sendForegroundState(
        ctx: ChannelHandlerContext,
        inForegroundApp: Boolean,
        inForegroundDevice: Boolean,
        keepAliveTimeout: Int
    ) {
        val topicName = InstagramConstants.RealTimeTopics.FOREGROUND_STATE.id.toString()
        val packetID = generatePacketID()
        val payload = PayloadProcessor.buildForegroundStateThrift(ForegroundStateConfig().apply {
            this.inForegroundApp = inForegroundApp
            this.inForegroundDevice = inForegroundDevice
            this.keepAliveTimeOut = keepAliveTimeOut
        })
        val mqttPublishMessage = MqttPublishMessage(
            MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, false, 0),
            MqttPublishVariableHeader(topicName, packetID),
            payload
        )
        Log.i(InstagramConstants.DEBUG_TAG, "RealTime Update foregroundState $inForegroundApp with id $packetID")
        ctx.writeAndFlush(mqttPublishMessage)
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

    private fun generatePacketID():Int{
        return Random().nextInt(65535)
    }
    @Throws(Exception::class)
    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable
    ) {
        Log.i(InstagramConstants.DEBUG_TAG, "RealTime Exception ${cause.message}");
    }

}