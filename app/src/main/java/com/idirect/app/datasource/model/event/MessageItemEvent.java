package com.idirect.app.datasource.model.event;

import com.idirect.app.datasource.model.Message;

public class MessageItemEvent {

    public MessageItemEvent(String threadId, Message message) {
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
