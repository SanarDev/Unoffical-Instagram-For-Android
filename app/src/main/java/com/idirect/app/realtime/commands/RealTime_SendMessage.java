package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.service.realtime.RealTimeIntent;

public class RealTime_SendMessage extends RealTimeCommand implements Parcelable {

    public RealTime_SendMessage(String threadId, String clientContext, String text) {
        this.text = text;
        this.threadId = threadId;
        this.clientContext = clientContext;
    }

    private String threadId;
    private String clientContext;
    private String text;

    protected RealTime_SendMessage(Parcel in) {
        threadId = in.readString();
        clientContext = in.readString();
        text = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(threadId);
        dest.writeString(clientContext);
        dest.writeString(text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RealTime_SendMessage> CREATOR = new Creator<RealTime_SendMessage>() {
        @Override
        public RealTime_SendMessage createFromParcel(Parcel in) {
            return new RealTime_SendMessage(in);
        }

        @Override
        public RealTime_SendMessage[] newArray(int size) {
            return new RealTime_SendMessage[size];
        }
    };

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_SEND_TEXT_MESSAGE;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}