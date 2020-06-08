package com.sanardev.instagrammqtt.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReelShare {


    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("reel_owner_id")
    @Expose
    private Long reelOwnerId;
    @SerializedName("is_reel_persisted")
    @Expose
    private boolean isReelPersisted;
    @SerializedName("mentioned_user_id")
    @Expose
    private Long mentionedUserId;
    @SerializedName("reel_name")
    @Expose
    private String reelName;
    @SerializedName("reel_id")
    @Expose
    private String reelId;
    @SerializedName("reel_type")
    @Expose
    private String reelType;
    @SerializedName("media")
    @Expose
    private Media media;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getReelOwnerId() {
        return reelOwnerId;
    }

    public void setReelOwnerId(Long reelOwnerId) {
        this.reelOwnerId = reelOwnerId;
    }

    public boolean isIsReelPersisted() {
        return isReelPersisted;
    }

    public void setIsReelPersisted(boolean isReelPersisted) {
        this.isReelPersisted = isReelPersisted;
    }

    public Long getMentionedUserId() {
        return mentionedUserId;
    }

    public void setMentionedUserId(Long mentionedUserId) {
        this.mentionedUserId = mentionedUserId;
    }

    public String getReelName() {
        return reelName;
    }

    public void setReelName(String reelName) {
        this.reelName = reelName;
    }

    public String getReelId() {
        return reelId;
    }

    public void setReelId(String reelId) {
        this.reelId = reelId;
    }

    public String getReelType() {
        return reelType;
    }

    public void setReelType(String reelType) {
        this.reelType = reelType;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

}
