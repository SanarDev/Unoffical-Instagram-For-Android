package com.idirect.app.fbns.packethelper

import com.idirect.app.fbns.packethelper.FbnsPacketDecoderJava.ParseState
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.ReplayingDecoder
import io.netty.handler.codec.mqtt.*
import io.netty.util.CharsetUtil
import java.util.*
import kotlin.experimental.and

class FbnsPacketDecoderJava : ReplayingDecoder<ParseState?>() {
    enum class ParseState {
        READ_FIXED_HEADER, READ_VARIABLE_HEADER, READ_PAYLOAD, BAD_MESSAGE
    }

    private class Signatures {
        //            public const byte UnsubAck = 176;
        fun isPublish(signature: Int): Boolean {
            return signature and 240 == 48
        }

        companion object {
            const val PubAck = 64
            const val ConAck = 32

            //            public const byte PubRec = 80;
            //            public const byte PubRel = 98;
            //            public const byte PubComp = 112;
            const val Connect = 16
            const val Subscribe = 130
            const val SubAck = 144

            //            public const byte PingReq = 192;
            const val PingResp = 208

            //            public const byte Disconnect = 224;
            const val Unsubscribe = 162
        }
    }

    private var mqttFixedHeader: MqttFixedHeader? = null
    private var variableHeader: Any? = null
    private var bytesRemainingInVariablePart = 0
    private val maxBytesInMessage =
        DEFAULT_MAX_BYTES_IN_MESSAGE

    @Throws(Exception::class)
    override fun decode(
        ctx: ChannelHandlerContext,
        buffer: ByteBuf,
        out: MutableList<Any>
    ) {
        val signature = buffer.readByte().toInt()
        val data =
            buffer.readBytes(buffer.writerIndex() - buffer.readerIndex()).array()
        when (state()) {
            ParseState.READ_FIXED_HEADER -> {
                run {
                    try {
                        mqttFixedHeader = decodeFixedHeader(buffer)
                        bytesRemainingInVariablePart = mqttFixedHeader!!.remainingLength()
                        checkpoint(ParseState.READ_VARIABLE_HEADER)
                        // fall through
                    } catch (cause: Exception) {
                        out.add(invalidMessage(cause))
                        return
                    }
                }
                try {
                    val decodedVariableHeader =
                        decodeVariableHeader(
                            buffer,
                            mqttFixedHeader
                        )
                    variableHeader = decodedVariableHeader!!.value
                    if (bytesRemainingInVariablePart > maxBytesInMessage) {
                        throw DecoderException("too large message: $bytesRemainingInVariablePart bytes")
                    }
                    bytesRemainingInVariablePart -= decodedVariableHeader.numberOfBytesConsumed
                    checkpoint(ParseState.READ_PAYLOAD)
                    // fall through
                } catch (cause: Exception) {
                    out.add(invalidMessage(cause))
                    return
                }
                try {
                    val decodedPayload =
                        decodePayload(
                            buffer,
                            mqttFixedHeader!!.messageType(),
                            bytesRemainingInVariablePart,
                            variableHeader
                        )
                    bytesRemainingInVariablePart -= decodedPayload.numberOfBytesConsumed
                    if (bytesRemainingInVariablePart != 0) {
                        throw DecoderException(
                            "non-zero remaining payload bytes: " +
                                    bytesRemainingInVariablePart + " (" + mqttFixedHeader!!.messageType() + ')'
                        )
                    }
                    checkpoint(ParseState.READ_FIXED_HEADER)
                    val message = MqttMessageFactory.newMessage(
                        mqttFixedHeader, variableHeader, decodedPayload.value
                    )
                    mqttFixedHeader = null
                    variableHeader = null
                    out.add(message)
                } catch (cause: Exception) {
                    out.add(invalidMessage(cause))
                    return
                }
                // Keep discarding until disconnection.
                buffer.skipBytes(actualReadableBytes())
            }
            ParseState.READ_VARIABLE_HEADER -> {
                try {
                    val decodedVariableHeader =
                        decodeVariableHeader(
                            buffer,
                            mqttFixedHeader
                        )
                    variableHeader = decodedVariableHeader!!.value
                    if (bytesRemainingInVariablePart > maxBytesInMessage) {
                        throw DecoderException("too large message: $bytesRemainingInVariablePart bytes")
                    }
                    bytesRemainingInVariablePart -= decodedVariableHeader.numberOfBytesConsumed
                    checkpoint(ParseState.READ_PAYLOAD)
                } catch (cause: Exception) {
                    out.add(invalidMessage(cause))
                    return
                }
                try {
                    val decodedPayload =
                        decodePayload(
                            buffer,
                            mqttFixedHeader!!.messageType(),
                            bytesRemainingInVariablePart,
                            variableHeader
                        )
                    bytesRemainingInVariablePart -= decodedPayload.numberOfBytesConsumed
                    if (bytesRemainingInVariablePart != 0) {
                        throw DecoderException(
                            "non-zero remaining payload bytes: " +
                                    bytesRemainingInVariablePart + " (" + mqttFixedHeader!!.messageType() + ')'
                        )
                    }
                    checkpoint(ParseState.READ_FIXED_HEADER)
                    val message = MqttMessageFactory.newMessage(
                        mqttFixedHeader, variableHeader, decodedPayload.value
                    )
                    mqttFixedHeader = null
                    variableHeader = null
                    out.add(message)
                } catch (cause: Exception) {
                    out.add(invalidMessage(cause))
                    return
                }
                buffer.skipBytes(actualReadableBytes())
            }
            ParseState.READ_PAYLOAD -> {
                try {
                    val decodedPayload =
                        decodePayload(
                            buffer,
                            mqttFixedHeader!!.messageType(),
                            bytesRemainingInVariablePart,
                            variableHeader
                        )
                    bytesRemainingInVariablePart -= decodedPayload.numberOfBytesConsumed
                    if (bytesRemainingInVariablePart != 0) {
                        throw DecoderException(
                            "non-zero remaining payload bytes: " +
                                    bytesRemainingInVariablePart + " (" + mqttFixedHeader!!.messageType() + ')'
                        )
                    }
                    checkpoint(ParseState.READ_FIXED_HEADER)
                    val message = MqttMessageFactory.newMessage(
                        mqttFixedHeader, variableHeader, decodedPayload.value
                    )
                    mqttFixedHeader = null
                    variableHeader = null
                    out.add(message)
                } catch (cause: Exception) {
                    out.add(invalidMessage(cause))
                    return
                }
                buffer.skipBytes(actualReadableBytes())
            }
            ParseState.BAD_MESSAGE -> buffer.skipBytes(actualReadableBytes())
            else -> throw Error()
        }
    }

    private class Result<T> internal constructor(val value: T, val numberOfBytesConsumed: Int)

    private fun invalidMessage(cause: Throwable): MqttMessage {
        checkpoint(ParseState.BAD_MESSAGE)
        return MqttMessageFactory.newInvalidMessage(mqttFixedHeader, variableHeader, cause)
    }

    companion object {
        private val TOPIC_WILDCARDS = charArrayOf('#', '+')
        private const val MIN_CLIENT_ID_LENGTH = 1
        private const val MAX_CLIENT_ID_LENGTH = 23
        private const val DEFAULT_MAX_BYTES_IN_MESSAGE = 8092

        private fun decodeConnAckVariableHeader(buffer: ByteBuf): Result<MqttConnAckVariableHeader> {
            val sessionPresent = (buffer.readUnsignedByte().toInt() and 0x01) == 0x01
            val returnCode = buffer.readByte()
            val numberOfBytesConsumed = 2
            val mqttConnAckVariableHeader =
                MqttConnAckVariableHeader(MqttConnectReturnCode.valueOf(returnCode), sessionPresent)
            return Result(
                mqttConnAckVariableHeader,
                numberOfBytesConsumed
            )
        }

        private fun decodeVariableHeader(
            buffer: ByteBuf,
            mqttFixedHeader: MqttFixedHeader?
        ): Result<*>? {
            return when (mqttFixedHeader!!.messageType()) {
                MqttMessageType.CONNECT -> decodeConnectionVariableHeader(
                    buffer
                )
                MqttMessageType.CONNACK -> decodeConnAckVariableHeader(
                    buffer
                )
                MqttMessageType.SUBSCRIBE, MqttMessageType.UNSUBSCRIBE, MqttMessageType.SUBACK, MqttMessageType.UNSUBACK, MqttMessageType.PUBACK, MqttMessageType.PUBREC, MqttMessageType.PUBCOMP, MqttMessageType.PUBREL -> decodeMessageIdVariableHeader(
                    buffer
                )
                MqttMessageType.PUBLISH -> decodePublishVariableHeader(
                    buffer,
                    mqttFixedHeader
                )
                MqttMessageType.PINGREQ, MqttMessageType.PINGRESP, MqttMessageType.DISCONNECT ->                 // Empty variable header
                    Result<Any?>(
                        null,
                        0
                    )
                else -> {
                    null;
                }
            }
            return Result<Any?>(
                null,
                0
            ) //should never reach here
        }

        private fun decodeMessageIdVariableHeader(buffer: ByteBuf): Result<MqttMessageIdVariableHeader> {
            val messageId =
                decodeMessageId(buffer)
            return Result(
                MqttMessageIdVariableHeader.from(messageId.value),
                messageId.numberOfBytesConsumed
            )
        }

        fun isValidPublishTopicName(topicName: String?): Boolean {
            // publish topic name must not contain any wildcard
            for (c in TOPIC_WILDCARDS) {
                if (topicName!!.indexOf(c) >= 0) {
                    return false
                }
            }
            return true
        }

        private fun decodeString(
            buffer: ByteBuf,
            minBytes: Int = 0,
            maxBytes: Int = Int.MAX_VALUE
        ): Result<String?> {
            val decodedSize =
                decodeMsbLsb(buffer)
            val size = decodedSize.value
            var numberOfBytesConsumed = decodedSize.numberOfBytesConsumed
            if (size < minBytes || size > maxBytes) {
                buffer.skipBytes(size)
                numberOfBytesConsumed += size
                return Result(
                    null,
                    numberOfBytesConsumed
                )
            }
            val s = buffer.toString(buffer.readerIndex(), size, CharsetUtil.UTF_8)
            buffer.skipBytes(size)
            numberOfBytesConsumed += size
            return Result(
                s,
                numberOfBytesConsumed
            )
        }

        private fun decodePublishVariableHeader(
            buffer: ByteBuf,
            mqttFixedHeader: MqttFixedHeader?
        ): Result<MqttPublishVariableHeader> {
            val decodedTopic =
                decodeString(buffer)
            if (!isValidPublishTopicName(decodedTopic.value)) {
                throw DecoderException("invalid publish topic name: " + decodedTopic.value + " (contains wildcards)")
            }
            var numberOfBytesConsumed = decodedTopic.numberOfBytesConsumed
            var messageId = -1
            if (mqttFixedHeader!!.qosLevel().value() > 0) {
                val decodedMessageId =
                    decodeMessageId(buffer)
                messageId = decodedMessageId.value
                numberOfBytesConsumed += decodedMessageId.numberOfBytesConsumed
            }
            val mqttPublishVariableHeader =
                MqttPublishVariableHeader(decodedTopic.value, messageId)
            return Result(
                mqttPublishVariableHeader,
                numberOfBytesConsumed
            )
        }

        private fun decodeMsbLsb(
            buffer: ByteBuf,
            min: Int = 0,
            max: Int = 65535
        ): Result<Int> {
            val msbSize = buffer.readUnsignedByte()
            val lsbSize = buffer.readUnsignedByte()
            val numberOfBytesConsumed = 2
            var result: Int = msbSize.toInt() shl 8 or lsbSize.toInt()
            if (result < min || result > max) {
                result = -1
            }
            return Result(
                result,
                numberOfBytesConsumed
            )
        }

        fun isValidMessageId(messageId: Int): Boolean {
            return messageId != 0
        }

        private fun decodeMessageId(buffer: ByteBuf): Result<Int> {
            val messageId =
                decodeMsbLsb(buffer)
            if (!isValidMessageId(messageId.value)) {
                throw DecoderException("invalid messageId: " + messageId.value)
            }
            return messageId
        }

        private fun decodeConnectionVariableHeader(buffer: ByteBuf): Result<MqttConnectVariableHeader> {
            val protoString =
                decodeString(buffer)
            var numberOfBytesConsumed = protoString.numberOfBytesConsumed
            val protocolLevel = buffer.readByte()
            numberOfBytesConsumed += 1
            val mqttVersion =
                MqttVersion.fromProtocolNameAndLevel(protoString.value, protocolLevel)
            val b1 = buffer.readUnsignedByte().toInt()
            numberOfBytesConsumed += 1
            val keepAlive =
                decodeMsbLsb(buffer)
            numberOfBytesConsumed += keepAlive.numberOfBytesConsumed
            val hasUserName = b1 and 0x80 == 0x80
            val hasPassword = b1 and 0x40 == 0x40
            val willRetain = b1 and 0x20 == 0x20
            val willQos = b1 and 0x18 shr 3
            val willFlag = b1 and 0x04 == 0x04
            val cleanSession = b1 and 0x02 == 0x02
            if (mqttVersion == MqttVersion.MQTT_3_1_1) {
                val zeroReservedFlag = b1 and 0x01 == 0x0
                if (!zeroReservedFlag) {
                    // MQTT v3.1.1: The Server MUST validate that the reserved flag in the CONNECT Control Packet is
                    // set to zero and disconnect the Client if it is not zero.
                    // See http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc385349230
                    throw DecoderException("non-zero reserved flag")
                }
            }
            val mqttConnectVariableHeader = MqttConnectVariableHeader(
                mqttVersion.protocolName(),
                mqttVersion.protocolLevel().toInt(),
                hasUserName,
                hasPassword,
                willRetain,
                willQos,
                willFlag,
                cleanSession,
                keepAlive.value
            )
            return Result(
                mqttConnectVariableHeader,
                numberOfBytesConsumed
            )
        }

        private fun decodePayload(
            buffer: ByteBuf,
            messageType: MqttMessageType,
            bytesRemainingInVariablePart: Int,
            variableHeader: Any?
        ): Result<*> {
            return when (messageType) {
                MqttMessageType.CONNECT -> decodeConnectionPayload(
                    buffer,
                    variableHeader as MqttConnectVariableHeader?
                )
                MqttMessageType.SUBSCRIBE -> decodeSubscribePayload(
                    buffer,
                    bytesRemainingInVariablePart
                )
                MqttMessageType.SUBACK -> decodeSubackPayload(
                    buffer,
                    bytesRemainingInVariablePart
                )
                MqttMessageType.UNSUBSCRIBE -> decodeUnsubscribePayload(
                    buffer,
                    bytesRemainingInVariablePart
                )
                MqttMessageType.PUBLISH -> decodePublishPayload(
                    buffer,
                    bytesRemainingInVariablePart
                )
                else ->                 // unknown payload , no byte consumed
                    Result<Any?>(
                        null,
                        0
                    )
            }
        }

        private fun decodeByteArray(buffer: ByteBuf): Result<ByteArray> {
            val decodedSize =
                decodeMsbLsb(buffer)
            val size = decodedSize.value
            val bytes = ByteArray(size)
            buffer.readBytes(bytes)
            return Result(
                bytes,
                decodedSize.numberOfBytesConsumed + size
            )
        }

        private fun decodeConnectionPayload(
            buffer: ByteBuf,
            mqttConnectVariableHeader: MqttConnectVariableHeader?
        ): Result<MqttConnectPayload> {
            val decodedClientId =
                decodeString(buffer)
            val decodedClientIdValue = decodedClientId.value
            val mqttVersion = MqttVersion.fromProtocolNameAndLevel(
                mqttConnectVariableHeader!!.name(),
                mqttConnectVariableHeader.version().toByte()
            )
            if (!isValidClientId(
                    mqttVersion,
                    decodedClientIdValue
                )
            ) {
                throw MqttIdentifierRejectedException("invalid clientIdentifier: $decodedClientIdValue")
            }
            var numberOfBytesConsumed = decodedClientId.numberOfBytesConsumed
            var decodedWillTopic: Result<String?>? =
                null
            var decodedWillMessage: Result<ByteArray>? =
                null
            if (mqttConnectVariableHeader.isWillFlag) {
                decodedWillTopic = decodeString(buffer, 0, 32767)
                numberOfBytesConsumed += decodedWillTopic.numberOfBytesConsumed
                decodedWillMessage = decodeByteArray(buffer)
                numberOfBytesConsumed += decodedWillMessage.numberOfBytesConsumed
            }
            var decodedUserName: Result<String?>? =
                null
            var decodedPassword: Result<ByteArray>? =
                null
            if (mqttConnectVariableHeader.hasUserName()) {
                decodedUserName = decodeString(buffer)
                numberOfBytesConsumed += decodedUserName.numberOfBytesConsumed
            }
            if (mqttConnectVariableHeader.hasPassword()) {
                decodedPassword = decodeByteArray(buffer)
                numberOfBytesConsumed += decodedPassword.numberOfBytesConsumed
            }
            val mqttConnectPayload = MqttConnectPayload(
                decodedClientId.value,
                decodedWillTopic?.value,
                decodedWillMessage?.value,
                decodedUserName?.value,
                decodedPassword?.value
            )
            return Result(
                mqttConnectPayload,
                numberOfBytesConsumed
            )
        }

        fun isValidClientId(mqttVersion: MqttVersion, clientId: String?): Boolean {
            if (mqttVersion == MqttVersion.MQTT_3_1) {
                return clientId != null && clientId.length >= MIN_CLIENT_ID_LENGTH && clientId.length <= MAX_CLIENT_ID_LENGTH
            }
            if (mqttVersion == MqttVersion.MQTT_3_1_1) {
                // In 3.1.3.1 Client Identifier of MQTT 3.1.1 specification, The Server MAY allow ClientId’s
                // that contain more than 23 encoded bytes. And, The Server MAY allow zero-length ClientId.
                return clientId != null
            }
            throw IllegalArgumentException("$mqttVersion is unknown mqtt version")
        }

        private fun decodeSubscribePayload(
            buffer: ByteBuf,
            bytesRemainingInVariablePart: Int
        ): Result<MqttSubscribePayload> {
            val subscribeTopics: MutableList<MqttTopicSubscription> =
                ArrayList()
            var numberOfBytesConsumed = 0
            while (numberOfBytesConsumed < bytesRemainingInVariablePart) {
                val decodedTopicName =
                    decodeString(buffer)
                numberOfBytesConsumed += decodedTopicName.numberOfBytesConsumed
                val qos: Int = buffer.readUnsignedByte().toInt() and 0x03
                numberOfBytesConsumed++
                subscribeTopics.add(
                    MqttTopicSubscription(
                        decodedTopicName.value,
                        MqttQoS.valueOf(qos)
                    )
                )
            }
            return Result(
                MqttSubscribePayload(subscribeTopics),
                numberOfBytesConsumed
            )
        }

        private fun decodeSubackPayload(
            buffer: ByteBuf,
            bytesRemainingInVariablePart: Int
        ): Result<MqttSubAckPayload> {
            val grantedQos: MutableList<Int> = ArrayList()
            var numberOfBytesConsumed = 0
            while (numberOfBytesConsumed < bytesRemainingInVariablePart) {
                var qos = buffer.readUnsignedByte().toInt()
                if (qos != MqttQoS.FAILURE.value()) {
                    qos = qos and 0x03
                }
                numberOfBytesConsumed++
                grantedQos.add(qos)
            }
            return Result(
                MqttSubAckPayload(grantedQos),
                numberOfBytesConsumed
            )
        }

        private fun decodeUnsubscribePayload(
            buffer: ByteBuf,
            bytesRemainingInVariablePart: Int
        ): Result<MqttUnsubscribePayload> {
            val unsubscribeTopics: MutableList<String?> =
                ArrayList()
            var numberOfBytesConsumed = 0
            while (numberOfBytesConsumed < bytesRemainingInVariablePart) {
                val decodedTopicName =
                    decodeString(buffer)
                numberOfBytesConsumed += decodedTopicName.numberOfBytesConsumed
                unsubscribeTopics.add(decodedTopicName.value)
            }
            return Result(
                MqttUnsubscribePayload(unsubscribeTopics),
                numberOfBytesConsumed
            )
        }

        private fun decodePublishPayload(
            buffer: ByteBuf,
            bytesRemainingInVariablePart: Int
        ): Result<ByteBuf> {
            val b = buffer.readRetainedSlice(bytesRemainingInVariablePart)
            return Result(
                b,
                bytesRemainingInVariablePart
            )
        }

        private fun decodeFixedHeader(buffer: ByteBuf): MqttFixedHeader {
            val b1 = buffer.readUnsignedByte().toInt()
            val messageType: MqttMessageType = MqttMessageType.valueOf(b1 shr 4)
            val dupFlag = b1 and 0x08 == 0x08
            val qosLevel: Int = b1 and 0x06 shr 1
            val retain = b1 and 0x01 != 0
            var remainingLength = 0
            var multiplier = 1
            var digit: Short
            var loops = 0
            do {
                digit = buffer.readUnsignedByte()
                remainingLength += (digit and 127) * multiplier
                multiplier *= 128
                loops++
            } while (digit.toInt() and 128 != 0 && loops < 4)

            // MQTT protocol limits Remaining Length to 4 bytes
            if (loops == 4 && digit.toInt() and 128 != 0) {
                throw DecoderException("remaining length exceeds 4 digits ($messageType)")
            }
            val decodedFixedHeader = MqttFixedHeader(
                messageType,
                dupFlag,
                MqttQoS.valueOf(qosLevel),
                retain,
                remainingLength
            )
            return validateFixedHeader(
                resetUnusedFields(
                    decodedFixedHeader
                )
            )
        }

        fun validateFixedHeader(mqttFixedHeader: MqttFixedHeader): MqttFixedHeader {
            return when (mqttFixedHeader.messageType()) {
                MqttMessageType.PUBREL, MqttMessageType.SUBSCRIBE, MqttMessageType.UNSUBSCRIBE -> {
                    if (mqttFixedHeader.qosLevel() != MqttQoS.AT_LEAST_ONCE) {
                        throw DecoderException(mqttFixedHeader.messageType().name + " message must have QoS 1")
                    }
                    mqttFixedHeader
                }
                else -> mqttFixedHeader
            }
        }

        fun resetUnusedFields(mqttFixedHeader: MqttFixedHeader): MqttFixedHeader {
            return when (mqttFixedHeader.messageType()) {
                MqttMessageType.CONNECT, MqttMessageType.CONNACK, MqttMessageType.PUBACK, MqttMessageType.PUBREC, MqttMessageType.PUBCOMP, MqttMessageType.SUBACK, MqttMessageType.UNSUBACK, MqttMessageType.PINGREQ, MqttMessageType.PINGRESP, MqttMessageType.DISCONNECT -> {
                    if (mqttFixedHeader.isDup || mqttFixedHeader.qosLevel() != MqttQoS.AT_MOST_ONCE ||
                        mqttFixedHeader.isRetain
                    ) {
                        MqttFixedHeader(
                            mqttFixedHeader.messageType(),
                            false,
                            MqttQoS.AT_MOST_ONCE,
                            false,
                            mqttFixedHeader.remainingLength()
                        )
                    } else mqttFixedHeader
                }
                MqttMessageType.PUBREL, MqttMessageType.SUBSCRIBE, MqttMessageType.UNSUBSCRIBE -> {
                    if (mqttFixedHeader.isRetain) {
                        MqttFixedHeader(
                            mqttFixedHeader.messageType(),
                            mqttFixedHeader.isDup,
                            mqttFixedHeader.qosLevel(),
                            false,
                            mqttFixedHeader.remainingLength()
                        )
                    } else mqttFixedHeader
                }
                else -> mqttFixedHeader
            }
        } /*

            case Signatures.ConAck : {
                String conAckPacket = new String(data).substring(6);
                FbnsAuth fbnsAuth = new Gson().fromJson(conAckPacket, FbnsAuth.class);
                fbnsAuth.setToken(conAckPacket);
                FbnsConnAckPacket connAckPacket = new FbnsConnAckPacket(null,null, fbnsAuth);
                out.add(connAckPacket);
            }
     */
    }
}