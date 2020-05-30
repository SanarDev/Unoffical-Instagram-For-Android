package com.sanardev.instagrammqtt.usecase

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginTwoFactorPayload
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.datasource.model.response.InstagramTwoFactorInfo
import com.sanardev.instagrammqtt.helper.Resource
import com.sanardev.instagrammqtt.repository.InstagramRepository
import okhttp3.Headers
import run.tripa.android.extensions.toHexString
import java.lang.StringBuilder
import java.net.URLEncoder
import java.security.Key
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UseCase(
    var application: Application,
    var mInstagramRepository: InstagramRepository,
    var gson: Gson
) {
    val XLATE = "0123456789abcdef"
    /**
     * Digest a string using the given codec and input
     *
     * @param codec
     * Codec to use
     * @param source
     * Source to use
     * @return
     */
    protected fun digest(codec: String, source: String): String {
        try {
            val digest = MessageDigest.getInstance(codec)
            val digestBytes = digest.digest(source.toByteArray())
            return hexlate(digestBytes, digestBytes.size)
        } catch (nsae: NoSuchAlgorithmException) {
            throw RuntimeException(codec + " codec not available")
        }
    }

    /**
     * Get the MD5 (in hexadecimal presentation) for the given source
     *
     * @param source
     * The string to hash
     * @return MD5 hex presentation
     */
    fun md5hex(source: String): String {
        return digest("MD5", source)
    }

    /**
     * Convert the byte array to a hexadecimal presentation (String)
     *
     * @param bytes
     * byte array
     * @param initialCount
     * count (length) of the input
     * @return
     */
    protected fun hexlate(bytes: ByteArray, initialCount: Int): String {
        if (bytes == null) {
            return ""
        }
        val count = Math.min(initialCount, bytes.size)
        val chars = CharArray(count * 2)
        for (i in 0 until count) {
            var value = bytes[i].toInt()
            if (value < 0) {
                value += 256
            }
            chars[(2 * i)] = XLATE.get(value / 16)
            chars[(2 * i + 1)] = XLATE.get(value % 16)
        }
        return String(chars)
    }

    /**
     * Generates Instagram Device ID
     *
     * @param username
     * Username to generate
     * @param password
     * Password to generate
     * @return device id
     */
    private fun generateDeviceId(username: String, password: String): String {
        val seed = md5hex(username + password)
        val volatileSeed = "12345"
        return "android-" + md5hex(seed + volatileSeed).substring(0, 16)
    }

    /**
     * Generate a Hmac SHA-256 hash
     * @param key key
     * @param string value
     * @return hashed
     */
    private fun generateHash(key: String, string: String): String? {
        val `object` = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(`object` as Key)
            val byteArray = mac.doFinal(string.toByteArray(charset("UTF-8")))
            return byteArray.toHexString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Generate signed payload
     * @param payload Payload
     * @return Signed string
     */
    private fun generateSignature(payload: String): String {
        val parsedData = URLEncoder.encode(payload, "UTF-8")
        val signedBody = generateHash(InstagramConstants.API_KEY, payload)
        return StringBuilder()
            .append("ig_sig_key_version=")
            .append(InstagramConstants.API_KEY_VERSION)
            .append("&signed_body=")
            .append(signedBody)
            .append(".")
            .append(parsedData)
            .toString()
    }

    private fun generateUuid(dash: Boolean): String {
        val uuid = UUID.randomUUID().toString()

        return if (dash) {
            uuid
        } else uuid.replace("-".toRegex(), "")

    }

    private fun login() {

    }

    fun getInstagramToken(): LiveData<String> {
        val liveDataToken = MutableLiveData<Headers?>()
        mInstagramRepository.requestCsrfToken(liveDataToken)

        liveDataToken.observeForever {
        }
        return Transformations.switchMap(liveDataToken) {
            return@switchMap MutableLiveData<String>(findCookie(it!!, "csrftoken"))
        }
    }


    fun instagramLogin(
        username: String,
        password: String,
        result: MutableLiveData<Resource<InstagramLoginResult>>
    ) {

        getInstagramToken().observeForever {

            val deviceId = generateDeviceId(username, password)
            val uuid = UUID.randomUUID().toString()
            val adid = UUID.randomUUID().toString()
            val advertisingId = generateUuid(true)
            val phoneID = UUID.randomUUID().toString()

            val instagramLoginPayload =
                InstagramLoginPayload(
                    username,
                    phoneID,
                    it!!,
                    uuid,
                    adid,
                    deviceId,
                    password
                )
            val payload = gson.toJson(instagramLoginPayload)
            val signatureBody = generateSignature(payload)
            mInstagramRepository.login(
                result,
                generateHeaders(signatureBody.toByteArray().size),
                signatureBody
            )
        }
    }

    private fun generateHeaders(contentLenght: Int): Map<String, String> {
        val map = HashMap<String, String>()
        map["X-DEVICE-ID"] = getDeviceID()
        map["X-IG-App-Locale"] = "en_US"
        map["X-IG-Device-Locale"] = "en_US"
        map["X-IG-Mapped-Locale"] = "en_US"
        map["X-Pigeon-Session-Id"] = getSessionID()
        map["X-Pigeon-Rawclienttime"] = "1590787625.099"
        map["X-IG-Connection-Speed"] = "-1kbps"
        map["X-IG-Bandwidth-Speed-KBPS"] = "290.992"
        map["X-IG-Bandwidth-TotalBytes-B"] = "465691"
        map["X-IG-Bandwidth-TotalTime-MS"] = "3322"
        map["X-IG-App-Startup-Country"] = "IR"
        map["X-Bloks-Version-Id"] = InstagramConstants.BLOKS_VERSION_ID
        map["X-IG-WWW-Claim"] = 0.toString()
        map["X-Bloks-Is-Layout-RTL"] = false.toString()
        map["X-Bloks-Enable-RenderCore"] = false.toString()
        map["X-IG-Device-ID"] = getDeviceID()
        map["X-IG-Android-ID"] = "android-2d397713fddd2a9d"
        map["X-IG-Connection-Type"] = "WIFI"
        map["X-IG-Capabilities"] = InstagramConstants.DEVICE_CAPABILITIES
        map["X-IG-App-ID"] = InstagramConstants.APP_ID
        map["User-Agent"] =
            "Instagram 130.0.0.31.121 Android (29/10; 408dpi; 1080x2038; Xiaomi/xiaomi; Mi A2; jasmine_sprout; qcom; en_US; 200396019)"
        map["Accept-Language"] = "en-US"
        map["Cookie"] = "mid=${getMid()}; csrftoken=${getCsrfToken()}"
        map["Content-Type"] = "application/x-www-form-urlencoded; charset=UTF-8"
        map["Accept-Encoding"] = "gzip, deflate"
        map["Host"] = "i.instagram.com"
        map["X-FB-HTTP-Engine"] = "Liger"
        map["Connection"] = "keep-alive"
        map["Content-Length"] = contentLenght.toString()
        return map
    }

    private fun findCookie(headers: Headers, s: String): String? {
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

    private fun getCookie(headers: Headers): MutableList<Pair<String, String>> {
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

    private fun openSharedPref(name: String): SharedPreferences {
        return application.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    fun isLogged(): Boolean {
        val sessionId = openSharedPref("user_login_data").getString("sessionid", null)
        return sessionId != null
    }

    fun getDeviceID(): String {
        val sessionId = openSharedPref("user_login_data").getString("device_id", null)
        return UUID.randomUUID().toString()
    }

    fun getSessionID(): String {
        return openSharedPref("user_login_data").getString(
            "sessionid",
            UUID.randomUUID().toString()
        )!!
    }

    fun getMid(): String? {
        return openSharedPref("user_login_data").getString("mid", null)
    }

    fun getRur(): String? {
        return openSharedPref("user_login_data").getString("rur", null)
    }

    fun getGuid(): String {
        return openSharedPref("user_login_data").getString("guid", UUID.randomUUID().toString())!!
    }

    fun getAdid(): String {
        return openSharedPref("user_login_data").getString("adid", UUID.randomUUID().toString())!!
    }

    fun getCsrfToken(): String? {
        return openSharedPref("user_login_data").getString(
            "csrftoken",
            null
        )
    }

    fun getPhoneID(): String {
        return openSharedPref("user_login_data").getString(
            "phone_id",
            UUID.randomUUID().toString()
        )!!
    }

    fun getPassword(): String {
        return openSharedPref("user_login_data").getString("password", "unKnown")!!
    }

    fun getUsername(): String {
        return openSharedPref("user_login_data").getString("username", "unKnown")!!
    }

    fun checkTwoFactorCode(
        responseLiveData: MutableLiveData<Resource<InstagramLoginResult>>,
        instagramTwoFactorInfo: InstagramTwoFactorInfo,
        code: String
    ) {
        val mInstagramLoginTwoFactorPayload = InstagramLoginTwoFactorPayload().apply {
            username = instagramTwoFactorInfo.username
            phone_id = getPhoneID()
            csrftoken = getCsrfToken()
            guid = getGuid()
            adid = getAdid()
            device_id = getDeviceID()
            verification_code = code
            two_factor_identifier = instagramTwoFactorInfo.twoFactorIdentifier
            password = getPassword()
        }
        val payload = gson.toJson(mInstagramLoginTwoFactorPayload)
        val signatureBody = generateSignature(payload)
        mInstagramRepository.verifyTwoFactor(
            responseLiveData,
            generateHeaders(signatureBody.length),
            signatureBody
        )
    }


}