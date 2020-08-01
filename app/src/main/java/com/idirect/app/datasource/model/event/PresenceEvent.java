package com.idirect.app.datasource.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PresenceEvent {

    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("is_active")
    private boolean isActive;
    @JsonProperty("last_activity_at_ms")
    private String lastActivityAtMs;
    @JsonProperty("in_threads")
    private Object inThreads;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getLastActivityAtMs() {
        return lastActivityAtMs;
    }

    public void setLastActivityAtMs(String lastActivityAtMs) {
        this.lastActivityAtMs = lastActivityAtMs;
    }

    public Object getInThreads() {
        return inThreads;
    }

    public void setInThreads(Object inThreads) {
        this.inThreads = inThreads;
    }
}
