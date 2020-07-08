package com.sanardev.instagrammqtt.datasource.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FriendshipStatus {

    @SerializedName("following")
    @Expose
    private boolean following;
    @SerializedName("followed_by")
    @Expose
    private boolean followedBt;
    @SerializedName("blocking")
    @Expose
    private boolean blocking;
    @SerializedName("is_private")
    @Expose
    private boolean isPrivate;
    @SerializedName("muting")
    @Expose
    private boolean muting;
    @SerializedName("incoming_request")
    @Expose
    private boolean incomingRequest;
    @SerializedName("outgoing_request")
    @Expose
    private boolean outgoingRequest;
    @SerializedName("is_bestie")
    @Expose
    private boolean isBestie;
    @SerializedName("is_restricted")
    @Expose
    private boolean isRestricted;

    public Boolean getFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    public Boolean getBlocking() {
        return blocking;
    }

    public void setBlocking(Boolean blocking) {
        this.blocking = blocking;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Boolean getIncomingRequest() {
        return incomingRequest;
    }

    public void setIncomingRequest(Boolean incomingRequest) {
        this.incomingRequest = incomingRequest;
    }

    public Boolean getOutgoingRequest() {
        return outgoingRequest;
    }

    public void setOutgoingRequest(Boolean outgoingRequest) {
        this.outgoingRequest = outgoingRequest;
    }

    public Boolean getIsBestie() {
        return isBestie;
    }

    public void setIsBestie(Boolean isBestie) {
        this.isBestie = isBestie;
    }

    public Boolean getIsRestricted() {
        return isRestricted;
    }

    public void setIsRestricted(Boolean isRestricted) {
        this.isRestricted = isRestricted;
    }

}