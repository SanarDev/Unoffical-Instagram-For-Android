package com.sanardev.instagrammqtt.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.sanardev.instagrammqtt.service.realtime.RealTimeIntent;

public class RealTime_SendReaction extends RealTimeCommand implements Parcelable {

    protected RealTime_SendReaction(Parcel in) {
        itemId = in.readString();
        reactionType = in.readString();
        clientContext = in.readString();
        threadId = in.readString();
        reactionStatus = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemId);
        dest.writeString(reactionType);
        dest.writeString(clientContext);
        dest.writeString(threadId);
        dest.writeString(reactionStatus);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RealTime_SendReaction> CREATOR = new Creator<RealTime_SendReaction>() {
        @Override
        public RealTime_SendReaction createFromParcel(Parcel in) {
            return new RealTime_SendReaction(in);
        }

        @Override
        public RealTime_SendReaction[] newArray(int size) {
            return new RealTime_SendReaction[size];
        }
    };


    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_SEND_REACTION;
    }

    public RealTime_SendReaction(String itemId,String reactionType,String clientContext,String threadId,String reactionStatus){
        this.itemId = itemId;
        this.reactionStatus = reactionStatus;
        this.reactionType = reactionType;
        this.clientContext = clientContext;
        this.threadId = threadId;
    }

    private String itemId;
    private String reactionType;
    private String clientContext;
    private String threadId;
    private String reactionStatus;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }

    public String getClientContext() {
        return clientContext;
    }

    public void setClientContext(String clientContext) {
        this.clientContext = clientContext;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getReactionStatus() {
        return reactionStatus;
    }

    public void setReactionStatus(String reactionStatus) {
        this.reactionStatus = reactionStatus;
    }
}
