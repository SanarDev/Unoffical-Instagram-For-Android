package com.sanardev.instagrammqtt.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DirectLikeReactions {

    @SerializedName("sender_id")
    @Expose
    private long senderId;
    @SerializedName("timestamp")
    @Expose
    private long timestamp;
    @SerializedName("client_context")
    @Expose
    private String clientContext;
}
