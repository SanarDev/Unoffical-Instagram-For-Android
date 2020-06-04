package com.sanardev.instagrammqtt.mqtt.packethelper;

public class MQTToTConnectionData {

    public String clientIdentifier;
    public String willTopic;
    public String willMessage;
    public MQTTotConnectionClientInfo clientInfo;
    public String password;
    public int unknown;
    public MQTToTConnectionAppSpecificInfo appSpecificInfo;
}
