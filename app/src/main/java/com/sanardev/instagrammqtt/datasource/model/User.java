package com.sanardev.instagrammqtt.datasource.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("pk")
    @Expose
    private long pk;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("is_private")
    @Expose
    private Boolean isPrivate;
    @SerializedName("profile_pic_url")
    @Expose
    private String profilePicUrl;
    @SerializedName("profile_pic_id")
    @Expose
    private String profilePicId;
    @SerializedName("friendship_status")
    @Expose
    private FriendshipStatus friendshipStatus;
    @SerializedName("is_verified")
    @Expose
    private Boolean isVerified;
    @SerializedName("has_anonymous_profile_picture")
    @Expose
    private Boolean hasAnonymousProfilePicture;
    @SerializedName("has_threads_app")
    @Expose
    private Boolean hasThreadsApp;
    @SerializedName("is_using_unified_inbox_for_direct")
    @Expose
    private Boolean isUsingUnifiedInboxForDirect;
    @SerializedName("interop_messaging_user_fbid")
    @Expose
    private long interopMessagingUserFbid;

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getProfilePicId() {
        return profilePicId;
    }

    public void setProfilePicId(String profilePicId) {
        this.profilePicId = profilePicId;
    }

    public FriendshipStatus getFriendshipStatus() {
        return friendshipStatus;
    }

    public void setFriendshipStatus(FriendshipStatus friendshipStatus) {
        this.friendshipStatus = friendshipStatus;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Boolean getHasAnonymousProfilePicture() {
        return hasAnonymousProfilePicture;
    }

    public void setHasAnonymousProfilePicture(Boolean hasAnonymousProfilePicture) {
        this.hasAnonymousProfilePicture = hasAnonymousProfilePicture;
    }

    public Boolean getHasThreadsApp() {
        return hasThreadsApp;
    }

    public void setHasThreadsApp(Boolean hasThreadsApp) {
        this.hasThreadsApp = hasThreadsApp;
    }

    public Boolean getIsUsingUnifiedInboxForDirect() {
        return isUsingUnifiedInboxForDirect;
    }

    public void setIsUsingUnifiedInboxForDirect(Boolean isUsingUnifiedInboxForDirect) {
        this.isUsingUnifiedInboxForDirect = isUsingUnifiedInboxForDirect;
    }

    public long getInteropMessagingUserFbid() {
        return interopMessagingUserFbid;
    }

    public void setInteropMessagingUserFbid(long interopMessagingUserFbid) {
        this.interopMessagingUserFbid = interopMessagingUserFbid;
    }

}