package com.sanardev.instagrammqtt.datasource.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class InstagramTwoFactorInfo() : Serializable{
    @SerializedName("username")
    var username: String? = null
    @SerializedName("sms_two_factor_on")
    var SmsTwoFactorOn: Boolean = false
    @SerializedName("totp_two_factor_on")
    var TotpTwoFactorOn: Boolean = false
    @SerializedName("obfuscated_phone_number")
    var obfuscatedPhoneNumber: String? = null
    @SerializedName("two_factor_identifier")
    var twoFactorIdentifier: String? = null
    @SerializedName("show_messenger_code_option")
    var showMessengerCodeOption: Boolean = false
    @SerializedName("show_new_login_screen")
    var showNewLoginScreen: Boolean = false
    @SerializedName("show_trusted_device_option")
    var showTrustedDevideOption: Boolean = false
    @SerializedName("phone_verification_settings")
    var phoneVerificationSettings: PhoneVerificationSettings? = null
}