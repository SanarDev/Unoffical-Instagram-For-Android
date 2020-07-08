package com.sanardev.instagrammqtt.datasource.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Seen {

    @SerializedName("timestamp")
    @Expose
    private long timeStamp;
    @SerializedName("item_id")
    @Expose
    private String itemId;
    @SerializedName("")

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
