package com.sanardev.instagrammqtt.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.sanardev.instagrammqtt.service.realtime.RealTimeIntent;

public class RealTime_ClearItemCache extends RealTimeCommand implements Parcelable {

    public RealTime_ClearItemCache(String threadId, String itemId) {
        this.itemId = itemId;
        this.threadId = threadId;
    }

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_CLEAR_ITEM_CACHE;
    }

    private String itemId;
    private String threadId;

    protected RealTime_ClearItemCache(Parcel in) {
        itemId = in.readString();
        threadId = in.readString();
    }

    public static final Creator<RealTime_ClearItemCache> CREATOR = new Creator<RealTime_ClearItemCache>() {
        @Override
        public RealTime_ClearItemCache createFromParcel(Parcel in) {
            return new RealTime_ClearItemCache(in);
        }

        @Override
        public RealTime_ClearItemCache[] newArray(int size) {
            return new RealTime_ClearItemCache[size];
        }
    };

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemId);
        dest.writeString(threadId);
    }
}
