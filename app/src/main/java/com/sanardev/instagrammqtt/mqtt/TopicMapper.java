package com.sanardev.instagrammqtt.mqtt;

public interface TopicMapper {

    public String map(String topic);

    public String unmap(String id);
}
