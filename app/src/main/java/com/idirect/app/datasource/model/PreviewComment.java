package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PreviewComment {

    @SerializedName("pk")
    @Expose
    private long pk;
    @SerializedName("user_id")
    @Expose
    private long userId;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("type")
    @Expose
    private int type;
    @SerializedName("created_at")
    @Expose
    private long createdAt;
    @SerializedName("created_at_utc")
    @Expose
    private long createdAtUtc;
    @SerializedName("content_type")
    @Expose
    private String contentType;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("bit_flags")
    @Expose
    private int bitFlags;
    @SerializedName("did_report_as_spam")
    @Expose
    private Boolean didReportAsSpam;
    @SerializedName("share_enabled")
    @Expose
    private Boolean shareEnabled;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("media_id")
    @Expose
    private long mediaId;
    @SerializedName("has_translation")
    @Expose
    private Boolean hasTranslation;
    @SerializedName("parent_comment_id")
    @Expose
    private long parentCommentId;

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedAtUtc() {
        return createdAtUtc;
    }

    public void setCreatedAtUtc(long createdAtUtc) {
        this.createdAtUtc = createdAtUtc;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBitFlags() {
        return bitFlags;
    }

    public void setBitFlags(int bitFlags) {
        this.bitFlags = bitFlags;
    }

    public Boolean getDidReportAsSpam() {
        return didReportAsSpam;
    }

    public void setDidReportAsSpam(Boolean didReportAsSpam) {
        this.didReportAsSpam = didReportAsSpam;
    }

    public Boolean getShareEnabled() {
        return shareEnabled;
    }

    public void setShareEnabled(Boolean shareEnabled) {
        this.shareEnabled = shareEnabled;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getMediaId() {
        return mediaId;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }

    public Boolean getHasTranslation() {
        return hasTranslation;
    }

    public void setHasTranslation(Boolean hasTranslation) {
        this.hasTranslation = hasTranslation;
    }

    public long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}
