package com.idirect.app.datasource.model.event;

import com.idirect.app.datasource.model.Seen;

public class UpdateSeenEvent {

    public UpdateSeenEvent(String threadId,Seen seen){
        this.threadId = threadId;
        this.seen = seen;
    }

    private String threadId;
    private Seen seen;

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public Seen getSeen() {
        return seen;
    }

    public void setSeen(Seen seen) {
        this.seen = seen;
    }
}
