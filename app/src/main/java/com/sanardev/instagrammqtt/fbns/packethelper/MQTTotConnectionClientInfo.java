package com.sanardev.instagrammqtt.fbns.packethelper;

public class MQTTotConnectionClientInfo {
    public long userId;
    public long clientCapabilities;
    public long endpointCapabilities;
    public long appId;
    public long anotherUnknown;
    public long clientMqttSessionId = System.currentTimeMillis();

    public boolean noAutomaticForeground;
    public boolean makeUserAvailableInForeground;
    public boolean isInitiallyForeground;
    public boolean overrideNectarLogging;

    public byte networkType;
    public byte networkSubtype;
    public byte clientStack;
    private byte fbnsConnectionKey;
    public byte publishFormat;

    public int[] subscribeTopics;

    public String deviceId;
    public String userAgent;
    public String clientIpAddress;
    public String clientType;
    public String connectTokenHash;
    public String regionPreference;
    public String deviceSecret;
    public String fbnsConnectionSecret;
    public String fbnsDeviceId;
    public String fbnsDeviceSecret;
}
