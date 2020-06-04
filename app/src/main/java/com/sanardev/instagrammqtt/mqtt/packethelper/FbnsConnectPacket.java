package com.sanardev.instagrammqtt.mqtt.packethelper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Signal;

public class FbnsConnectPacket extends Packet
{
    @Override
    public int getPacketType() {
        return PacketType.CONNECT;
    }

    public int connectFlags = 194;
    public int protocolLevel = 3;
    public int keepAliveInSeconds = 900;
    public String protocolName = "MQTToT";
    public boolean cleanSession = false;
    public boolean hashWill = false;
    public ByteBuf willMessage;
    public String username;
    public String password;
    public String clientId;
    public String wllTopicName;
    public ByteBuf payload;
}
