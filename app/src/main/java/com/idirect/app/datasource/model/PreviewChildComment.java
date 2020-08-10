package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PreviewChildComment {


    @SerializedName("content_type")
    @Expose
    private String contentType;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("pk")
    @Expose
    private Long pk;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("type")
    @Expose
    private Long type;
    @SerializedName("created_at")
    @Expose
    private Long createdAt;
    @SerializedName("created_at_utc")
    @Expose
    private Long createdAtUtc;
    @SerializedName("media_id")
    @Expose
    private Long mediaId;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("parent_comment_id")
    @Expose
    private Long parentCommentId;
    @SerializedName("share_enabled")
    @Expose
    private Boolean shareEnabled;
    @SerializedName("has_liked_comment")
    @Expose
    private Boolean hasLikedComment;
    @SerializedName("comment_like_count")
    @Expose
    private Long commentLikeCount;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedAtUtc() {
        return createdAtUtc;
    }

    public void setCreatedAtUtc(Long createdAtUtc) {
        this.createdAtUtc = createdAtUtc;
    }

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public Boolean getShareEnabled() {
        return shareEnabled;
    }

    public void setShareEnabled(Boolean shareEnabled) {
        this.shareEnabled = shareEnabled;
    }

    public Boolean getHasLikedComment() {
        return hasLikedComment;
    }

    public void setHasLikedComment(Boolean hasLikedComment) {
        this.hasLikedComment = hasLikedComment;
    }

    public Long getCommentLikeCount() {
        return commentLikeCount;
    }

    public void setCommentLikeCount(Long commentLikeCount) {
        this.commentLikeCount = commentLikeCount;
    }

}
