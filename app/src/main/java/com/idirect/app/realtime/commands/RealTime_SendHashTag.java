package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.service.realtime.RealTimeIntent;

public class RealTime_SendHashTag  extends RealTimeCommand implements Parcelable {

    protected RealTime_SendHashTag(Parcel in) {
        text = in.readString();
        hashTag = in.readString();
        threadId = in.readString();
        clientContext = in.readString();
    }

    public static final Creator<RealTime_SendHashTag> CREATOR = new Creator<RealTime_SendHashTag>() {
        @Override
        public RealTime_SendHashTag createFromParcel(Parcel in) {
            return new RealTime_SendHashTag(in);
        }

        @Override
        public RealTime_SendHashTag[] newArray(int size) {
            return new RealTime_SendHashTag[size];
        }
    };

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_SEND_HASH_TAG;
    }

    private String text;
    private String hashTag;
    private String threadId;
    private String clientContext;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHashTag() {
        return hashTag;
    }

    public void setHashTag(String hashTag) {
        this.hashTag = hashTag;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getClientContext() {
        return clientContext;
    }

    public void setClientContext(String clientContext) {
        this.clientContext = clientContext;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(hashTag);
        dest.writeString(threadId);
        dest.writeString(clientContext);
    }
}
