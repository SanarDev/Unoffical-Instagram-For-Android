package com.sanardev.instagrammqtt.usecase

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginTwoFactorPayload
import com.sanardev.instagrammqtt.datasource.model.response.InstagramInbox
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.datasource.model.response.InstagramTwoFactorInfo
import com.sanardev.instagrammqtt.utils.Resource
import com.sanardev.instagrammqtt.repository.InstagramRepository
import com.sanardev.instagrammqtt.utils.CookieUtils
import com.sanardev.instagrammqtt.utils.InstagramHashUtils
import com.sanardev.instagrammqtt.utils.StorageUtils
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class UseCase(
    var application: Application,
    var mInstagramRepository: InstagramRepository,
    var cookieUtils: CookieUtils,
    var gson: Gson
) {
    val XLATE = "0123456789abcdef"


    protected fun digest(codec: String, source: String): String {
        try {
            val digest = MessageDigest.getInstance(codec)
            val digestBytes = digest.digest(source.toByteArray())
            return hexlate(digestBytes, digestBytes.size)
        } catch (nsae: NoSuchAlgorithmException) {
            throw RuntimeException(codec + " codec not available")
        }
    }

    fun md5hex(source: String): String {
        return digest("MD5", source)
    }

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

    private fun generateDeviceId(username: String, password: String): String {
        val seed = md5hex(username + password)
        val volatileSeed = "12345"
        return "android-" + md5hex(seed + volatileSeed).substring(0, 16)
    }


    private fun generateUuid(dash: Boolean): String {
        val uuid = UUID.randomUUID().toString()

        return if (dash) {
            uuid
        } else uuid.replace("-".toRegex(), "")

    }


    fun getInstagramToken(result: MediatorLiveData<Resource<InstagramLoginResult>>): LiveData<String> {
        val liveDataToken = MutableLiveData<Headers?>()
        result.value = Resource.loading(null)
        mInstagramRepository.requestCsrfToken(liveDataToken)

        liveDataToken.observeForever {
        }
        return Transformations.switchMap(liveDataToken) {
            return@switchMap MutableLiveData<String>(cookieUtils.findCookie(it!!, "csrftoken"))
        }
    }


    fun instagramLogin(
        username: String,
        password: String,
        result: MediatorLiveData<Resource<InstagramLoginResult>>
    ) {

        getInstagramToken(result).observeForever {

            val cookie = cookieUtils.getLocalCookie()
            val deviceId = generateDeviceId(username, password)

            val instagramLoginPayload =
                InstagramLoginPayload(
                    username,
                    cookie.phoneID,
                    it!!,
                    cookie.guid,
                    cookie.adid,
                    deviceId,
                    password
                )
            StorageUtils.saveLastLoginData(application,instagramLoginPayload)
            mInstagramRepository.login(
                result,
                instagramLoginPayload,
                { cookieUtils.getHeaders() },
                { t -> getPayload(t) }
            )
        }
    }

    fun isLogged(): Boolean {
        val user = StorageUtils.getUserData(application)
        return user != null
    }


    fun checkTwoFactorCode(
        responseLiveData: MediatorLiveData<Resource<InstagramLoginResult>>,
        instagramTwoFactorInfo: InstagramTwoFactorInfo,
        code: String
    ) {
        val instagramLoginTwoFactorPayload =
            InstagramLoginTwoFactorPayload.fromCookie(cookieUtils.getLocalCookie()).apply {
                username = instagramTwoFactorInfo.username
                verification_code = code
                two_factor_identifier = instagramTwoFactorInfo.twoFactorIdentifier
            }

        mInstagramRepository.verifyTwoFactor(
            responseLiveData,
            instagramLoginTwoFactorPayload,
            { cookieUtils.getHeaders() },
            { t -> getPayload(t) }
        )
    }

    private fun getPayload(obj: Any): RequestBody {
        val payload = gson.toJson(obj)
        val signatureBody = InstagramHashUtils.generateSignature(payload)
        return RequestBody.create(
            okhttp3.MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
            signatureBody
        )
    }

    fun saveCookie(headers: Headers?) {
        if(headers == null)
            return
        cookieUtils.saveCookie(headers)
    }

    fun saveUserData(
        loggedInUser: InstagramLoggedUser?,
        headers: Headers?
    ) {
        if(loggedInUser == null)
            return
        val instagramLoginPayload = StorageUtils.getLastLoginData(application)
        loggedInUser.cookie = cookieUtils.getCookieFromHeadersAndLocalData(headers!!)
        loggedInUser.password = instagramLoginPayload!!.password
        StorageUtils.saveLoggedInUserData(application,loggedInUser)
    }

    fun getUserData():InstagramLoggedUser?{
        return StorageUtils.getUserData(application)
    }

    fun getDirectInbox(responseLiveData: MediatorLiveData<Resource<InstagramInbox>>){
        mInstagramRepository.getDirectInbox(responseLiveData) {cookieUtils.getHeaders()}
    }
    fun getLastLoginData():InstagramLoginPayload?{
        return StorageUtils.getLastLoginData(application)
    }

    fun resetUserData() {
        StorageUtils.removeLoggedData(application)
        cookieUtils.removeCookie()
    }
}