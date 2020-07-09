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
    @SerializedName("link")
    @Expose
    private Link link;
    @SerializedName("reel_share")
    @Expose
    private ReelShare reelShare;
    @SerializedName("action_log")
    @Expose
    private ActionLog actionLog;
    @SerializedName("path")
    @Expose
    private String path;
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
    @SerializedName("felix_share")
    @Expose
    private Object felixShare;
    @SerializedName("animated_media")
    @Expose
    private AnimatedMedia animatedMedia;
    @SerializedName("like")
    @Expose
    private String like;
    @SerializedName("reactions")
    @Expose
    private DirectReactions reactions;
    @SerializedName("is_delivered")
    @Expose
    private boolean isDelivered = true;

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public DirectReactions getReactions() {
        return reactions;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public void setReactions(DirectReactions reactions) {
        this.reactions = reactions;
    }

    public Object getFelixShare() {
        return felixShare;
    }

    public void setFelixShare(Object felixShare) {
        this.felixShare = felixShare;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public RavenMedia getRavenMedia() {
        return ravenMedia;
    }


    public AnimatedMedia getAnimatedMedia() {
        return animatedMedia;
    }

    public void setAnimatedMedia(AnimatedMedia animatedMedia) {
        this.animatedMedia = animatedMedia;
    }

    public void setRavenMedia(RavenMedia ravenMedia) {
        this.ravenMedia = ravenMedia;
    }

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