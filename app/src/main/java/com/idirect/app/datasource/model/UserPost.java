package com.idirect.app.datasource.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserPost implements Parcelable {

    @SerializedName("taken_at")
    @Expose
    private long takenAt;
    @SerializedName("pk")
    @Expose
    private long pk;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("device_timestamp")
    @Expose
    private long deviceTimestamp;
    @SerializedName("media_type")
    @Expose
    private int mediaType;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("client_cache_key")
    @Expose
    private String clientCacheKey;
    @SerializedName("filter_type")
    @Expose
    private int filterType;
    @SerializedName("image_versions2")
    @Expose
    private ImageVersions2 imageVersions2;
    @SerializedName("video_versions")
    @Expose
    private List<VideoVersion> videoVersions;
    @SerializedName("carousel_media_count")
    @Expose
    private int carouselMediaCount;
    @SerializedName("carousel_media")
    @Expose
    private List<CarouselMedia> carouselMedias;
    @SerializedName("original_width")
    @Expose
    private int originalWidth;
    @SerializedName("original_height")
    @Expose
    private int originalHeight;
    @SerializedName("location")
    @Expose
    private Location location;
    @SerializedName("lat")
    @Expose
    private double lat;
    @SerializedName("lng")
    @Expose
    private double lng;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("can_viewer_reshare")
    @Expose
    private boolean canViewerReshare;
    @SerializedName("caption_is_edited")
    @Expose
    private boolean captionIsEdited;
    @SerializedName("comment_likes_enabled")
    @Expose
    private boolean commentLikesEnabled;
    @SerializedName("comment_threading_enabled")
    @Expose
    private boolean commentThreadingEnabled;
    @SerializedName("hash_more_comments")
    @Expose
    private boolean hashMoreComments;
    @SerializedName("preview_comments")
    @Expose
    private List<PreviewComment> previewComments;
    @SerializedName("max_num_visible_preview_comments")
    @Expose
    private int maxNumVisiblePreviewComments;
    @SerializedName("can_view_more_preview_comments")
    @Expose
    private boolean canViewMorePreviewComments;
    @SerializedName("comment_count")
    @Expose
    private int commentCount;
    @SerializedName("like_count")
    @Expose
    private int likeCount;
    @SerializedName("inline_composer_display_condition")
    @Expose
    private String inlineComposerDisplayCondition;
    @SerializedName("inline_composer_imp_trigger_time")
    @Expose
    private int inlineComposerImpTriggerTime;
    @SerializedName("has_liked")
    @Expose
    private boolean hasLiked;
    @SerializedName("top_likers")
    @Expose
    private List<String> topLikers;
    @SerializedName("facepile_top_likers")
    @Expose
    private List<User> facepileTopLikers;
    @SerializedName("photo_of_you")
    @Expose
    private boolean photoOfYou;
    @SerializedName("usertags")
    @Expose
    private Usertags usertags;
    @SerializedName("can_viewer_save")
    @Expose
    private boolean canViewerSave;
    @SerializedName("organic_tracking_token")
    @Expose
    private String organicTrackingToken;
    @SerializedName("is_in_profile_grid")
    @Expose
    private boolean isInProfileGrid;
    @SerializedName("profile_grid_control_enabled")
    @Expose
    private boolean profileGridControlEnabled;
    @SerializedName("caption")
    @Expose
    private Caption caption;
    @SerializedName("parent_comment_id")
    @Expose
    private long parentCommentId;

    protected UserPost(Parcel in) {
        takenAt = in.readLong();
        pk = in.readLong();
        id = in.readString();
        deviceTimestamp = in.readLong();
        mediaType = in.readInt();
        code = in.readString();
        clientCacheKey = in.readString();
        filterType = in.readInt();
        carouselMediaCount = in.readInt();
        originalWidth = in.readInt();
        originalHeight = in.readInt();
        lat = in.readDouble();
        lng = in.readDouble();
        canViewerReshare = in.readByte() != 0;
        captionIsEdited = in.readByte() != 0;
        commentLikesEnabled = in.readByte() != 0;
        commentThreadingEnabled = in.readByte() != 0;
        hashMoreComments = in.readByte() != 0;
        maxNumVisiblePreviewComments = in.readInt();
        canViewMorePreviewComments = in.readByte() != 0;
        commentCount = in.readInt();
        likeCount = in.readInt();
        inlineComposerDisplayCondition = in.readString();
        inlineComposerImpTriggerTime = in.readInt();
        hasLiked = in.readByte() != 0;
        topLikers = in.createStringArrayList();
        photoOfYou = in.readByte() != 0;
        canViewerSave = in.readByte() != 0;
        organicTrackingToken = in.readString();
        isInProfileGrid = in.readByte() != 0;
        profileGridControlEnabled = in.readByte() != 0;
        parentCommentId = in.readLong();
        productType = in.readString();
    }

    public static final Creator<UserPost> CREATOR = new Creator<UserPost>() {
        @Override
        public UserPost createFromParcel(Parcel in) {
            return new UserPost(in);
        }

        @Override
        public UserPost[] newArray(int size) {
            return new UserPost[size];
        }
    };

    public long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
    @SerializedName("product_type")
    @Expose
    private String productType;

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public List<PreviewComment> getPreviewComments() {
        return previewComments;
    }

    public void setPreviewComments(List<PreviewComment> previewComments) {
        this.previewComments = previewComments;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public Caption getCaption() {
        return caption;
    }

    public void setCaption(Caption caption) {
        this.caption = caption;
    }

    public long getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(long takenAt) {
        this.takenAt = takenAt;
    }

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDeviceTimestamp() {
        return deviceTimestamp;
    }

    public void setDeviceTimestamp(long deviceTimestamp) {
        this.deviceTimestamp = deviceTimestamp;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
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

    public int getFilterType() {
        return filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public ImageVersions2 getImageVersions2() {
        return imageVersions2;
    }

    public void setImageVersions2(ImageVersions2 imageVersions2) {
        this.imageVersions2 = imageVersions2;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isCanViewerReshare() {
        return canViewerReshare;
    }

    public void setCanViewerReshare(boolean canViewerReshare) {
        this.canViewerReshare = canViewerReshare;
    }

    public boolean isCaptionIsEdited() {
        return captionIsEdited;
    }

    public void setCaptionIsEdited(boolean captionIsEdited) {
        this.captionIsEdited = captionIsEdited;
    }

    public boolean isCommentLikesEnabled() {
        return commentLikesEnabled;
    }

    public void setCommentLikesEnabled(boolean commentLikesEnabled) {
        this.commentLikesEnabled = commentLikesEnabled;
    }

    public boolean isCommentThreadingEnabled() {
        return commentThreadingEnabled;
    }

    public void setCommentThreadingEnabled(boolean commentThreadingEnabled) {
        this.commentThreadingEnabled = commentThreadingEnabled;
    }

    public boolean isHashMoreComments() {
        return hashMoreComments;
    }

    public void setHashMoreComments(boolean hashMoreComments) {
        this.hashMoreComments = hashMoreComments;
    }

    public int getMaxNumVisiblePreviewComments() {
        return maxNumVisiblePreviewComments;
    }

    public void setMaxNumVisiblePreviewComments(int maxNumVisiblePreviewComments) {
        this.maxNumVisiblePreviewComments = maxNumVisiblePreviewComments;
    }

    public boolean isCanViewMorePreviewComments() {
        return canViewMorePreviewComments;
    }

    public void setCanViewMorePreviewComments(boolean canViewMorePreviewComments) {
        this.canViewMorePreviewComments = canViewMorePreviewComments;
    }


    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getInlineComposerDisplayCondition() {
        return inlineComposerDisplayCondition;
    }

    public void setInlineComposerDisplayCondition(String inlineComposerDisplayCondition) {
        this.inlineComposerDisplayCondition = inlineComposerDisplayCondition;
    }

    public int getInlineComposerImpTriggerTime() {
        return inlineComposerImpTriggerTime;
    }

    public void setInlineComposerImpTriggerTime(int inlineComposerImpTriggerTime) {
        this.inlineComposerImpTriggerTime = inlineComposerImpTriggerTime;
    }

    public boolean isHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked) {
        this.hasLiked = hasLiked;
    }

    public List<String> getTopLikers() {
        return topLikers;
    }

    public void setTopLikers(List<String> topLikers) {
        this.topLikers = topLikers;
    }

    public List<User> getFacepileTopLikers() {
        return facepileTopLikers;
    }

    public void setFacepileTopLikers(List<User> facepileTopLikers) {
        this.facepileTopLikers = facepileTopLikers;
    }

    public boolean isPhotoOfYou() {
        return photoOfYou;
    }

    public void setPhotoOfYou(boolean photoOfYou) {
        this.photoOfYou = photoOfYou;
    }

    public Usertags getUsertags() {
        return usertags;
    }

    public void setUsertags(Usertags usertags) {
        this.usertags = usertags;
    }

    public boolean isCanViewerSave() {
        return canViewerSave;
    }

    public void setCanViewerSave(boolean canViewerSave) {
        this.canViewerSave = canViewerSave;
    }

    public String getOrganicTrackingToken() {
        return organicTrackingToken;
    }

    public void setOrganicTrackingToken(String organicTrackingToken) {
        this.organicTrackingToken = organicTrackingToken;
    }

    public boolean isInProfileGrid() {
        return isInProfileGrid;
    }

    public void setInProfileGrid(boolean inProfileGrid) {
        isInProfileGrid = inProfileGrid;
    }

    public boolean isProfileGridControlEnabled() {
        return profileGridControlEnabled;
    }

    public void setProfileGridControlEnabled(boolean profileGridControlEnabled) {
        this.profileGridControlEnabled = profileGridControlEnabled;
    }

    public List<VideoVersion> getVideoVersions() {
        return videoVersions;
    }

    public void setVideoVersions(List<VideoVersion> videoVersions) {
        this.videoVersions = videoVersions;
    }

    public int getCarouselMediaCount() {
        return carouselMediaCount;
    }

    public void setCarouselMediaCount(int carouselMediaCount) {
        this.carouselMediaCount = carouselMediaCount;
    }

    public List<CarouselMedia> getCarouselMedias() {
        return carouselMedias;
    }

    public void setCarouselMedias(List<CarouselMedia> carouselMedias) {
        this.carouselMedias = carouselMedias;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(takenAt);
        dest.writeLong(pk);
        dest.writeString(id);
        dest.writeLong(deviceTimestamp);
        dest.writeInt(mediaType);
        dest.writeString(code);
        dest.writeString(clientCacheKey);
        dest.writeInt(filterType);
        dest.writeInt(carouselMediaCount);
        dest.writeInt(originalWidth);
        dest.writeInt(originalHeight);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeByte((byte) (canViewerReshare ? 1 : 0));
        dest.writeByte((byte) (captionIsEdited ? 1 : 0));
        dest.writeByte((byte) (commentLikesEnabled ? 1 : 0));
        dest.writeByte((byte) (commentThreadingEnabled ? 1 : 0));
        dest.writeByte((byte) (hashMoreComments ? 1 : 0));
        dest.writeInt(maxNumVisiblePreviewComments);
        dest.writeByte((byte) (canViewMorePreviewComments ? 1 : 0));
        dest.writeInt(commentCount);
        dest.writeInt(likeCount);
        dest.writeString(inlineComposerDisplayCondition);
        dest.writeInt(inlineComposerImpTriggerTime);
        dest.writeByte((byte) (hasLiked ? 1 : 0));
        dest.writeStringList(topLikers);
        dest.writeByte((byte) (photoOfYou ? 1 : 0));
        dest.writeByte((byte) (canViewerSave ? 1 : 0));
        dest.writeString(organicTrackingToken);
        dest.writeByte((byte) (isInProfileGrid ? 1 : 0));
        dest.writeByte((byte) (profileGridControlEnabled ? 1 : 0));
        dest.writeLong(parentCommentId);
        dest.writeString(productType);
    }
}
