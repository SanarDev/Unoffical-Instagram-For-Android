package com.idirect.app.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.SerializedName;

public class PushNotification {

    @SerializedName("t")
    private String title;
    @SerializedName("m")
    private String message;
    @SerializedName("tt")
    private String ticketText;
    @SerializedName("ig")
    private String igAction;
    @SerializedName("collapse_key")
    private String collapseKey;
    @SerializedName("i")
    private String optionImage;
    @SerializedName("a")
    private String optionAvatarUrl;
    @SerializedName("sound")
    private String sound;
    @SerializedName("pi")
    private String pushID;
    @SerializedName("c")
    private String pushCategory;
    @SerializedName("u")
    private String intendedRecipientUserId;
    @JsonIgnore()
    private String intendedRecipientUserName;
    @SerializedName("s")
    private String sourceUserId;
    @SerializedName("igo")
    private String igActionOverride;
    @SerializedName("ia")
    private String inAppActors;
    @SerializedName("bc")
    private String badgeCountJson;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTicketText() {
        return ticketText;
    }

    public void setTicketText(String ticketText) {
        this.ticketText = ticketText;
    }

    public String getIgAction() {
        return igAction;
    }

    public void setIgAction(String igAction) {
        this.igAction = igAction;
    }

    public String getCollapseKey() {
        return collapseKey;
    }

    public void setCollapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
    }

    public String getOptionImage() {
        return optionImage;
    }

    public void setOptionImage(String optionImage) {
        this.optionImage = optionImage;
    }

    public String getOptionAvatarUrl() {
        return optionAvatarUrl;
    }

    public void setOptionAvatarUrl(String optionAvatarUrl) {
        this.optionAvatarUrl = optionAvatarUrl;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getPushID() {
        return pushID;
    }

    public void setPushID(String pushID) {
        this.pushID = pushID;
    }

    public String getPushCategory() {
        return pushCategory;
    }

    public void setPushCategory(String pushCategory) {
        this.pushCategory = pushCategory;
    }

    public String getIntendedRecipientUserId() {
        return intendedRecipientUserId;
    }

    public void setIntendedRecipientUserId(String intendedRecipientUserId) {
        this.intendedRecipientUserId = intendedRecipientUserId;
    }

    public String getIntendedRecipientUserName() {
        return intendedRecipientUserName;
    }

    public void setIntendedRecipientUserName(String intendedRecipientUserName) {
        this.intendedRecipientUserName = intendedRecipientUserName;
    }

    public String getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(String sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public String getIgActionOverride() {
        return igActionOverride;
    }

    public void setIgActionOverride(String igActionOverride) {
        this.igActionOverride = igActionOverride;
    }

    public String getInAppActors() {
        return inAppActors;
    }

    public void setInAppActors(String inAppActors) {
        this.inAppActors = inAppActors;
    }

    public String getBadgeCountJson() {
        return badgeCountJson;
    }

    public void setBadgeCountJson(String badgeCountJson) {
        this.badgeCountJson = badgeCountJson;
    }
}
