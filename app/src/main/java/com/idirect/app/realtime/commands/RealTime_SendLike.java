package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.realtime.service.RealTimeIntent;

public class RealTime_SendLike extends RealTimeCommand implements Parcelable {

    public RealTime_SendLike(String threadId,String clientContext){
        this.threadId = threadId;
        this.clientContext = clientContext;
    }
    private String threadId;
    private String clientContext;

    protected RealTime_SendLike(Parcel in) {
        threadId = in.readString();
        clientContext = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(threadId);
        dest.writeString(clientContext);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_SEND_LIKE;
    }

    public static final Creator<RealTime_SendLike> CREATOR = new Creator<RealTime_SendLike>() {
        @Override
        public RealTime_SendLike createFromParcel(Parcel in) {
            return new RealTime_SendLike(in);
        }

        @Override
        public RealTime_SendLike[] newArray(int size) {
            return new RealTime_SendLike[size];
        }
    };

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
