package com.idirect.app.datasource.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.idirect.app.datasource.model.Thread
import com.idirect.app.datasource.model.payload.StatusResult


class InstagramChats :StatusResult() {

    @SerializedName("thread")
    @Expose
    var thread: Thread? = null
}