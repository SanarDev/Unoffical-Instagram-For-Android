package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MediaOrAd {

    @SerializedName("taken_at")
    @Expose
    private Long takenAt;
    @SerializedName("pk")
    @Expose
    private Long pk;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("device_timestamp")
    @Expose
    private Long deviceTimestamp;
    @SerializedName("media_type")
    @Expose
    private Long mediaType;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("client_cache_key")
    @Expose
    private String clientCacheKey;
    @SerializedName("filter_type")
    @Expose
    private Long filterType;
    @SerializedName("carousel_media_count")
    @Expose
    private Long carouselMediaCount;
    @SerializedName("carousel_media")
    @Expose
    private List<CarouselMedia> carouselMedia;
    @SerializedName("can_see_insights_as_brand")
    @Expose
    private Boolean canSeeInsightsAsBrand;
    @SerializedName("should_request_ads")
    @Expose
    private Boolean shouldRequestAds;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("can_viewer_reshare")
    @Expose
    private Boolean canViewerReshare;
    @SerializedName("caption_is_edited")
    @Expose
    private Boolean captionIsEdited;
    @SerializedName("comment_likes_enabled")
    @Expose
    private Boolean commentLikesEnabled;
    @SerializedName("comment_threading_enabled")
    @Expose
    private Boolean commentThreadingEnabled;
    @SerializedName("has_more_comments")
    @Expose
    private Boolean hasMoreComments;
    @SerializedName("max_num_visible_preview_comments")
    @Expose
    private Long maxNumVisiblePreviewComments;
    @SerializedName("preview_comments")
    @Expose
    private List<Object> previewComments = null;
    @SerializedName("can_view_more_preview_comments")
    @Expose
    private Boolean canViewMorePreviewComments;
    @SerializedName("comment_count")
    @Expose
    private Long commentCount;
    @SerializedName("inline_composer_display_condition")
    @Expose
    private String inlineComposerDisplayCondition;
    @SerializedName("inline_composer_imp_trigger_time")
    @Expose
    private Long inlineComposerImpTriggerTime;
    @SerializedName("like_count")
    @Expose
    private Long likeCount;
    @SerializedName("has_liked")
    @Expose
    private Boolean hasLiked;
    @SerializedName("top_likers")
    @Expose
    private List<Object> topLikers = null;
    @SerializedName("photo_of_you")
    @Expose
    private Boolean photoOfYou;
    @SerializedName("caption")
    @Expose
    private Caption caption;
    @SerializedName("can_viewer_save")
    @Expose
    private Boolean canViewerSave;
    @SerializedName("organic_tracking_token")
    @Expose
    private String organicTrackingToken;
    @SerializedName("sharing_friction_info")
    @Expose
    private SharingFrictionInfo sharingFrictionInfo;
    @SerializedName("is_in_profile_grid")
    @Expose
    private Boolean isInProfileGrid;
    @SerializedName("profile_grid_control_enabled")
    @Expose
    private Boolean profileGridControlEnabled;
    @SerializedName("deleted_reason")
    @Expose
    private Long deletedReason;
    @SerializedName("main_feed_carousel_starting_media_id")
    @Expose
    private String mainFeedCarouselStartingMediaId;
    @SerializedName("main_feed_carousel_has_unseen_cover_media")
    @Expose
    private Boolean mainFeedCarouselHasUnseenCoverMedia;
    @SerializedName("inventory_source")
    @Expose
    private String inventorySource;
    @SerializedName("is_seen")
    @Expose
    private Boolean isSeen;
    @SerializedName("is_eof")
    @Expose
    private Boolean isEof;

    public Long getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(Long takenAt) {
        this.takenAt = takenAt;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDeviceTimestamp() {
        return deviceTimestamp;
    }

    public void setDeviceTimestamp(Long deviceTimestamp) {
        this.deviceTimestamp = deviceTimestamp;
    }

    public Long getMediaType() {
        return mediaType;
    }

    public void setMediaType(Long mediaType) {
        this.mediaType = mediaType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClientCacheKey() {
        return clientCacheKey;
    }

    public void setClientCacheKey(String clientCacheKey) {
        this.clientCacheKey = clientCacheKey;
    }

    public Long getFilterType() {
        return filterType;
    }

    public void setFilterType(Long filterType) {
        this.filterType = filterType;
    }

    public Long getCarouselMediaCount() {
        return carouselMediaCount;
    }

    public void setCarouselMediaCount(Long carouselMediaCount) {
        this.carouselMediaCount = carouselMediaCount;
    }

    public List<CarouselMedia> getCarouselMedia() {
        return carouselMedia;
    }

    public void setCarouselMedia(List<CarouselMedia> carouselMedia) {
        this.carouselMedia = carouselMedia;
    }

    public Boolean getCanSeeInsightsAsBrand() {
        return canSeeInsightsAsBrand;
    }

    public void setCanSeeInsightsAsBrand(Boolean canSeeInsightsAsBrand) {
        this.canSeeInsightsAsBrand = canSeeInsightsAsBrand;
    }

    public Boolean getShouldRequestAds() {
        return shouldRequestAds;
    }

    public void setShouldRequestAds(Boolean shouldRequestAds) {
        this.shouldRequestAds = shouldRequestAds;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getCanViewerReshare() {
        return canViewerReshare;
    }

    public void setCanViewerReshare(Boolean canViewerReshare) {
        this.canViewerReshare = canViewerReshare;
    }

    public Boolean getCaptionIsEdited() {
        return captionIsEdited;
    }

    public void setCaptionIsEdited(Boolean captionIsEdited) {
        this.captionIsEdited = captionIsEdited;
    }

    public Boolean getCommentLikesEnabled() {
        return commentLikesEnabled;
    }

    public void setCommentLikesEnabled(Boolean commentLikesEnabled) {
        this.commentLikesEnabled = commentLikesEnabled;
    }

    public Boolean getCommentThreadingEnabled() {
        return commentThreadingEnabled;
    }

    public void setCommentThreadingEnabled(Boolean commentThreadingEnabled) {
        this.commentThreadingEnabled = commentThreadingEnabled;
    }

    public Boolean getHasMoreComments() {
        return hasMoreComments;
    }

    public void setHasMoreComments(Boolean hasMoreComments) {
        this.hasMoreComments = hasMoreComments;
    }

    public Long getMaxNumVisiblePreviewComments() {
        return maxNumVisiblePreviewComments;
    }

    public void setMaxNumVisiblePreviewComments(Long maxNumVisiblePreviewComments) {
        this.maxNumVisiblePreviewComments = maxNumVisiblePreviewComments;
    }

    public List<Object> getPreviewComments() {
        return previewComments;
    }

    public void setPreviewComments(List<Object> previewComments) {
        this.previewComments = previewComments;
    }

    public Boolean getCanViewMorePreviewComments() {
        return canViewMorePreviewComments;
    }

    public void setCanViewMorePreviewComments(Boolean canViewMorePreviewComments) {
        this.canViewMorePreviewComments = canViewMorePreviewComments;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public String getInlineComposerDisplayCondition() {
        return inlineComposerDisplayCondition;
    }

    public void setInlineComposerDisplayCondition(String inlineComposerDisplayCondition) {
        this.inlineComposerDisplayCondition = inlineComposerDisplayCondition;
    }

    public Long getInlineComposerImpTriggerTime() {
        return inlineComposerImpTriggerTime;
    }

    public void setInlineComposerImpTriggerTime(Long inlineComposerImpTriggerTime) {
        this.inlineComposerImpTriggerTime = inlineComposerImpTriggerTime;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(Boolean hasLiked) {
        this.hasLiked = hasLiked;
    }

    public List<Object> getTopLikers() {
        return topLikers;
    }

    public void setTopLikers(List<Object> topLikers) {
        this.topLikers = topLikers;
    }

    public Boolean getPhotoOfYou() {
        return photoOfYou;
    }

    public void setPhotoOfYou(Boolean photoOfYou) {
        this.photoOfYou = photoOfYou;
    }

    public Caption getCaption() {
        return caption;
    }

    public void setCaption(Caption caption) {
        this.caption = caption;
    }

    public Boolean getCanViewerSave() {
        return canViewerSave;
    }

    public void setCanViewerSave(Boolean canViewerSave) {
        this.canViewerSave = canViewerSave;
    }

    public String getOrganicTrackingToken() {
        return organicTrackingToken;
    }

    public void setOrganicTrackingToken(String organicTrackingToken) {
        this.organicTrackingToken = organicTrackingToken;
    }

    public SharingFrictionInfo getSharingFrictionInfo() {
        return sharingFrictionInfo;
    }

    public void setSharingFrictionInfo(SharingFrictionInfo sharingFrictionInfo) {
        this.sharingFrictionInfo = sharingFrictionInfo;
    }

    public Boolean getIsInProfileGrid() {
        return isInProfileGrid;
    }

    public void setIsInProfileGrid(Boolean isInProfileGrid) {
        this.isInProfileGrid = isInProfileGrid;
    }

    public Boolean getProfileGridControlEnabled() {
        return profileGridControlEnabled;
    }

    public void setProfileGridControlEnabled(Boolean profileGridControlEnabled) {
        this.profileGridControlEnabled = profileGridControlEnabled;
    }

    public Long getDeletedReason() {
        return deletedReason;
    }

    public void setDeletedReason(Long deletedReason) {
        this.deletedReason = deletedReason;
    }

    public String getMainFeedCarouselStartingMediaId() {
        return mainFeedCarouselStartingMediaId;
    }

    public void setMainFeedCarouselStartingMediaId(String mainFeedCarouselStartingMediaId) {
        this.mainFeedCarouselStartingMediaId = mainFeedCarouselStartingMediaId;
    }

    public Boolean getMainFeedCarouselHasUnseenCoverMedia() {
        return mainFeedCarouselHasUnseenCoverMedia;
    }

    public void setMainFeedCarouselHasUnseenCoverMedia(Boolean mainFeedCarouselHasUnseenCoverMedia) {
        this.mainFeedCarouselHasUnseenCoverMedia = mainFeedCarouselHasUnseenCoverMedia;
    }

    public String getInventorySource() {
        return inventorySource;
    }

    public void setInventorySource(String inventorySource) {
        this.inventorySource = inventorySource;
    }

    public Boolean getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(Boolean isSeen) {
        this.isSeen = isSeen;
    }

    public Boolean getIsEof() {
        return isEof;
    }

    public void setIsEof(Boolean isEof) {
        this.isEof = isEof;
    }

}
