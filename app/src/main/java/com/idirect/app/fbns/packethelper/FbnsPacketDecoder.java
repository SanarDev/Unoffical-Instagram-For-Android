package com.idirect.app.fbns.packethelper;

import android.util.Log;

import com.google.gson.GsonBuilder;
import com.idirect.app.constants.InstagramConstants;
import com.idirect.app.datasource.model.FbnsAuth;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class FbnsPacketDecoder extends ReplayingDecoder<FbnsPacketDecoder.ParseState> {

    private static class Signatures {
        public static final int PubAck = 64;
        public static final int ConAck = 32;

        //            public const byte PubRec = 80;
        //            public const byte PubRel = 98;
        //            public const byte PubComp = 112;
        public static final int Connect = 16;
        public static final int Subscribe = 130;
        public static final int SubAck = 144;

        //            public const byte PingReq = 192;
        public static final int PingResp = 208;

        //            public const byte Disconnect = 224;
        public static final int Unsubscribe = 162;

        //            public const byte UnsubAck = 176;
        boolean isPublish(int signature){
            return (signature & 240) == 48;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int signature = in.readByte();
        byte[] data = in.readBytes(in.writerIndex() - in.readerIndex()).array();
        switch (signature){
            case Signatures.ConAck:{
                if(data.length > 6){
                    String conAckPacket = new String(data).substring(6);
                    Log.i(InstagramConstants.DEBUG_TAG,"Receive ConnAck packet "+conAckPacket);
                    FbnsAuth fbnsAuth = new GsonBuilder().setLenient().create().fromJson(conAckPacket, FbnsAuth.class);
                    fbnsAuth.setToken(conAckPacket);
                    FbnsConnAckPacket connAckPacket = new FbnsConnAckPacket(null,null, fbnsAuth);
                    out.add(connAckPacket);
                }else{
                    FbnsConnAckPacket connAckPacket = new FbnsConnAckPacket(null,null, null);
                    out.add(connAckPacket);
                }
                break;
            }
        }
    }

    enum ParseState {
        READY,
        FAIL
    }
}
