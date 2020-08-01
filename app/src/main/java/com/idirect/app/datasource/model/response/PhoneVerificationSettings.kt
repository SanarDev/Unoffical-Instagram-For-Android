package com.idirect.app.datasource.model.response

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class PhoneVerificationSettings() :Parcelable{

    @SerializedName("max_sms_count")
    var maxSmsCount : Int = 0
    @SerializedName("resend_sms_delay_sec")
    var resendSmsDelaySec : Int = 0
    @SerializedName("robocall_count_down_time_sec")
    var robocallCountDownTimeSec : Int = 0
    @SerializedName("robocall_after_max_sms")
    var robocallAfterMaxSms : Boolean = false

    constructor(parcel: Parcel) : this() {
        maxSmsCount = parcel.readInt()
        resendSmsDelaySec = parcel.readInt()
        robocallCountDownTimeSec = parcel.readInt()
        robocallAfterMaxSms = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(maxSmsCount)
        parcel.writeInt(resendSmsDelaySec)
        parcel.writeInt(robocallCountDownTimeSec)
        parcel.writeByte(if (robocallAfterMaxSms) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhoneVerificationSettings> {
        override fun createFromParcel(parcel: Parcel): PhoneVerificationSettings {
            return PhoneVerificationSettings(parcel)
        }

        override fun newArray(size: Int): Array<PhoneVerificationSettings?> {
            return arrayOfNulls(size)
        }
    }
}