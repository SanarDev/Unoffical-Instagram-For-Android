package com.sanardev.instagrammqtt.datasource.model.payload

import com.google.gson.annotations.SerializedName

class InstagramLoginTwoFactorPayload {
    @SerializedName("username")
    var username: String? = null
    @SerializedName("phone_id")
    var phone_id: String? = null
    @SerializedName("_csrftoken")
    var csrftoken: String? = null
    @SerializedName("guid")
    var guid: String? = null
    @SerializedName("adid")
    var adid: String? = null
    @SerializedName("device_id")
    var device_id: String? = null
    @SerializedName("verification_code")
    var verification_code: String? = null
    @SerializedName("two_factor_identifier")
    var two_factor_identifier: String? = null
    @SerializedName("password")
    var password: String? = null
    @SerializedName("login_attempt_account")
    var login_attempt_account = 0
}