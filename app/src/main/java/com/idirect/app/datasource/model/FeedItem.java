package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FeedItem {


    @SerializedName("media_or_ad")
    @Expose
    private Media media;

    public Media getMediaOrAd() {
        return media;
    }

    public void setMediaOrAd(Media media) {
        this.media = media;
    }
}
