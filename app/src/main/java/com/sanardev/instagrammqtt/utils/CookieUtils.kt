package com.sanardev.instagrammqtt.utils

import android.app.Application
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.Cookie
import okhttp3.Headers
import run.tripa.android.extensions.openSharedPref
import java.util.*
import kotlin.collections.ArrayList

class CookieUtils(var application: Application) {

    fun getCookie(headers: Headers): MutableList<Pair<String, String>> {
        val cookies = headers.values("set-cookie")
        val list = ArrayList<Pair<String, String>>().toMutableList()
        for (cookie in cookies) {
            val items = cookie.split(";")
            for (item in items) {
                val split = item.split("=")
                if (split.size == 2) {
                    val key = split[0].trim()
                    val value = split[1].trim()
                    list.add(Pair(key, value))
                }
            }
        }
        return list
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

    fun getLocalCookie(): Cookie {
        val pref = application.openSharedPref("user_login_data")
        return Cookie(
            username = pref!!.getString("username", null),
            sessionID = pref.getString(
                "sessionid",
                UUID.randomUUID().toString()
            )!!,
            password = pref.getString("password", null),
            adid = pref.getString(
                "adid",
                UUID.randomUUID().toString()
            )!!,
            guid = pref.getString(
                "guid",
                UUID.randomUUID().toString()
            )!!,
            deviceID = pref.getString(
                "device_id",
                UUID.randomUUID().toString()
            )!!,
            csrftoken = pref.getString(
                "csrftoken",
                null
            ),
            phoneID = pref.getString(
                "phone_id",
                UUID.randomUUID().toString()
            )!!,
            mid = pref.getString("mid", null),
            rur = pref.getString("rur", null)
        )
    }

    fun getHeaders(): HashMap<String, String> {
        val cookie = getLocalCookie()
        val map = HashMap<String, String>()
        map["X-DEVICE-ID"] = cookie.deviceID
        map["X-IG-App-Locale"] = "en_US"
        map["X-IG-Device-Locale"] = "en_US"
        map["X-IG-Mapped-Locale"] = "en_US"
        map["X-Pigeon-Session-Id"] = cookie.sessionID
        map["X-Pigeon-Rawclienttime"] = System.currentTimeMillis().toString()
        map["X-IG-Connection-Speed"] = "-1kbps"
        map["X-IG-Bandwidth-Speed-KBPS"] = "1665"
        map["X-IG-Bandwidth-TotalBytes-B"] = "465691"
        map["X-IG-Bandwidth-TotalTime-MS"] = "3322"
        map["X-IG-App-Startup-Country"] = "IR"
        map["X-Bloks-Version-Id"] = InstagramConstants.BLOKS_VERSION_ID
        map["X-IG-WWW-Claim"] = 0.toString()
        map["X-Bloks-Is-Layout-RTL"] = false.toString()
        map["X-Bloks-Enable-RenderCore"] = false.toString()
        map["X-IG-Device-ID"] = cookie.deviceID
        map["X-IG-Android-ID"] = "android-2d397713fddd2a9d"
        map["X-IG-Connection-Type"] = "WIFI"
        map["X-IG-Capabilities"] = InstagramConstants.DEVICE_CAPABILITIES
        map["X-IG-App-ID"] = InstagramConstants.APP_ID
        map["User-Agent"] =
            "Instagram ${InstagramConstants.APP_VERSION} Android (29/10; 408dpi; 1080x2038; Xiaomi/xiaomi; Mi A2; jasmine_sprout; qcom; en_US; 200396019)"
        map["Accept-Language"] = "en-US"
        map["Cookie"] = "mid=${cookie.mid}; csrftoken=${cookie.csrftoken}"
        map["Content-Type"] = "application/x-www-form-urlencoded; charset=UTF-8"
        map["Accept-Encoding"] = "gzip, deflate"
        map["Host"] = "i.instagram.com"
        map["X-FB-HTTP-Engine"] = "Liger"
        map["Connection"] = "keep-alive"
        return map
    }

    fun saveCookie(headers: Headers) {
        val cookies = getCookie(headers)
        application.openSharedPref("user_login_data")!!.edit().apply {
            for (cookie in cookies){
                putString(cookie.first,cookie.second)
            }
        }.apply()
    }

}