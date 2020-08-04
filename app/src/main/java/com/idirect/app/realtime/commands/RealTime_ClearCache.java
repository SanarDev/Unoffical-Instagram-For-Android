package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.realtime.service.RealTimeIntent;

public class RealTime_ClearCache extends RealTimeCommand implements Parcelable {

    public RealTime_ClearCache(){

    }
    protected RealTime_ClearCache(Parcel in) {
    }

    public static final Creator<RealTime_ClearCache> CREATOR = new Creator<RealTime_ClearCache>() {
        @Override
        public RealTime_ClearCache createFromParcel(Parcel in) {
            return new RealTime_ClearCache(in);
        }

        @Override
        public RealTime_ClearCache[] newArray(int size) {
            return new RealTime_ClearCache[size];
        }
    };

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_CLEAR_CACHE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
