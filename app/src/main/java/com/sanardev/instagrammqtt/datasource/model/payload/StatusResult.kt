package com.sanardev.instagrammqtt.datasource.model.payload

import androidx.annotation.NonNull
import com.google.gson.annotations.SerializedName

open class StatusResult {
    @NonNull
    var status: String? = null
    var message: String? = null

    var spam: Boolean = false
    var lock: Boolean = false

    @SerializedName("error_title")
    var errorTitle: String? = null

    @SerializedName("error_body")
    var errorMessage: String? = null

    @SerializedName("error_type")
    var errorType: String? = null
    var checkpoint_url: String? = null

    companion object {

        fun setValues(to: StatusResult, from: StatusResult) {
            to.status = from.status
            to.message = from.status
            to.spam = from.spam
            to.lock = from.lock
            to.errorTitle = from.errorTitle
            to.errorMessage = from.errorMessage
            to.errorType = from.errorType
            to.checkpoint_url = from.checkpoint_url
        }
    }
}