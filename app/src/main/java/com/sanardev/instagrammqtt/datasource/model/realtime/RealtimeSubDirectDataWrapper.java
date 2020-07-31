package com.sanardev.instagrammqtt.datasource.model.realtime;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RealtimeSubDirectDataWrapper {

    @SerializedName("op")
    @Expose
    private String op;
    @SerializedName("path")
    @Expose
    private String path;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("doublePublish")
    @Expose
    private boolean doublePublish;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isDoublePublish() {
        return doublePublish;
    }

    public void setDoublePublish(boolean doublePublish) {
        this.doublePublish = doublePublish;
    }
}
