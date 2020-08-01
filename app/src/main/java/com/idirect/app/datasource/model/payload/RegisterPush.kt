package com.idirect.app.datasource.model.payload

import com.google.gson.annotations.SerializedName

data class RegisterPush (
    @SerializedName("device_type")
    var deviceType: String = "android_mqtt",
    @SerializedName("is_main_push_channel")
    var isMainPushChannel:Boolean = true,
    @SerializedName("phone_id")
    var phoneID:String,
    @SerializedName("device_sub_type")
    var deviceSubType:Int = 2,
    @SerializedName("device_token")
    var deviceToken:String,
    @SerializedName("_csrftoken")
    var csrfToken:String,
    @SerializedName("guid")
    var guid:String,
    @SerializedName("uuid")
    var uuid:String,
    @SerializedName("users")
    var pk:String,
    @SerializedName("family_device_id")
    var familyDeviceID:String

    ){
}