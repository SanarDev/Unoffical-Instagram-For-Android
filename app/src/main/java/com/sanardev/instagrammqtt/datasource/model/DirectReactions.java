package com.sanardev.instagrammqtt.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectReactions {

    @SerializedName("likes")
    @Expose
    private List<DirectLikeReactions> likes;
    @SerializedName("likes_count")
    @Expose
    private int likesCount;

}
