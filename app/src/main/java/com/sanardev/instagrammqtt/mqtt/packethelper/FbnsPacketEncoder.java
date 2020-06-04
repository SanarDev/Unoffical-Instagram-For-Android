package com.sanardev.instagrammqtt.mqtt.packethelper;

import android.util.Xml;

import java.io.UnsupportedEncodingException;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class FbnsPacketEncoder extends MessageToMessageEncoder<Packet> {

    public static final short PACKET_ID_LENGTH = 2;
    public static final short STRING_SIZE_LENGTH = 2;
    public static final short MAX_VARIABLE_LENGTH = 4;

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) throws Exception {
        ByteBufAllocator byteBufAllocator = ctx.alloc();
        switch (packet.getPacketType()) {
            case PacketType.CONNECT: {
                if (packet instanceof FbnsConnectPacket) {
                    encodeFbnsConnectPacket(byteBufAllocator, (FbnsConnectPacket) packet, out);
                }
                break;
            }
        }
    }

    private static void encodeFbnsConnectPacket(ByteBufAllocator bufferAllocator, FbnsConnectPacket packet, List<Object> output) throws UnsupportedEncodingException {
        ByteBuf payload = packet.payload;
        int payloadSize = 0;
        if (payload != null) {
            payloadSize = payload.readableBytes();
        }
        byte[] protocolNameBytes = packet.protocolName.getBytes("UTF-8");
        int variableHeaderBufferSize = STRING_SIZE_LENGTH + protocolNameBytes.length + 4;
        int variablePartSize = variableHeaderBufferSize + payloadSize;
        int fixedHeaderBufferSize = 1 + MAX_VARIABLE_LENGTH;
        ByteBuf buf = null;
        try {

            buf = bufferAllocator.buffer(fixedHeaderBufferSize + variableHeaderBufferSize);
            buf.writeByte((int) packet.getPacketType() << 4);
            writeVariableLengthInt(buf, variablePartSize);

            // variable part
            buf.writeShort(protocolNameBytes.length);
            buf.writeBytes(protocolNameBytes);
            buf.writeByte(packet.protocolLevel);
            buf.writeByte(packet.connectFlags);
            buf.writeShort(packet.keepAliveInSeconds);

            output.add(buf);
            buf = null;
        }finally {
            if(buf != null)
                buf.release();
        }
        if(payload != null){
            if(!payload.isReadable()){
                output.add(payload.retain());
            }
        }

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
}
