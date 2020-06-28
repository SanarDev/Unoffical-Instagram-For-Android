package com.sanardev.instagrammqtt.fbns.packethelper;

import java.util.HashMap;

public class MQTToTConnectionData {

    public String clientIdentifier;
    public String willTopic;
    public String willMessage;
    public MQTTotConnectionClientInfo clientInfo;
    public String password;
    public int unknown;
    public HashMap<String,String> appSpecificInfo;
}
