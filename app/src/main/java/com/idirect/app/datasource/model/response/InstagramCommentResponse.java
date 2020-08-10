package com.idirect.app.datasource.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.idirect.app.datasource.model.Caption;
import com.idirect.app.datasource.model.Comment;
import com.idirect.app.datasource.model.PreviewComment;
import com.idirect.app.datasource.model.QuickResponseEmoji;
import com.idirect.app.datasource.model.payload.StatusResult;

import java.util.List;

public class InstagramCommentResponse extends StatusResult {

    @SerializedName("comment_likes_enabled")
    @Expose
    private Boolean commentLikesEnabled;
    @SerializedName("comments")
    @Expose
    private List<Comment> comments = null;
    @SerializedName("comment_count")
    @Expose
    private Long commentCount;
    @SerializedName("caption")
    @Expose
    private Caption caption;
    @SerializedName("caption_is_edited")
    @Expose
    private Boolean captionIsEdited;
    @SerializedName("has_more_comments")
    @Expose
    private Boolean hasMoreComments;
    @SerializedName("has_more_headload_comments")
    @Expose
    private Boolean hasMoreHeadloadComments;
    @SerializedName("threading_enabled")
    @Expose
    private Boolean threadingEnabled;
    @SerializedName("media_header_display")
    @Expose
    private String mediaHeaderDisplay;
    @SerializedName("display_realtime_typing_indicator")
    @Expose
    private Boolean displayRealtimeTypingIndicator;
    @SerializedName("quick_response_emojis")
    @Expose
    private List<QuickResponseEmoji> quickResponseEmojis = null;
    @SerializedName("preview_comments")
    @Expose
    private List<PreviewComment> previewComments = null;
    @SerializedName("can_view_more_preview_comments")
    @Expose
    private Boolean canViewMorePreviewComments;
    @SerializedName("next_max_id")
    @Expose
    private String nextMaxId;
    @JsonIgnore
    private String mediaId;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public Boolean getCommentLikesEnabled() {
        return commentLikesEnabled;
    }

    public void setCommentLikesEnabled(Boolean commentLikesEnabled) {
        this.commentLikesEnabled = commentLikesEnabled;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Caption getCaption() {
        return caption;
    }

    public void setCaption(Caption caption) {
        this.caption = caption;
    }

    public Boolean getCaptionIsEdited() {
        return captionIsEdited;
    }

    public void setCaptionIsEdited(Boolean captionIsEdited) {
        this.captionIsEdited = captionIsEdited;
    }

    public Boolean getHasMoreComments() {
        return hasMoreComments;
    }

    public void setHasMoreComments(Boolean hasMoreComments) {
        this.hasMoreComments = hasMoreComments;
    }

    public Boolean getHasMoreHeadloadComments() {
        return hasMoreHeadloadComments;
    }

    public void setHasMoreHeadloadComments(Boolean hasMoreHeadloadComments) {
        this.hasMoreHeadloadComments = hasMoreHeadloadComments;
    }

    public Boolean getThreadingEnabled() {
        return threadingEnabled;
    }

    public void setThreadingEnabled(Boolean threadingEnabled) {
        this.threadingEnabled = threadingEnabled;
    }

    public String getMediaHeaderDisplay() {
        return mediaHeaderDisplay;
    }

    public void setMediaHeaderDisplay(String mediaHeaderDisplay) {
        this.mediaHeaderDisplay = mediaHeaderDisplay;
    }

    public Boolean getDisplayRealtimeTypingIndicator() {
        return displayRealtimeTypingIndicator;
    }

    public void setDisplayRealtimeTypingIndicator(Boolean displayRealtimeTypingIndicator) {
        this.displayRealtimeTypingIndicator = displayRealtimeTypingIndicator;
    }

    public List<QuickResponseEmoji> getQuickResponseEmojis() {
        return quickResponseEmojis;
    }

    public void setQuickResponseEmojis(List<QuickResponseEmoji> quickResponseEmojis) {
        this.quickResponseEmojis = quickResponseEmojis;
    }

    public List<PreviewComment> getPreviewComments() {
        return previewComments;
    }

    public void setPreviewComments(List<PreviewComment> previewComments) {
        this.previewComments = previewComments;
    }

    public Boolean getCanViewMorePreviewComments() {
        return canViewMorePreviewComments;
    }

    public void setCanViewMorePreviewComments(Boolean canViewMorePreviewComments) {
        this.canViewMorePreviewComments = canViewMorePreviewComments;
    }

    public String getNextMaxId() {
        return nextMaxId;
    }

    public void setNextMaxId(String nextMaxId) {
        this.nextMaxId = nextMaxId;
    }
}
