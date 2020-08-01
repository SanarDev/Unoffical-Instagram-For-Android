package com.idirect.app.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class In {

    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("position")
    @Expose
    private List<Double> position = null;
    @SerializedName("start_time_in_video_in_sec")
    @Expose
    private Object startTimeInVideoInSec;
    @SerializedName("duration_in_video_in_sec")
    @Expose
    private Object durationInVideoInSec;

    public List<Double> getPosition() {
        return position;
    }

    public void setPosition(List<Double> position) {
        this.position = position;
    }

    public Object getStartTimeInVideoInSec() {
        return startTimeInVideoInSec;
    }

    public void setStartTimeInVideoInSec(Object startTimeInVideoInSec) {
        this.startTimeInVideoInSec = startTimeInVideoInSec;
    }

    public Object getDurationInVideoInSec() {
        return durationInVideoInSec;
    }

    public void setDurationInVideoInSec(Object durationInVideoInSec) {
        this.durationInVideoInSec = durationInVideoInSec;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
