package com.sanardev.instagrammqtt.datasource.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class Presence(
    @SerializedName("is_active")
    private var isActive: Boolean = false,
    @SerializedName("last_activity_at_ms")
private var lastActivityAtMs: Long)