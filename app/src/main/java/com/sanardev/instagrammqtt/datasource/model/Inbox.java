package com.sanardev.instagrammqtt.datasource.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Inbox {

    @SerializedName("threads")
    @Expose
    private List<Thread> threads = null;
    @SerializedName("has_older")
    @Expose
    private Boolean hasOlder;
    @SerializedName("unseen_count")
    @Expose
    private long unseenCount;
    @SerializedName("unseen_count_ts")
    @Expose
    private long unseenCountTs;
    @SerializedName("oldest_cursor")
    @Expose
    private String oldestCursor;
    @SerializedName("prev_cursor")
    @Expose
    private PrevCursor prevCursor;
    @SerializedName("next_cursor")
    @Expose
    private NextCursor nextCursor;
    @SerializedName("blended_inbox_enabled")
    @Expose
    private Boolean blendedInboxEnabled;

    public String getOldestCursor() {
        return oldestCursor;
    }

    public void setOldestCursor(String oldestCursor) {
        this.oldestCursor = oldestCursor;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public void setThreads(List<Thread> threads) {
        this.threads = threads;
    }

    public Boolean getHasOlder() {
        return hasOlder;
    }

    public void setHasOlder(Boolean hasOlder) {
        this.hasOlder = hasOlder;
    }

    public long getUnseenCount() {
        return unseenCount;
    }

    public void setUnseenCount(long unseenCount) {
        this.unseenCount = unseenCount;
    }

    public long getUnseenCountTs() {
        return unseenCountTs;
    }

    public void setUnseenCountTs(long unseenCountTs) {
        this.unseenCountTs = unseenCountTs;
    }

    public PrevCursor getPrevCursor() {
        return prevCursor;
    }

    public void setPrevCursor(PrevCursor prevCursor) {
        this.prevCursor = prevCursor;
    }

    public NextCursor getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(NextCursor nextCursor) {
        this.nextCursor = nextCursor;
    }

    public Boolean getBlendedInboxEnabled() {
        return blendedInboxEnabled;
    }

    public void setBlendedInboxEnabled(Boolean blendedInboxEnabled) {
        this.blendedInboxEnabled = blendedInboxEnabled;
    }

}