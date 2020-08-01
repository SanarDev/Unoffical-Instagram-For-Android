package com.idirect.app.datasource.remote.response

import com.google.gson.annotations.SerializedName
import com.idirect.app.datasource.model.payload.StatusResult
import com.idirect.app.datasource.model.response.InstagramChallenge
import com.idirect.app.datasource.model.response.InstagramLoggedUser
import com.idirect.app.datasource.model.response.InstagramTwoFactorInfo

class LoginResponse : StatusResult(){

    @SerializedName("two_factor_required")
    var twoFactorRequired : Boolean = false
    @SerializedName("logged_in_user")
    var loggedInUser: InstagramLoggedUser? = null
    @SerializedName("two_factor_info")
    var two_factor_info: InstagramTwoFactorInfo? = null
    @SerializedName("challenge")
    var challenge: InstagramChallenge? = null
}