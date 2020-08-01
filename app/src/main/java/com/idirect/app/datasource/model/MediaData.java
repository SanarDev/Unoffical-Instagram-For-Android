package com.idirect.app.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MediaData {

    @SerializedName("media")
    @Expose
    private VoiceMedia voiceMedia;
    @SerializedName("expiring_media_action_summary")
    @Expose
    private ExpiringMediaActionSummary expiringMediaActionSummary;
    @SerializedName("seen_user_ids")
    @Expose
    private List<Long> seenUserIds;
    @SerializedName("view_mode")
    @Expose
    private String viewMode;
    @SerializedName("seen_count")
    @Expose
    private int seenCount;
    @SerializedName("reply_expiring_at_us")
    @Expose
    private Object replyExpiringAtUs;
    @JsonIgnore
    private boolean isLocal=false;
    @JsonIgnore
    private String localFilePath;
    @JsonIgnore
    private int localDuration = 0;

    public int getLocalDuration() {
        return localDuration;
    }

    public void setLocalDuration(int localDuration) {
        this.localDuration = localDuration;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public Object getReplyExpiringAtUs() {
        return replyExpiringAtUs;
    }

    public void setReplyExpiringAtUs(Object replyExpiringAtUs) {
        this.replyExpiringAtUs = replyExpiringAtUs;
    }

    public int getSeenCount() {
        return seenCount;
    }

    public void setSeenCount(int seenCount) {
        this.seenCount = seenCount;
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
    }

    public List<Long> getSeenUserIds() {
        return seenUserIds;
    }

    public void setSeenUserIds(List<Long> seenUserIds) {
        this.seenUserIds = seenUserIds;
    }

    public ExpiringMediaActionSummary getExpiringMediaActionSummary() {
        return expiringMediaActionSummary;
    }

    public void setExpiringMediaActionSummary(ExpiringMediaActionSummary expiringMediaActionSummary) {
        this.expiringMediaActionSummary = expiringMediaActionSummary;
    }

    public void setVoiceMedia(VoiceMedia voiceMedia) {
        this.voiceMedia = voiceMedia;
    }

    public VoiceMedia getVoiceMedia(){
        return this.voiceMedia;
    }
}
