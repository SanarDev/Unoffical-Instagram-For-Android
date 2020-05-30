package com.sanardev.instagrammqtt.datasource.model.response

import com.google.gson.annotations.SerializedName

class PhoneVerificationSettings (){

    @SerializedName("max_sms_count")
    var maxSmsCount : Int = 0
    @SerializedName("resend_sms_delay_sec")
    var resendSmsDelaySec : Int = 0
    @SerializedName("robocall_count_down_time_sec")
    var robocallCountDownTimeSec : Int = 0
    @SerializedName("robocall_after_max_sms")
    var robocallAfterMaxSms : Boolean = false
}