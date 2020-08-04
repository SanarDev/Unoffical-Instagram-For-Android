package com.idirect.app.realtime.commands;

import android.os.Parcel;
import android.os.Parcelable;

import com.idirect.app.realtime.service.RealTimeIntent;

public class RealTime_StartService extends RealTimeCommand implements Parcelable {

    private long seqId;
    private long snapShotAt;

    public RealTime_StartService(long seqId , long snapShotAt){
        this.seqId = seqId;
        this.snapShotAt = snapShotAt;

    }

    protected RealTime_StartService(Parcel in) {
        seqId = in.readLong();
        snapShotAt = in.readLong();
    }

    public static final Creator<RealTime_StartService> CREATOR = new Creator<RealTime_StartService>() {
        @Override
        public RealTime_StartService createFromParcel(Parcel in) {
            return new RealTime_StartService(in);
        }

        @Override
        public RealTime_StartService[] newArray(int size) {
            return new RealTime_StartService[size];
        }
    };

    @Override
    public String getAction() {
        return RealTimeIntent.ACTION_CONNECT_SESSION;
    }


    public long getSeqId() {
        return seqId;
    }

    public void setSeqId(long seqId) {
        this.seqId = seqId;
    }

    public long getSnapShotAt() {
        return snapShotAt;
    }

    public void setSnapShotAt(long snapShotAt) {
        this.snapShotAt = snapShotAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(seqId);
        dest.writeLong(snapShotAt);
    }
}
