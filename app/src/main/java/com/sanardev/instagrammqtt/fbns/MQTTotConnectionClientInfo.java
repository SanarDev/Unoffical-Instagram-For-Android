package com.sanardev.instagrammqtt.fbns;

public class MQTTotConnectionClientInfo {

    private int userId;
    private String userAgent;
    private int clientCapabilities;
    private int endpointCapabilities;
    private long publishFormat;
    private boolean noAutomaticForeground;
    private boolean makeUserAvailableInForeground;
    private String deviceId;
    private boolean isInitiallyForeground;
    private long networkType;
    private long networkSubtype;
    private int clientMqttSessionId;
    private long[] subscribeTopics;
    private String clientType;
    private int appId;
    private String regionPreference;
    private String deviceSecret;
    private long clientStack;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getClientCapabilities() {
        return clientCapabilities;
    }

    public void setClientCapabilities(int clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    public int getEndpointCapabilities() {
        return endpointCapabilities;
    }

    public void setEndpointCapabilities(int endpointCapabilities) {
        this.endpointCapabilities = endpointCapabilities;
    }

    public long getPublishFormat() {
        return publishFormat;
    }

    public void setPublishFormat(long publishFormat) {
        this.publishFormat = publishFormat;
    }

    public boolean isNoAutomaticForeground() {
        return noAutomaticForeground;
    }

    public void setNoAutomaticForeground(boolean noAutomaticForeground) {
        this.noAutomaticForeground = noAutomaticForeground;
    }

    public boolean isMakeUserAvailableInForeground() {
        return makeUserAvailableInForeground;
    }

    public void setMakeUserAvailableInForeground(boolean makeUserAvailableInForeground) {
        this.makeUserAvailableInForeground = makeUserAvailableInForeground;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isInitiallyForeground() {
        return isInitiallyForeground;
    }

    public void setInitiallyForeground(boolean initiallyForeground) {
        isInitiallyForeground = initiallyForeground;
    }

    public long getNetworkType() {
        return networkType;
    }

    public void setNetworkType(long networkType) {
        this.networkType = networkType;
    }

    public long getNetworkSubtype() {
        return networkSubtype;
    }

    public void setNetworkSubtype(long networkSubtype) {
        this.networkSubtype = networkSubtype;
    }

    public int getClientMqttSessionId() {
        return clientMqttSessionId;
    }

    public void setClientMqttSessionId(int clientMqttSessionId) {
        this.clientMqttSessionId = clientMqttSessionId;
    }

    public long[] getSubscribeTopics() {
        return subscribeTopics;
    }

    public void setSubscribeTopics(long[] subscribeTopics) {
        this.subscribeTopics = subscribeTopics;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getRegionPreference() {
        return regionPreference;
    }

    public void setRegionPreference(String regionPreference) {
        this.regionPreference = regionPreference;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public long getClientStack() {
        return clientStack;
    }

    public void setClientStack(long clientStack) {
        this.clientStack = clientStack;
    }
}
