package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.service.realtime.RealTimeIntent;

public class RealTime_SendMedia extends RealTimeCommand implements Parcelable {

    protected RealTime_SendMedia(Parcel in) {
        text = in.readString();
        mediaId = in.readString();
        threadId = in.readString();
        clientContext = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(mediaId);
        dest.writeString(threadId);
        dest.writeString(clientContext);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RealTime_SendMedia> CREATOR = new Creator<RealTime_SendMedia>() {
        @Override
        public RealTime_SendMedia createFromParcel(Parcel in) {
            return new RealTime_SendMedia(in);
        }

        @Override
        public RealTime_SendMedia[] newArray(int size) {
            return new RealTime_SendMedia[size];
        }
    };

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_SEND_MEDIA;
    }

    public RealTime_SendMedia(String text,String mediaId,String threadId,String clientContext){
        this.text = text;
        this.mediaId = mediaId;
        this.threadId = threadId;
        this.clientContext = clientContext;
    }
    private String text;
    private String mediaId;
    private String threadId;
    private String clientContext;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
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
