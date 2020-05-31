package com.sanardev.instagrammqtt.datasource.model.response

import com.google.gson.annotations.SerializedName
import com.sanardev.instagrammqtt.datasource.model.Cookie
import com.sanardev.instagrammqtt.datasource.model.payload.StatusResult
import okhttp3.Headers
import kotlin.reflect.KClass

class InstagramLoginResult : StatusResult(){
    @SerializedName("two_factor_required")
    var twoFactorRequired : Boolean = false
    @SerializedName("logged_in_user")
    var loggedInUser: InstagramLoggedUser? = null
    @SerializedName("two_factor_info")
    var two_factor_info: InstagramTwoFactorInfo? = null
    @SerializedName("challenge")
    var challenge: InstagramChallenge? = null
    var headers:Headers?=null
}