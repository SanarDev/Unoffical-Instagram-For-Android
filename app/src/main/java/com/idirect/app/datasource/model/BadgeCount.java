package com.idirect.app.datasource.model;

import com.google.gson.annotations.SerializedName;

public class BadgeCount {
    @SerializedName("di")
    private int direct;
    @SerializedName("ds")
    private int ds;
    @SerializedName("ac")
    private int activities;


    public int getDirect() {
        return direct;
    }

    public void setDirect(int direct) {
        this.direct = direct;
    }

    public int getDs() {
        return ds;
    }

    public void setDs(int ds) {
        this.ds = ds;
    }

    public int getActivities() {
        return activities;
    }

    public void setActivities(int activities) {
        this.activities = activities;
    }

}
