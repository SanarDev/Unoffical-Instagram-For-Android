package com.sanardev.instagrammqtt.datasource.model.realtime

import com.google.gson.annotations.SerializedName

data class RealtimeSubDirectDataWrapper(
    @SerializedName("op")
    var op: String,
    @SerializedName("path")
    var path: String,
    @SerializedName("value")
    var value: String,
    @SerializedName("doublePublish")
    var doublePublish: Boolean = false)