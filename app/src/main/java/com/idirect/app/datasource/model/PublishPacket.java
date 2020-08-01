package com.idirect.app.datasource.model;

import io.netty.buffer.ByteBuf;

public class PublishPacket {

    private ByteBuf payload;
    private String topicName;
    private int packetId;

    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }
}
