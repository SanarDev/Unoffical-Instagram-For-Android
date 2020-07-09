package com.sanardev.instagrammqtt.datasource.model.event;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sanardev.instagrammqtt.datasource.model.Payload;

public class MessageResponseEvent {

    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("status_code")
    @Expose
    private String statusCode;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("payload")
    @Expose
    private Payload payload;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
