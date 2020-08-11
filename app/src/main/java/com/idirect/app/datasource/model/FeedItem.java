package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FeedItem {


    @SerializedName("media_or_ad")
    @Expose
    private UserPost post;

    public UserPost getMediaOrAd() {
        return post;
    }

    public void setMediaOrAd(UserPost media) {
        this.post = media;
    }
}
