package com.sanardev.instagrammqtt.datasource.model.event;

public class TypingEvent {


    public TypingEvent(String threadId){
        this.threadId = threadId;
    }

    private String threadId;

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }
}
