package com.idirect.app.datasource.model;

public class ParsedMessage {

    public ParsedMessage(String topicName,String payload){
        this.topicName = topicName;
        this.payload = payload;
    }
    private String topicName;
    private String payload;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
