package com.sanardev.instagrammqtt.utils

import android.app.Application
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.Cookie
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import okhttp3.Headers
import run.tripa.android.extensions.openSharedPref
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CookieUtils(var application: Application) {

    fun getCookieFromHeadersAndLocalData(
        headers: Headers
    ): Cookie {
        val data = getCookie(headers)
        val localCookie = getLocalCookie()
        val cookie = Cookie(
            sessionID = if(data["sessionid"] == null) localCookie.sessionID else data["sessionid"]!!,
            rur = if(data["rur"] == null) localCookie.rur else data["rur"]!!,
            mid = if(data["mid"] == null) localCookie.mid else data["mid"]!!,
            phoneID = if(data["phone_id"] == null) localCookie.phoneID else data["phone_id"]!!,
            csrftoken = if(data["csrftoken"] == null) localCookie.csrftoken else data["csrftoken"]!!,
            deviceID = if(data["device_id"] == null) localCookie.deviceID else data["device_id"]!!,
            guid = if(data["guid"] == null) localCookie.guid else data["guid"]!!,
            adid = if(data["adid"] == null) localCookie.adid else data["adid"]!!
        )

        return cookie
    }

    fun getCookie(headers: Headers): HashMap<String,String> {
        val cookies = headers.values("set-cookie")
        val map= HashMap<String,String>()
        for (cookie in cookies) {
            val items = cookie.split(";")
            for (item in items) {
                val split = item.split("=")
                if (split.size == 2) {
                    val key = split[0].trim()
                    val value = split[1].trim()
                    map.put(key,value)
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
        map[InstagramConstants.X_DEVICE_ID] = cookie.deviceID
        map[InstagramConstants.X_DEVICE_ID] = "en_US"
        map[InstagramConstants.X_IG_DEVICE_LOCALE] = "en_US"
        map[InstagramConstants.X_IG_MAPPED_LOCALE] = "en_US"
        map[InstagramConstants.X_PIGEON_SESSION_ID] = cookie.sessionID
        map[InstagramConstants.X_PIGEON_RAWCLIENT_TIEM] = System.currentTimeMillis().toString()
        map[InstagramConstants.X_IG_CONNECTION_SPEED] = "-1kbps"
        map[InstagramConstants.X_IG_BANDWIDTH_SPEED_KBPS] = "1665"
        map[InstagramConstants.X_IG_BANDWIDTH_TOTALBYTES_B] = "465691"
        map[InstagramConstants.X_IG_BAND_WIDTH_TOTALTIME_MS] = "3322"
        map[InstagramConstants.X_IG_APP_STARTUP_COUNTRY] = "IR"
        map[InstagramConstants.X_BLOKS_VERSION_ID] = InstagramConstants.BLOKS_VERSION_ID
        map[InstagramConstants.X_IG_WWW_CLAIM] = 0.toString()
        map[InstagramConstants.X_BLOKS_IS_LAYOUT_RTL] = false.toString()
        map[InstagramConstants.X_BLOKS_ENABLE_RENDER_CORE] = false.toString()
        map[InstagramConstants.X_IG_DEVICE_ID] = cookie.deviceID
        map[InstagramConstants.X_IG_ANDROID_ID] = "android-2d397713fddd2a9d"
        map[InstagramConstants.X_IG_CONNECTION_TYPE] = "WIFI"
        map[InstagramConstants.X_IG_CAPABILITIES] = InstagramConstants.DEVICE_CAPABILITIES
        map[InstagramConstants.X_IG_APP_ID] = InstagramConstants.APP_ID
        map[InstagramConstants.X_USER_AGENT] =
            "Instagram ${InstagramConstants.APP_VERSION} Android (29/10; 408dpi; 1080x2038; Xiaomi/xiaomi; Mi A2; jasmine_sprout; qcom; en_US; 200396019)"
        map[InstagramConstants.ACCEPT_LANGUAGE] = "en-US"
        map[InstagramConstants.COOKIE] = "mid=${cookie.mid}; csrftoken=${cookie.csrftoken}"
        map[InstagramConstants.ACCEPT] = "application/json"
        map[InstagramConstants.CONTENT_TYPE] = "application/x-www-form-urlencoded; charset=UTF-8"
        map[InstagramConstants.HOST] = "i.instagram.com"
        map[InstagramConstants.X_FB_HTTP_ENGINE] = "Liger"
        map[InstagramConstants.CONNECTION] = "keep-alive"
        return map
    }

    fun saveCookie(headers: Headers) {
        val cookies = getCookie(headers)
        application.openSharedPref("user_login_data")!!.edit().apply {
            for (cookie in cookies) {
                putString(cookie.key, cookie.value)
            }
        }.apply()
    }

}