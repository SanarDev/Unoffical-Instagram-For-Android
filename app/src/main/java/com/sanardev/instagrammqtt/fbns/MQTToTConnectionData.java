package com.sanardev.instagrammqtt.fbns;

import com.microsoft.thrifty.Struct;
import com.microsoft.thrifty.protocol.Protocol;

import java.io.IOException;

public class MQTToTConnectionData  implements Struct {

    /*
     clientIdentifier: string;
    willTopic: string;
    willMessage: string;
    clientInfo: MQTToTConnectionClientInfo;
    password: string;
    unknown: number;
    appSpecificInfo: MQTToTConnectionAppSpecificInfo;
     */
    private String clientIdentifier;
    private String willTopic;
    private String willMessage;
    private MQTTotConnectionClientInfo clientInfo;
    private String password;
    private long unknown;
    private MQTToTConnectionAppSpecificInfo appSpecificInfo;

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public String getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(String willMessage) {
        this.willMessage = willMessage;
    }

    public MQTTotConnectionClientInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(MQTTotConnectionClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getUnknown() {
        return unknown;
    }

    public void setUnknown(long unknown) {
        this.unknown = unknown;
    }

    public MQTToTConnectionAppSpecificInfo getAppSpecificInfo() {
        return appSpecificInfo;
    }

    public void setAppSpecificInfo(MQTToTConnectionAppSpecificInfo appSpecificInfo) {
        this.appSpecificInfo = appSpecificInfo;
    }

    @Override
    public void write(Protocol protocol) throws IOException {
    }
}
