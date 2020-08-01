package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PublishMetadata {


    @SerializedName("publish_time_ms")
    @Expose
    private String publishTimeMs;
    @SerializedName("topic_publish_id")
    @Expose
    private Long topicPublishId;

    public String getPublishTimeMs() {
        return publishTimeMs;
    }

    public void setPublishTimeMs(String publishTimeMs) {
        this.publishTimeMs = publishTimeMs;
    }

    public Long getTopicPublishId() {
        return topicPublishId;
    }

    public void setTopicPublishId(Long topicPublishId) {
        this.topicPublishId = topicPublishId;
    }
}
