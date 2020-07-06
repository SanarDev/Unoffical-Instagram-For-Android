package com.sanardev.instagrammqtt.datasource.model.event;

import com.sanardev.instagrammqtt.datasource.model.Message;

public class MessageEvent {

    public MessageEvent(String threadId,Message message){
        this.threadId = threadId;
        this.message = message;
    }
    private String threadId;
    private Message message;

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
