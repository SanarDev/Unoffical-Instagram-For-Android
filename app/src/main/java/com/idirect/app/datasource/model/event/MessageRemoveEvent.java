package com.idirect.app.datasource.model.event;

public class MessageRemoveEvent {

    private final String threadId;
    private String itemId;

    public MessageRemoveEvent(String threadId,String itemId) {
        this.itemId = itemId;
        this.threadId = threadId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getThreadId() {
        return threadId;
    }
}
