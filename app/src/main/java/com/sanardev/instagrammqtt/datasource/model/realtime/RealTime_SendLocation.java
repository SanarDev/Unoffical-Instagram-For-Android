package com.sanardev.instagrammqtt.datasource.model.realtime;

import android.os.Parcel;
import android.os.Parcelable;

import com.sanardev.instagrammqtt.service.realtime.RealTimeIntent;

public class RealTime_SendLocation extends RealTimeCommand implements Parcelable {

    protected RealTime_SendLocation(Parcel in) {
        text = in.readString();
        locationId = in.readString();
        threadId = in.readString();
        clientContext = in.readString();
    }

    public static final Creator<RealTime_SendLocation> CREATOR = new Creator<RealTime_SendLocation>() {
        @Override
        public RealTime_SendLocation createFromParcel(Parcel in) {
            return new RealTime_SendLocation(in);
        }

        @Override
        public RealTime_SendLocation[] newArray(int size) {
            return new RealTime_SendLocation[size];
        }
    };

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_SEND_LOCATION;
    }

    public RealTime_SendLocation(String text, String locationId, String threadId, String clientContext){
        this.text = text;
        this.locationId = locationId;
        this.threadId = threadId;
        this.clientContext = clientContext;
    }

    private String text;
    private String locationId;
    private String threadId;
    private String clientContext;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getClientContext() {
        return clientContext;
    }

    public void setClientContext(String clientContext) {
        this.clientContext = clientContext;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(locationId);
        dest.writeString(threadId);
        dest.writeString(clientContext);
    }
}
