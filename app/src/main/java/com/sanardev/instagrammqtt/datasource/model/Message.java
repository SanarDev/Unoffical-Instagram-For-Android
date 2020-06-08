package com.sanardev.instagrammqtt.datasource.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("item_id")
    @Expose
    private String itemId;
    @SerializedName("user_id")
    @Expose
    private long userId;
    @SerializedName("timestamp")
    @Expose
    private long timestamp;
    @SerializedName("item_type")
    @Expose
    private String itemType;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("reel_share")
    @Expose
    private ReelShare reelShare;
    @SerializedName("action_log")
    @Expose
    private ActionLog actionLog;

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public RavenMedia getRavenMedia() {
        return ravenMedia;
    }

    public void setRavenMedia(RavenMedia ravenMedia) {
        this.ravenMedia = ravenMedia;
    }

    @SerializedName("media")
    @Expose
    private Media media;
    @SerializedName("visual_media")
    @Expose
    private RavenMedia ravenMedia;
    @SerializedName("voice_media")
    @Expose
    private MediaData voiceMediaData;
    @SerializedName("media_share")
    @Expose
    private MediaShare mediaShare;
    @SerializedName("story_share")
    @Expose
    private StoryShare storyShare;
    @SerializedName("video_call_event")
    @Expose
    private VideoCallEvent videoCallEvent;
    @SerializedName("client_context")
    @Expose
    private String clientContext;
    @SerializedName("show_forward_attribution")
    @Expose
    private Boolean showForwardAttribution;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getClientContext() {
        return clientContext;
    }

    public void setClientContext(String clientContext) {
        this.clientContext = clientContext;
    }

    public Boolean getShowForwardAttribution() {
        return showForwardAttribution;
    }

    public void setShowForwardAttribution(Boolean showForwardAttribution) {
        this.showForwardAttribution = showForwardAttribution;
    }

    public ReelShare getReelShare() {
        return reelShare;
    }

    public void setReelShare(ReelShare reelShare) {
        this.reelShare = reelShare;
    }

    public ActionLog getActionLog() {
        return actionLog;
    }

    public void setActionLog(ActionLog actionLog) {
        this.actionLog = actionLog;
    }

    public MediaShare getMediaShare() {
        return mediaShare;
    }

    public void setMediaShare(MediaShare mediaShare) {
        this.mediaShare = mediaShare;
    }

    public StoryShare getStoryShare() {
        return storyShare;
    }

    public void setStoryShare(StoryShare storyShare) {
        this.storyShare = storyShare;
    }

    public VideoCallEvent getVideoCallEvent() {
        return videoCallEvent;
    }

    public void setVideoCallEvent(VideoCallEvent videoCallEvent) {
        this.videoCallEvent = videoCallEvent;
    }

    public void setVoiceMediaData(MediaData voiceMediaData) {
        this.voiceMediaData = voiceMediaData;
    }

    public MediaData getVoiceMediaData() {
        return this.voiceMediaData;
    }
}