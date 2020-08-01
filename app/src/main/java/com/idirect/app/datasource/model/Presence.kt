package com.idirect.app.datasource.model

import com.google.gson.annotations.SerializedName

data class Presence(
    @SerializedName("is_active")
    private var isActive: Boolean = false,
    @SerializedName("last_activity_at_ms")
private var lastActivityAtMs: Long)