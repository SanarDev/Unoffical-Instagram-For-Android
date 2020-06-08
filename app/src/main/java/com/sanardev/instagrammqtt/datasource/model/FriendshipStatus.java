package com.sanardev.instagrammqtt.datasource.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FriendshipStatus {

    @SerializedName("following")
    @Expose
    private Boolean following;
    @SerializedName("blocking")
    @Expose
    private Boolean blocking;
    @SerializedName("is_private")
    @Expose
    private Boolean isPrivate;
    @SerializedName("incoming_request")
    @Expose
    private Boolean incomingRequest;
    @SerializedName("outgoing_request")
    @Expose
    private Boolean outgoingRequest;
    @SerializedName("is_bestie")
    @Expose
    private Boolean isBestie;
    @SerializedName("is_restricted")
    @Expose
    private Boolean isRestricted;

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