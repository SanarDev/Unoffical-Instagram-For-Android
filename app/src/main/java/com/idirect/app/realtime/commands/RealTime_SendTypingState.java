package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.realtime.service.RealTimeIntent;

public class RealTime_SendTypingState extends RealTimeCommand implements Parcelable {

    protected RealTime_SendTypingState(Parcel in) {
        threadId = in.readString();
        isActive = in.readByte() != 0;
        clientContext = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(threadId);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeString(clientContext);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RealTime_SendTypingState> CREATOR = new Creator<RealTime_SendTypingState>() {
        @Override
        public RealTime_SendTypingState createFromParcel(Parcel in) {
            return new RealTime_SendTypingState(in);
        }

        @Override
        public RealTime_SendTypingState[] newArray(int size) {
            return new RealTime_SendTypingState[size];
        }
    };

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_SEND_TYPING_STATE;
    }

    public RealTime_SendTypingState(String threadId, boolean isActive, String clientContext){
        this.threadId = threadId;
        this.isActive = isActive;
        this.clientContext = clientContext;
    }

    private String threadId;
    private boolean isActive;
    private String clientContext;


    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getClientContext() {
        return clientContext;
    }

    public void setClientContext(String clientContext) {
        this.clientContext = clientContext;
    }

}
