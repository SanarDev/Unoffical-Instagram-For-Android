package com.sanardev.instagrammqtt.utils

import android.app.Application
import android.os.Build
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.Cookie
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import okhttp3.Headers
import run.tripa.android.extensions.openSharedPref
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CookieUtils(var application: Application) {

    companion object{
        fun generateCookie(): Cookie {
            return Cookie(
                sessionID = UUID.randomUUID().toString(),
                adid = UUID.randomUUID().toString(),
                guid = UUID.randomUUID().toString(),
                deviceID =
                UUID.randomUUID().toString(),
                phoneID = UUID.randomUUID().toString()
            )
        }
    }
    fun getCookieFromHeadersAndLocalData(
        headers: Headers,
        localCookie:Cookie
    ): Cookie {
        val data = getCookie(headers)
        val cookie = Cookie(
            sessionID = if (data["sessionid"] == null) localCookie.sessionID else data["sessionid"]!!,
            rur = if (data["rur"] == null) localCookie.rur else data["rur"]!!,
            mid = if (data["mid"] == null) localCookie.mid else data["mid"]!!,
            phoneID = if (data["phone_id"] == null) localCookie.phoneID else data["phone_id"]!!,
            csrftoken = if (data["csrftoken"] == null) localCookie.csrftoken else data["csrftoken"]!!,
            deviceID = if (data["device_id"] == null) localCookie.deviceID else data["device_id"]!!,
            guid = if (data["guid"] == null) localCookie.guid else data["guid"]!!,
            adid = if (data["adid"] == null) localCookie.adid else data["adid"]!!
        )

        return cookie
    }

    fun getCookie(headers: Headers): HashMap<String, String> {
        val cookies = headers.values("set-cookie")
        val map = HashMap<String, String>()
        for (cookie in cookies) {
            val items = cookie.split(";")
            for (item in items) {
                val split = item.split("=")
                if (split.size == 2) {
                    val key = split[0].trim()
                    val value = split[1].trim()
                    map.put(key, value)
                }
            }
        }
        return map
    }

    fun findCookie(headers: Headers, s: String): String? {
        val cookies = headers.values("set-cookie")
        val list = ArrayList<Pair<String, String>>().toMutableList()
        for (cookie in cookies) {
            val items = cookie.split(";")
            for (item in items) {
                val split = item.split("=")
                if (split.size == 2) {
                    val key = split[0].trim()
                    val value = split[1].trim()
                    if (key == s)
                        return value
                }
            }
        }
        return null
    }


    fun saveCookie(headers: Headers) {
        val cookies = getCookie(headers)
        application.openSharedPref("user_login_data")!!.edit().apply {
            for (cookie in cookies) {
                putString(cookie.key, cookie.value)
            }
        }.apply()
    }

    fun removeCookie() {
        application.openSharedPref("user_login_data")!!.edit().clear().apply()
    }

}