package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LinkedFbInfo {


    @SerializedName("linked_fb_user")
    @Expose
    private LinkedFbUser linkedFbUser;

    public LinkedFbUser getLinkedFbUser() {
        return linkedFbUser;
    }

    public void setLinkedFbUser(LinkedFbUser linkedFbUser) {
        this.linkedFbUser = linkedFbUser;
    }
}
