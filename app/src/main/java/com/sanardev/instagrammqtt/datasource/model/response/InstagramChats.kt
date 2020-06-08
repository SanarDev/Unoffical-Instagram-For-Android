package com.sanardev.instagrammqtt.datasource.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.sanardev.instagrammqtt.datasource.model.Thread
import com.sanardev.instagrammqtt.datasource.model.payload.StatusResult


class InstagramChats :StatusResult() {

    @SerializedName("thread")
    @Expose
    var thread: Thread? = null
}