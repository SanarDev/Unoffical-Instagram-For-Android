package com.sanardev.instagrammqtt.datasource.model.realtime;

import android.os.Parcel;
import android.os.Parcelable;

public class RealTimeCommand  {

    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}