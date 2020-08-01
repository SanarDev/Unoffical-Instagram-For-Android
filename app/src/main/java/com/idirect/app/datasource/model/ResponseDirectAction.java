package com.idirect.app.datasource.model;

import com.google.gson.annotations.SerializedName;

public class ResponseDirectAction {

    @SerializedName("action")
    private String action;
    @SerializedName("status_code")
    private String statusCode;
    @SerializedName("status")
    private String status;
    @SerializedName("payload")
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
