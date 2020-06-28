package com.sanardev.instagrammqtt.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class NotificationContentJson {

    @SerializedName("token")
    private String token;
    @SerializedName("ck")
    private String connectionKey;
    @SerializedName("pn")
    private String packageName;
    @SerializedName("cp")
    private String collapseKey;
    @SerializedName("fbpushnotif")
    private String fbpushnotif;
    @SerializedName("nid")
    private String notificationID;
    @SerializedName("bu")
    private String isBuffered;
    @JsonIgnore
    private PushNotification notificationContent;

    public String getFbpushnotif() {
        return fbpushnotif;
    }

    public void setFbpushnotif(String fbpushnotif) {
        this.fbpushnotif = fbpushnotif;
    }

    public PushNotification getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(PushNotification notificationContent) {
        this.notificationContent = notificationContent;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getConnectionKey() {
        return connectionKey;
    }

    public void setConnectionKey(String connectionKey) {
        this.connectionKey = connectionKey;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getCollapseKey() {
        return collapseKey;
    }

    public void setCollapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
    }


    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getIsBuffered() {
        return isBuffered;
    }

    public void setIsBuffered(String isBuffered) {
        this.isBuffered = isBuffered;
    }
}
