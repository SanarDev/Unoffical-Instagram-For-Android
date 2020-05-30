package com.sanardev.instagrammqtt.datasource.model.payload

import com.google.gson.annotations.SerializedName

class InstagramResendTwoFactorCodePayload {

    @SerializedName("_csrftoken")
    var csrftoken: String? = null
    @SerializedName("two_factor_identifier")
    var twoFactorIdentifier: String? = null
    @SerializedName("username")
    var username: String? = null
    @SerializedName("guid")
    var guid: String? = null
    @SerializedName("device_id")
    var deviceID: String? = null
}