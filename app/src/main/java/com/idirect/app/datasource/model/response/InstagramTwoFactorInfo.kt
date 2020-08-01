package com.idirect.app.datasource.model.response

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class InstagramTwoFactorInfo() : Parcelable{
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

    constructor(parcel: Parcel) : this() {
        username = parcel.readString()
        SmsTwoFactorOn = parcel.readByte() != 0.toByte()
        TotpTwoFactorOn = parcel.readByte() != 0.toByte()
        obfuscatedPhoneNumber = parcel.readString()
        twoFactorIdentifier = parcel.readString()
        showMessengerCodeOption = parcel.readByte() != 0.toByte()
        showNewLoginScreen = parcel.readByte() != 0.toByte()
        showTrustedDevideOption = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeByte(if (SmsTwoFactorOn) 1 else 0)
        parcel.writeByte(if (TotpTwoFactorOn) 1 else 0)
        parcel.writeString(obfuscatedPhoneNumber)
        parcel.writeString(twoFactorIdentifier)
        parcel.writeByte(if (showMessengerCodeOption) 1 else 0)
        parcel.writeByte(if (showNewLoginScreen) 1 else 0)
        parcel.writeByte(if (showTrustedDevideOption) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InstagramTwoFactorInfo> {
        override fun createFromParcel(parcel: Parcel): InstagramTwoFactorInfo {
            return InstagramTwoFactorInfo(parcel)
        }

        override fun newArray(size: Int): Array<InstagramTwoFactorInfo?> {
            return arrayOfNulls(size)
        }
    }
}