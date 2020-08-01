package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.service.realtime.RealTimeIntent;

public class RealTime_SendProfile extends RealTimeCommand implements Parcelable {

    protected RealTime_SendProfile(Parcel in) {
        text = in.readString();
        userId = in.readString();
        threadId = in.readString();
        clientContext = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(userId);
        dest.writeString(threadId);
        dest.writeString(clientContext);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RealTime_SendProfile> CREATOR = new Creator<RealTime_SendProfile>() {
        @Override
        public RealTime_SendProfile createFromParcel(Parcel in) {
            return new RealTime_SendProfile(in);
        }

        @Override
        public RealTime_SendProfile[] newArray(int size) {
            return new RealTime_SendProfile[size];
        }
    };

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_SEND_PROFILE;
    }

    private String text;
    private String userId;
    private String threadId;
    private String clientContext;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
