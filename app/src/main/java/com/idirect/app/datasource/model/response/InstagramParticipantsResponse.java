package com.idirect.app.datasource.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.idirect.app.datasource.model.Thread;
import com.idirect.app.datasource.model.payload.StatusResult;

public class InstagramParticipantsResponse extends StatusResult {

    @SerializedName("thread")
    @Expose
    private Thread thread;

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
