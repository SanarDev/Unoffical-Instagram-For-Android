package com.sanardev.instagrammqtt.datasource.model.payload

import com.google.gson.annotations.SerializedName

data class InstagramLoginPayload(
     @SerializedName("username")
     var username :String,
     @SerializedName("phone_id")
     var phone_id :String,
     @SerializedName("_csrftoken")
     var _csrftoken :String,
     @SerializedName("guid")
     var guid :String,
     @SerializedName("adid")
     var adid :String,
     @SerializedName("device_id")
     var device_id :String,
     @SerializedName("password")
     var password :String,
     @SerializedName("country_codes")
     var country_codes :String = "[{\"country_code\":\"1\",\"source\":[\"default\"]},{\"country_code\":\"7\",\"source\":[\"uig_via_phone_id\"]}]",
     @SerializedName("login_attempt_account")
     var login_attempt_account :Int = 0
)