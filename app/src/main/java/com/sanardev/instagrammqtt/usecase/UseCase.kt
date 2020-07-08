package com.sanardev.instagrammqtt.usecase

import android.app.Application
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.Cookie
import com.sanardev.instagrammqtt.datasource.model.FbnsAuth
import com.sanardev.instagrammqtt.datasource.model.NotificationContentJson
import com.sanardev.instagrammqtt.datasource.model.PresenceResponse
import com.sanardev.instagrammqtt.datasource.model.event.MessageEvent
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginTwoFactorPayload
import com.sanardev.instagrammqtt.datasource.model.response.*
import com.sanardev.instagrammqtt.repository.InstagramRepository
import com.sanardev.instagrammqtt.utils.*
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Query
import java.io.File
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class UseCase(
    var application: Application,
    var mInstagramRepository: InstagramRepository,
    var cookieUtils: CookieUtils,
    var mHandler: Handler,
    var gson: Gson
) {
    val XLATE = "0123456789abcdef"


    private val audioList = HashMap<String, InputStream>()

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

            val cookie = getCookie()
            cookie.csrftoken = it!!
            val deviceId = generateDeviceId(username, password)

            val instagramLoginPayload =
                InstagramLoginPayload(
                    username,
                    cookie.phoneID,
                    cookie.csrftoken!!,
                    cookie.guid,
                    cookie.adid,
                    deviceId,
                    password
                )
            StorageUtils.saveLastLoginData(application, instagramLoginPayload)
            StorageUtils.saveLoginCookie(application, cookie)
            mInstagramRepository.login(
                result,
                instagramLoginPayload,
                { getHeaders() },
                { t -> getPayload(t) }
            )
        }
    }

    fun isLogged(): Boolean {
        val user = StorageUtils.getUserData(application)
        return user != null
    }

    fun getDirectPresence(responseLiveData: MediatorLiveData<Resource<PresenceResponse>>) {
        mInstagramRepository.getDirectPresence(responseLiveData,headersGenerator = {getHeaders()})
    }


    fun checkTwoFactorCode(
        responseLiveData: MediatorLiveData<Resource<InstagramLoginResult>>,
        instagramTwoFactorInfo: InstagramTwoFactorInfo,
        code: String
    ) {
        val instagramLoginTwoFactorPayload =
            InstagramLoginTwoFactorPayload.fromCookie(getCookie()).apply {
                username = instagramTwoFactorInfo.username
                verification_code = code
                two_factor_identifier = instagramTwoFactorInfo.twoFactorIdentifier
            }

        mInstagramRepository.verifyTwoFactor(
            responseLiveData,
            instagramLoginTwoFactorPayload,
            { getHeaders() },
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

    private fun formUrlEncode(obj: Map<*, *>): RequestBody {
        val parsedData = urlEncodeUTF8(obj)
        Log.i(InstagramConstants.DEBUG_TAG, "FormUrlEncode $parsedData ");
        return RequestBody.create(
            okhttp3.MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
            parsedData
        )
    }

    fun urlEncodeUTF8(map: Map<*, *>): String? {
        val sb = StringBuilder()
        for (entry in map.entries) {
            if (sb.length > 0) {
                sb.append("&")
            }
            sb.append(
                String.format(
                    "%s=%s",
                    urlEncodeUTF8(entry.key.toString()),
                    urlEncodeUTF8(entry.value.toString())
                )
            )
        }
        return sb.toString()
    }

    fun urlEncodeUTF8(s: String?): String? {
        return try {
            URLEncoder.encode(s, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            throw UnsupportedOperationException(e)
        }
    }

    fun saveUserData(
        loggedInUser: InstagramLoggedUser?,
        headers: Headers?
    ) {
        if (loggedInUser == null)
            return
        val instagramLoginPayload = StorageUtils.getLastLoginData(application)
        loggedInUser.cookie = cookieUtils.getCookieFromHeadersAndLocalData(
            headers!!,
            StorageUtils.getCookie(application)!!
        )
        loggedInUser.password = instagramLoginPayload!!.password
        StorageUtils.saveLoggedInUserData(application, loggedInUser)
    }

    fun getUserData(): InstagramLoggedUser? {
        return StorageUtils.getUserData(application)
    }

    fun getDirectInbox(responseLiveData: MediatorLiveData<Resource<InstagramDirects>>) {
        mInstagramRepository.getDirectInbox(responseLiveData) { getHeaders() }
    }

    fun getLastLoginData(): InstagramLoginPayload? {
        return StorageUtils.getLastLoginData(application)
    }

    fun getCookie(): Cookie {
        val cookie = StorageUtils.getCookie(application)
        if (cookie != null) {
            return cookie
        } else {
            return CookieUtils.generateCookie()
        }
//        return Cookie(
//            csrftoken = "wqKWi6ifSTi0qLfYetUyqNaKbAOIz8BV",
//            rur = "PRN",
//            mid = "XvMPKgABAAHDyYM-2UzWLZ4maha_",
//            sessionID = "11292195227%3A9ZtQHcjsfe5HUO%3A10",
//        phoneID = "2c03e37d-85cb-4e1e-b313-ecf5f33e98b0",
//        adid = "6ea72bf1-7e36-4321-9dc3-f5cf567ee98e",
//        guid = "2c03e37d-85cb-4e1e-b313-ecf5f33e98b0",
//        deviceID = "")
    }

    fun getHeaders(): HashMap<String, String> {
        val cookie = getCookie()
        val user = StorageUtils.getUserData(application)
        val map = HashMap<String, String>()
        map[InstagramConstants.X_DEVICE_ID] = cookie.deviceID
        map[InstagramConstants.X_DEVICE_ID] = "en_US"
        map[InstagramConstants.X_IG_DEVICE_LOCALE] = "en_US"
        map[InstagramConstants.X_IG_MAPPED_LOCALE] = "en_US"
        map[InstagramConstants.X_PIGEON_SESSION_ID] = UUID.randomUUID().toString()
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
            "Instagram ${InstagramConstants.APP_VERSION} Android (29/10; 408dpi; ${DisplayUtils.getScreenWidth()}x${DisplayUtils.getScreenHeight()}; Xiaomi/xiaomi; Mi A2; jasmine_sprout; qcom; en_US; 200396019)"
        map[InstagramConstants.ACCEPT_LANGUAGE] = "en-US"
        map[InstagramConstants.COOKIE] =
            "mid=${cookie.mid}; csrftoken=${cookie.csrftoken};sessionid=${cookie.sessionID};dc_user=${user?.username.toString()
                .toLowerCase()};dc_user_id=${user?.pk.toString()}"
        map[InstagramConstants.X_MID] = cookie.mid.toString()
        map[InstagramConstants.ACCEPT] = "application/json"
        map[InstagramConstants.CONTENT_TYPE] = "application/x-www-form-urlencoded; charset=UTF-8"
        map[InstagramConstants.HOST] = "i.instagram.com"
        map[InstagramConstants.X_FB_HTTP_ENGINE] = "Liger"
        map[InstagramConstants.CONNECTION] = "keep-alive"
        return map
    }

    fun resetUserData() {
        StorageUtils.removeLoggedData(application)
        cookieUtils.removeCookie()
    }

    fun getChats(
        result: MediatorLiveData<Resource<InstagramChats>>,
        limit: Int = 20,
        threadId: String,
        seqID: Int
    ) {
        mInstagramRepository.getChats(result, threadId, limit, seqID, { getHeaders() })
    }


    fun pushRegister(token: String) {
        val res = MediatorLiveData<Resource<ResponseBody>>()
        val cookie = getCookie()
        val user = getUserData()

        val map = HashMap<String, String>().apply {
            put("device_type", "android_mqtt")
            put("is_main_push_channel", "true")
            put("phone_id", cookie.phoneID)
            put("device_sub_type", 2.toString())
            put("device_token", token)
            put("_csrftoken", cookie.csrftoken!!)
            put("guid", cookie.guid)
            put("_uuid", cookie.guid)
            put("users", user!!.pk!!.toString())
        }

        /*
       put(InstagramConstants.DEVICE_TYPE,"android_mqtt")
         put(InstagramConstants.IS_MAIN_PUSH_CHANNEL,true.toString())
         put(InstagramConstants.PHONE_ID,cookie.phoneID)
         put(InstagramConstants.DEVICE_SUB_TYPE,2.toString())
         put(InstagramConstants.DEVICE_TOKEN,token)
         put(InstagramConstants.CSRFTOKEN,cookie.csrftoken!!)
         put(InstagramConstants.GUID,cookie.phoneID)
         put(InstagramConstants.UUID,cookie.guid)
         put(InstagramConstants.USERS,user!!.pk!!.toString())
      */
        mHandler.post {
            mInstagramRepository.sendPushRegister(
                res,
                map,
                { t -> formUrlEncode(t) },
                { getHeaders() })
            res.observeForever {
                Log.i(
                    InstagramConstants.DEBUG_TAG,
                    "PushRegister State ${it.status.name} with data ${it.data}"
                );
            }
        }
    }

    fun getDifferentTimeString(time: Long, startFromDay: Boolean = true): String {
        val nowTime = System.currentTimeMillis()
        val differentTime = (nowTime - time) / 1000
        val rightNow = (3 * 60)
        val today = 24 * 60 * 60
        val month = 30 * 24 * 60 * 60
        val year = 12 * 30 * 24 * 60 * 60
        if (differentTime < rightNow && !startFromDay) {
            return application.getString(R.string.right_now)
        }
        if (differentTime < today) {
            if (startFromDay) {
                return application.getString(R.string.today)
            }
            val hour = differentTime / (60 * 60)
            if (hour > 0) {
                return String.format(application.getString(R.string.hours_ago), hour)
            } else {
                val min = differentTime / (60)
                return String.format(application.getString(R.string.min_ago), min)
            }
        }
        if (differentTime < month) {
            val days = (differentTime / (24 * 60 * 60)).toInt()
            if (days == 1) {
                return application.getString(R.string.yesterday)
            }
            return String.format(application.getString(R.string.days_ago), days)
        }
        if (differentTime < year) {
            val month = differentTime / (30 * 24 * 60 * 60)
            return String.format(application.getString(R.string.month_ago), month)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val netDate = Date(time)
        return sdf.format(netDate)
    }

    fun isFileExist(audioSrc: String): Boolean {
        return StorageUtils.isFileExist(application, audioSrc)
    }

    fun playAudio(audioSrc: String) {
        val inputStream = audioList[audioSrc]
        if (inputStream == null) {

        } else {

        }
    }

    fun getFile(fileLiveData: MutableLiveData<File>, url: String, id: String) {
        val file = StorageUtils.getFile(application, "$id")
        if (file != null) {
            fileLiveData.postValue(file)
        } else {
            val result = MediatorLiveData<InputStream>()
            mInstagramRepository.downloadAudio(result, url)
            result.observeForever {
                StorageUtils.saveFile(application, "$id", it)
                getFile(fileLiveData, url, id)
            }
        }
    }

    fun createFileInExternalStorage(currentVoiceFileName: String) {
        StorageUtils.createFileInExternalStorage(
            application,
            application.getString(R.string.app_name),
            currentVoiceFileName
        )
    }

    fun generateFilePath(format: String): String {
        return Environment.getExternalStorageDirectory()!!.absolutePath + File.separator + format
    }

    fun saveFbnsAuthData(fbnsAuth: FbnsAuth) {
        StorageUtils.saveFbnsAuth(application, fbnsAuth)
    }

    fun getFbnsAuthData(): FbnsAuth {
        return StorageUtils.getFbnsAuth(application)
    }

    fun notify(notification: NotificationContentJson?) {
        if (notification == null) {
            return
        }
        val nc = notification.notificationContent
        NotificationUtils.notify(
            application,
            notification.connectionKey,
            "test",
            "Minista",
            nc.message
        )
    }

    fun addMessage(event: MessageEvent) {
    }

    fun loadMoreChats(result: MediatorLiveData<Resource<InstagramChats>>, cursor: String,threadId:String,seqId:Int) {
        mInstagramRepository.loadMoreChats(result,cursor,threadId,seqId,{getHeaders()})
    }

    fun searchUser(result: MediatorLiveData<Resource<ResponseBody>>, query: String){
        mInstagramRepository.searchUser(result,query) {getHeaders()}
    }

    fun getRecipients(result: MediatorLiveData<Resource<InstagramRecipients>>, query: String?=null){
        mInstagramRepository.getRecipients(result,query,{getHeaders()})
    }

}