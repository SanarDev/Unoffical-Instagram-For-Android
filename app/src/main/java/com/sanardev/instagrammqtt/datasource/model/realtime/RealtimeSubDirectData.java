package com.sanardev.instagrammqtt.datasource.model.realtime;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RealtimeSubDirectData {

    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("sender_id")
    @Expose
    private String senderId;
    @SerializedName("sender_id")
    @Expose
    private int activity_status;
    @SerializedName("ttl")
    @Expose
    private int ttl;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public int getActivity_status() {
        return activity_status;
    }

    public void setActivity_status(int activity_status) {
        this.activity_status = activity_status;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }
}
