package com.idirect.app.datasource.model

import com.google.gson.annotations.SerializedName

data class PresenceResponse(
    @SerializedName("user_presence")
    var userPresence: HashMap<String, HashMap<String,Any>>,
    @SerializedName("status")
    var status: String
) {
}