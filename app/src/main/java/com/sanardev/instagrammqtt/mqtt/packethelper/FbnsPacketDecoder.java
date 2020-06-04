package com.sanardev.instagrammqtt.mqtt.packethelper;

import android.util.Log;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;

public class FbnsPacketDecoder extends ReplayingDecoder<FbnsPacketDecoder.ParseState> {

    private static class Signatures
    {
        public static final int PubAck = 64;
        public static final int ConnAck = 32;
//            public const byte PubRec = 80;
//            public const byte PubRel = 98;
//            public const byte PubComp = 112;
        public static final byte Connect = 16;
        public static final int Subscribe = 130;
        public static final int SubAck = 144;
//            public const byte PingReq = 192;
        public static final int PingResp = 208;
//            public const byte Disconnect = 224;
        public static final int Unsubscribe = 162;
//            public const byte UnsubAck = 176;

        public static boolean IsPublish(int signature)
        {
            return (signature & 240) == 48;
        }
    }
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Log.i("TEST_APPLICATION","decode");
    }

    public enum ParseState
    {
        Ready,
        Failed
    }

}
