package com.sanardev.instagrammqtt.datasource.model;

public class DirectDate {

    private long timeStamps;
    private String timeString;

    public DirectDate(long timeStamps,String timeString){
        this.timeStamps = timeStamps;
        this.timeString = timeString;
    }
    public long getTimeStamps() {
        return timeStamps;
    }

    public void setTimeStamps(long timeStamps) {
        this.timeStamps = timeStamps;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }
}
