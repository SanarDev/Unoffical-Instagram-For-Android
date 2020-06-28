package com.sanardev.instagrammqtt.fbns.packethelper;

import android.util.Log;

import com.sanardev.instagrammqtt.constants.InstagramConstants;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.CharsetUtil;


public class FbnsPacketEncoder extends MessageToMessageEncoder<MqttMessage> {

    public static final short PACKET_ID_LENGTH = 2;
    public static final short STRING_SIZE_LENGTH = 2;
    public static final short MAX_VARIABLE_LENGTH = 4;

    @Override
    protected void encode(ChannelHandlerContext ctx, MqttMessage packet, List<Object> out) throws Exception {
        ByteBufAllocator byteBufAllocator = ctx.alloc();
        switch (packet.fixedHeader().messageType()) {
            case CONNECT: {
                if (packet instanceof FbnsConnectPacket) {
                    encodeFbnsConnectPacket(byteBufAllocator, (FbnsConnectPacket) packet, out);
                    Log.i(InstagramConstants.DEBUG_TAG,"Encode connect packet");
                }
                break;
            }
            case PUBLISH: {
                encodePublishPacket(byteBufAllocator, (MqttPublishMessage) packet, out);
                break;
            }
            case PUBACK:
            case PUBREC:
            case PUBREL:
            case PUBCOMP:
            case UNSUBACK:
                break;
        }
    }

    private void encodeFbnsConnectPacket(ByteBufAllocator bufferAllocator, FbnsConnectPacket packet, List<Object> output) throws UnsupportedEncodingException {
        ByteBuf payload = (packet.payload() == null) ? Unpooled.EMPTY_BUFFER : (ByteBuf) packet.payload();
        int payloadSize = payload.readableBytes();
        byte[] protocolNameBytes = encodeStringUtf8(packet.getProtocolName());

        int variableHeaderBufferSize = STRING_SIZE_LENGTH + protocolNameBytes.length + 4;
        int variablePartSize = variableHeaderBufferSize + payloadSize;
        int fixedHeaderBufferSize = 1 + MAX_VARIABLE_LENGTH;

        ByteBuf buf = null;
        try {

            buf = bufferAllocator.buffer(fixedHeaderBufferSize + variableHeaderBufferSize);
            buf.writeByte((int) packet.fixedHeader().messageType().value() << 4);
            writeVariableLengthInt(buf, variablePartSize);

            // variable part
            buf.writeShort(protocolNameBytes.length);
            buf.writeBytes(protocolNameBytes);
            buf.writeByte(packet.getProtocolLevel());
            buf.writeByte(packet.getConnectFlags());
            buf.writeShort(packet.getKeepAliveInSeconds());

            output.add(buf);
            buf = null;
        } finally {
            if (buf != null)
                buf.release();
        }
        if (payload.isReadable()) {
            output.add(payload.retain());
        }

    }

    private void encodePublishPacket(ByteBufAllocator bufferAllocator, MqttPublishMessage packet, List<Object> output) {
        MqttFixedHeader mqttFixedHeader = packet.fixedHeader();
        MqttPublishVariableHeader variableHeader = packet.variableHeader();
        ByteBuf payload = (packet.payload() == null) ? Unpooled.EMPTY_BUFFER : packet.payload();
        String topicName = variableHeader.topicName();
        byte[] topicNameBytes = topicName.getBytes(StandardCharsets.UTF_8);

        int variableHeaderBufferSize = STRING_SIZE_LENGTH + topicNameBytes.length +
                (mqttFixedHeader.qosLevel().value() > MqttQoS.AT_MOST_ONCE.value() ? PACKET_ID_LENGTH : 0);
        int payloadBufferSize = payload.readableBytes();
        int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
        int fixedHeaderBufferSize = 1 + MAX_VARIABLE_LENGTH;
        ByteBuf buf = null;
        try {
            buf = bufferAllocator.buffer(fixedHeaderBufferSize + variablePartSize);
            buf.writeByte(getFixedHeaderByte1(mqttFixedHeader));
            writeVariableLengthInt(buf, variablePartSize);
            buf.writeShort(topicNameBytes.length);
            buf.writeBytes(topicNameBytes);
            if (mqttFixedHeader.qosLevel().value() > 0) {
                buf.writeShort(variableHeader.packetId());
            }

            output.add(buf);
            buf = null;
        } finally {
            if (buf != null) {
                buf.release();
            }
        }

        if (payload.isReadable()) {
            output.add(payload.retain());
        }
    }


    private static int getFixedHeaderByte1(MqttFixedHeader header) {
        int ret = 0;
        ret |= header.messageType().value() << 4;
        if (header.isDup()) {
            ret |= 0x08;
        }
        ret |= header.qosLevel().value() << 1;
        if (header.isRetain()) {
            ret |= 0x01;
        }
        return ret;
    }

    private static int getVariableLengthInt(int num) {
        int count = 0;
        do {
            num /= 128;
            count++;
        } while (num > 0);
        return count;
    }

    static void writeVariableLengthInt(ByteBuf buffer, int value) {
        do {
            int digit = value % 128;
            value /= 128;
            if (value > 0) {
                digit |= 0x80;
            }
            buffer.writeByte(digit);
        }
        while (value > 0);
    }

    private static byte[] encodeStringUtf8(String s) {
        return s.getBytes(CharsetUtil.UTF_8);
    }
}
