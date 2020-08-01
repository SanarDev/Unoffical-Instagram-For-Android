package com.idirect.app.datasource.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.idirect.app.datasource.model.User
import com.idirect.app.datasource.model.payload.StatusResult

class InstagramUserInfo : StatusResult() {

    @SerializedName("user")
    @Expose
    lateinit var user: User

}