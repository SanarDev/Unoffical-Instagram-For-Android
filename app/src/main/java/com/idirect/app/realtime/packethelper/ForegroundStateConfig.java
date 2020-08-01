package com.idirect.app.realtime.packethelper;

public class ForegroundStateConfig {

    public boolean inForegroundApp;
    public boolean inForegroundDevice;
    public int keepAliveTimeOut;
    public String[] subscribeTopics;
    public String[] unsubscribeTopics;
    public String[] subscribeGenericTopics;
    public String[] unsubscribeGenericTopics;
    public long requestId;
}
