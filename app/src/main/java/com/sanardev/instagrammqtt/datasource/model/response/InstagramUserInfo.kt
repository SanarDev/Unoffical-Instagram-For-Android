package com.sanardev.instagrammqtt.datasource.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.sanardev.instagrammqtt.datasource.model.User
import com.sanardev.instagrammqtt.datasource.model.payload.StatusResult

class InstagramUserInfo : StatusResult() {

    @SerializedName("user")
    @Expose
    lateinit var user: User

}