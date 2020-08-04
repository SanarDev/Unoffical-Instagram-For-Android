package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.realtime.service.RealTimeIntent;

public class RealTime_MarkAsSeen extends RealTimeCommand implements Parcelable {

    public RealTime_MarkAsSeen(String threadId, String itemId){
        this.threadId = threadId;
        this.itemId = itemId;
    }

    private String threadId;
    private String itemId;

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_MARK_AS_SEEN;
    }

    protected RealTime_MarkAsSeen(Parcel in) {
        threadId = in.readString();
        itemId = in.readString();
    }

    public static final Creator<RealTime_MarkAsSeen> CREATOR = new Creator<RealTime_MarkAsSeen>() {
        @Override
        public RealTime_MarkAsSeen createFromParcel(Parcel in) {
            return new RealTime_MarkAsSeen(in);
        }

        @Override
        public RealTime_MarkAsSeen[] newArray(int size) {
            return new RealTime_MarkAsSeen[size];
        }
    };

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(threadId);
        dest.writeString(itemId);
    }
}
