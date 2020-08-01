package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.service.realtime.RealTimeIntent;

public class RealTime_StopService extends RealTimeCommand implements Parcelable {

    public RealTime_StopService(){

    }
    protected RealTime_StopService(Parcel in) {
    }

    public static final Creator<RealTime_StopService> CREATOR = new Creator<RealTime_StopService>() {
        @Override
        public RealTime_StopService createFromParcel(Parcel in) {
            return new RealTime_StopService(in);
        }

        @Override
        public RealTime_StopService[] newArray(int size) {
            return new RealTime_StopService[size];
        }
    };

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_DISCONNECT_SESSION;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
