package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.service.realtime.RealTimeIntent;

public class RealTime_SendUserStory extends RealTimeCommand implements Parcelable {


    protected RealTime_SendUserStory(Parcel in) {
        storyId = in.readString();
        threadId = in.readString();
        text = in.readString();
        clientContext = in.readString();
    }

    public static final Creator<RealTime_SendUserStory> CREATOR = new Creator<RealTime_SendUserStory>() {
        @Override
        public RealTime_SendUserStory createFromParcel(Parcel in) {
            return new RealTime_SendUserStory(in);
        }

        @Override
        public RealTime_SendUserStory[] newArray(int size) {
            return new RealTime_SendUserStory[size];
        }
    };


    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_SEND_USER_STORY;
    }

    public RealTime_SendUserStory(String storyId, String threadId, String text, String clientContext){
        this.storyId = storyId;
        this.threadId = threadId;
        this.text = text;
        this.clientContext = clientContext;
    }

    private String storyId;
    private String threadId;
    private String text;
    private String clientContext;

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        dest.writeString(storyId);
        dest.writeString(threadId);
        dest.writeString(text);
        dest.writeString(clientContext);
    }
}
