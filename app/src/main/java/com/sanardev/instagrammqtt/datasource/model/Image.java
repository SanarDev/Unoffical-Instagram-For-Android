package com.sanardev.instagrammqtt.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Image {

    @SerializedName("fixed_height")
    @Expose
    private FixedHeight fixedHeight;

    public FixedHeight getFixedHeight() {
        return fixedHeight;
    }

    public void setFixedHeight(FixedHeight fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

}