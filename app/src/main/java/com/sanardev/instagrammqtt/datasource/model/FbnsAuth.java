package com.sanardev.instagrammqtt.datasource.model;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class FbnsAuth {


    @SerializedName("ck")
    private long userId;
    @SerializedName("cs")
    private String password = "";
    @SerializedName("di")
    private String deviceID = UUID.randomUUID().toString();
    private String clientID= "";
    @SerializedName("ds")
    private String deviceSecret= "";
    @SerializedName("sr")
    private String sr = "";
    @SerializedName("rc")
    private String rc = "";
    private String token= "";


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return deviceID;
    }

    public void setDeviceId(String deviceId) {
        this.deviceID = deviceId;
        this.clientID = this.deviceID.substring(0,20);
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public String getSr() {
        return sr;
    }

    public void setSr(String sr) {
        this.sr = sr;
    }

    public String getRc() {
        return rc;
    }

    public void setRc(String rc) {
        this.rc = rc;
    }

    public String getClientID() {
        return clientID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
