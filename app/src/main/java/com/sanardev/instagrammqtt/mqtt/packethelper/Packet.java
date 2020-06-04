package com.sanardev.instagrammqtt.mqtt.packethelper;

public abstract class Packet {

    private int packetType;

    public int getPacketType() {
        return packetType;
    }

    public void setPacketType(int packetType) {
        this.packetType = packetType;
    }
}
