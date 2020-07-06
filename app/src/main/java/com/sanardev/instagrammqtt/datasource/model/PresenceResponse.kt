package com.sanardev.instagrammqtt.datasource.model

import com.google.gson.annotations.SerializedName
import com.sanardev.instagrammqtt.datasource.model.event.PresenceEvent

data class PresenceResponse(
    @SerializedName("user_presence")
    var userPresence: HashMap<String, HashMap<String,Any>>,
    @SerializedName("status")
    var status: String
) {
}