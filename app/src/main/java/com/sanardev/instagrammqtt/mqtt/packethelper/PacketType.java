package com.sanardev.instagrammqtt.mqtt.packethelper;

public class PacketType {

    public static final int Reserved = 0;
    public static final int CONNECT = 1;
    public static final int CONNACK = 2;
    public static final int PUBLISH = 2;
    public static final int PUBACK = 3;
    public static final int PUBREC = 5;
    public static final int PUBREL = 6;
    public static final int PUBCOMP = 7;
    public static final int SUBSCRIBE = 8;
    public static final int SUBACK = 9;
    public static final int UNSUBSCRIBE = 10;
    public static final int UNSUBACK = 11;
    public static final int PINGREQ = 12;
    public static final int PINGRESP = 13;
    public static final int DISCONNECT = 14;
}
