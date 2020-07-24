package com.sanardev.instagrammqtt.usecase

import android.app.Application
import android.os.Build
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
import com.sanardev.instagrammqtt.datasource.model.*
import com.sanardev.instagrammqtt.datasource.model.event.MessageEvent
import com.sanardev.instagrammqtt.datasource.model.event.MessageResponse
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginTwoFactorPayload
import com.sanardev.instagrammqtt.datasource.model.response.*
import com.sanardev.instagrammqtt.extentions.toStringList
import com.sanardev.instagrammqtt.repository.InstagramRepository
import com.sanardev.instagrammqtt.utils.*
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList
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
                { t -> getSignaturePayload(t) }
            )
        }
    }

    fun isLogged(): Boolean {
        val user = StorageUtils.getUserData(application)
        return user != null
    }

    fun getDirectPresence(responseLiveData: MediatorLiveData<Resource<PresenceResponse>>) {
        mInstagramRepository.getDirectPresence(
            responseLiveData,
            headersGenerator = { getHeaders() })
    }

    fun sendReaction(
        itemId: String,
        threadId: String,
        clientContext: String = InstagramHashUtils.getClientContext(),
        reactionType: String = "like",
        reactionStatus: String = "created"
    ): MutableLiveData<Resource<ResponseDirectAction>> {
        val liveData = MutableLiveData<Resource<ResponseDirectAction>>()
        val cookie = StorageUtils.getCookie(application)
        val data = HashMap<String, Any>().apply {
            put("item_type", "reaction")
            put("reaction_type", reactionType)
            put("action", "send_item")
            put("thread_ids", "[$threadId]")
            put("client_context", clientContext)
            put("_csrftoken", cookie!!.csrftoken!!)
            put("mutation_token", clientContext)
            put("_uuid", cookie.adid)
            put("node_type", "item")
            put("reaction_status", reactionStatus)
            put("item_id", itemId)
            put("device_id", cookie.deviceID)
        }
        mInstagramRepository.sendReaction(
            liveData,
            { getHeaders() },
            data,
            { t -> formUrlEncode(t) })
        return liveData
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
            { t -> getSignaturePayload(t) }
        )
    }

    private fun getSignaturePayload(obj: Any): RequestBody {
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
        mInstagramRepository.getDirectInbox(responseLiveData,{ getHeaders() },20)
    }

    fun getMoreDirectItems(responseLiveData: MediatorLiveData<Resource<InstagramDirects>>,seqId: Int,cursor: String){
        mInstagramRepository.loadMoreDirects(responseLiveData,{getHeaders()},seqId,cursor)
    }

    fun sendLinkMessage(text:String,link:List<String>,threadId: String,clientContext: String): MutableLiveData<Resource<MessageResponse>> {
        val result = MutableLiveData<Resource<MessageResponse>>()
        val cookie = StorageUtils.getCookie(application)!!
        val data = HashMap<String,String>().apply {
            put("link_text",text)
            put("link_urls",link.toStringList())
            put("action","send_item")
            put("thread_ids","[$threadId]")
            put("client_context",clientContext)
            put("_csrftoken",cookie.csrftoken!!)
            put("device_id",cookie.deviceID)
            put("mutation_token",clientContext)
            put("offline_threading_id",clientContext)
            put("_uuid",cookie.adid)
        }
        mInstagramRepository.sendLinkMessage(result,{getHeaders()},data,{formUrlEncode(it)})
        return result
    }
    fun sendMediaImage(
        threadId: String,
        userId: String,
        filePath: String,
        clientContext: String
    ): MutableLiveData<Resource<MessageResponse>> {
        val liveDataGetUrl = MutableLiveData<Resource<ResponseBody>>()
        val liveDataUploadMedia = MutableLiveData<Resource<ResponseBody>>()
        val liveDataUploadFinish = MutableLiveData<Resource<ResponseBody>>()
        val liveDataResult = MutableLiveData<Resource<MessageResponse>>()

        val uploadId = InstagramHashUtils.getClientContext()
        val hash = File(filePath).name.hashCode()
        val uploadName = "${uploadId}_0_$hash"
        var type = MediaUtils.getMimeType(filePath)?:"image/jpeg"
        val path = if(type.contains("png")){
            val p = generateFilePath("$uploadId.jpeg")
            MediaUtils.convertImageFormatToJpeg(filePath,p)
            p
        }else{
            filePath
        }
        val cookie = StorageUtils.getCookie(application)!!
        val user = StorageUtils.getUserData(application)!!
        val byteLength = File(path).readBytes().size

        mInstagramRepository.getMediaImageUploadUrl(
            liveDataGetUrl,
            { getUploadImageUrlHeader(userId, uploadId) },
            uploadName
        )
        liveDataGetUrl.observeForever {
            if(it.status == Resource.Status.SUCCESS){
                mInstagramRepository.uploadMediaImage(
                    liveDataUploadMedia,
                    uploadName,
                    {
                        getUploadImageHeader(
                            byteLength,
                            userId,
                            uploadId,
                            uploadName,
                            type
                        )
                    },
                    getMediaRequestBody(path)
                )
            }
        }
//
//        liveDataUploadMedia.observeForever {
//            if(it.status == Resource.Status.SUCCESS){
//                val finishUploadData = HashMap<String, Any>()
//                finishUploadData["timezone_offset"] = TimeUtils.getTimeZoneOffset()
//                finishUploadData["_csrftoken"] = cookie.csrftoken!!
//                finishUploadData["source_type"] = "4"
//                finishUploadData["_uid"] = user.pk.toString()
//                finishUploadData["device_id"] = cookie.deviceID
//                finishUploadData["_uuid"] = cookie.adid
//                finishUploadData["upload_id"] = uploadId
//                finishUploadData["device"] = HashMap<String, String>().apply {
//                    put("manufacturer", android.os.Build.MANUFACTURER)
//                    put("model", android.os.Build.MODEL)
//                    put("android_release", Build.VERSION.RELEASE)
//                    put("android_version", Build.VERSION.SDK_INT.toString())
//                }
//                mInstagramRepository.uploadFinish(
//                    liveDataResult,
//                    { getUploadFinishHeader() },
//                    getSignaturePayload(finishUploadData)
//                )
//            }
//        }

        liveDataUploadMedia.observeForever {
            if(it.status == Resource.Status.SUCCESS){
                val sendMediaImageData = HashMap<String, String>()
                sendMediaImageData["action"] = "send_item"
                sendMediaImageData["thread_ids"] = "[${threadId}]"
                sendMediaImageData["client_context"] = clientContext
                sendMediaImageData["_csrftoken"] = cookie.csrftoken!!
                sendMediaImageData["device_id"] = cookie.deviceID
                sendMediaImageData["mutation_token"] = clientContext
                sendMediaImageData["allow_full_aspect_ratio"] = "true"
                sendMediaImageData["_uuid"] = cookie.adid
                sendMediaImageData["upload_id"] = uploadId
                sendMediaImageData["offline_threading_id"] = clientContext
                mInstagramRepository.sendMediaImage(
                    liveDataResult,
                    { getUploadFinishHeader() },
                    formUrlEncode(sendMediaImageData)
                )
            }
        }

        return liveDataResult
    }


    fun sendMediaVideo(
        threadId: String,
        userId: String,
        filePath: String,
        clientContext: String
    ): MutableLiveData<Resource<MessageResponse>> {
        val liveDataGetUrl = MutableLiveData<Resource<ResponseBody>>()
        val liveDataUploadMedia = MutableLiveData<Resource<ResponseBody>>()
        val liveDataUploadFinish = MutableLiveData<Resource<ResponseBody>>()
        val liveDataResult = MutableLiveData<Resource<MessageResponse>>()

        val uploadId = InstagramHashUtils.getClientContext()
        val hash = File(filePath).name.hashCode()
        val uploadName = "${uploadId}-0-$hash"
        val mediaWidthAndHeight = MediaUtils.getMediaWidthAndHeight(filePath)
        val mediaWidth = mediaWidthAndHeight[0]
        val mediaHeight = mediaWidthAndHeight[1]
        var type = MediaUtils.getMimeType(filePath)
        type = type
            ?: "image/jpg" ///////////////////////////////////////////////////////////////////////////

        val cookie = StorageUtils.getCookie(application)!!
        val user = StorageUtils.getUserData(application)!!
        val byteLength = File(filePath).readBytes().size
        val mediaDuration = MediaUtils.getMediaDuration(application, filePath)
        mInstagramRepository.getMediaUploadUrl(
            liveDataGetUrl,
            { getUploadVideoUrlHeader(userId, mediaHeight, mediaWidth, mediaDuration, uploadId) },
            uploadName
        )
        liveDataGetUrl.observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                mInstagramRepository.uploadMedia(
                    liveDataUploadMedia,
                    uploadName,
                    {
                        getUploadVideoHeader(
                            byteLength,
                            userId,
                            mediaDuration,
                            uploadId,
                            uploadName,
                            mediaWidth,
                            mediaHeight,
                            type
                        )
                    },
                    getMediaRequestBody(filePath)
                )
            }
        }
        liveDataUploadMedia.observeForever {
            val finishUploadData = HashMap<String, Any>()
            finishUploadData["timezone_offset"] = TimeUtils.getTimeZoneOffset()
            finishUploadData["_csrftoken"] = cookie.csrftoken!!
            finishUploadData["source_type"] = "4"
            finishUploadData["_uid"] = user.pk.toString()
            finishUploadData["device_id"] = cookie.deviceID
            finishUploadData["video_result"] = ""
            finishUploadData["_uuid"] = cookie.adid
            finishUploadData["upload_id"] = uploadId
            finishUploadData["length"] = 3.066f
            finishUploadData["device"] = HashMap<String, String>().apply {
                put("manufacturer", android.os.Build.MANUFACTURER)
                put("model", android.os.Build.MODEL)
                put("android_release", Build.VERSION.RELEASE)
                put("android_version", Build.VERSION.SDK_INT.toString())
            }
            finishUploadData["clips"] = HashMap<String, Any>().apply {
                put("length", 3.066f)
                put("source_type", "4")
            }
            finishUploadData["extra"] = HashMap<String, Any>().apply {
                put("source_width", mediaWidth)
                put("source_height", mediaHeight)
            }
            finishUploadData["audio_muted"] = false
            finishUploadData["poster_frame_index"] = 0
            mInstagramRepository.uploadFinish(
                liveDataUploadFinish,
                { getUploadFinishHeader() },
                getSignaturePayload(finishUploadData)
            )
        }
        liveDataUploadFinish.observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                val sendMediaVoiceData = HashMap<String, String>()
                sendMediaVoiceData["action"] = "send_item"
                sendMediaVoiceData["thread_ids"] = "[${threadId}]"
                sendMediaVoiceData["client_context"] = clientContext
                sendMediaVoiceData["_csrftoken"] = cookie.csrftoken!!
                sendMediaVoiceData["video_result"] = ""
                sendMediaVoiceData["device_id"] = cookie.deviceID
                sendMediaVoiceData["mutation_token"] = clientContext
                sendMediaVoiceData["_uuid"] = cookie.adid
                sendMediaVoiceData["upload_id"] = uploadId
                sendMediaVoiceData["offline_threading_id"] = clientContext
                mInstagramRepository.sendMediaVideo(
                    liveDataResult,
                    { getUploadFinishHeader() },
                    formUrlEncode(sendMediaVoiceData)
                )
            }
        }

        return liveDataResult
    }

    fun sendMediaVoice(
        threadId: String,
        userId: String,
        filePath: String,
        type: String,
        clientContext: String
    ): MutableLiveData<Resource<InstagramSendItemResponse>> {

        val liveDataGetUrl = MutableLiveData<Resource<ResponseBody>>()
        val liveDataUploadMedia = MutableLiveData<Resource<ResponseBody>>()
        val liveDataUploadFinish = MutableLiveData<Resource<ResponseBody>>()
        val liveDataResult = MutableLiveData<Resource<InstagramSendItemResponse>>()

        val uploadId = InstagramHashUtils.getClientContext()
        var hash = File(filePath).name.hashCode()
        hash = if (hash > 0) -hash else hash
        val uploadName = "${uploadId}_0_$hash"
        val byteLength = File(filePath).readBytes().size
        val mediaDuration = MediaUtils.getMediaDuration(application, filePath)
        val cookie = StorageUtils.getCookie(application)!!
        val user = StorageUtils.getUserData(application)!!

        mInstagramRepository.getMediaUploadUrl(
            liveDataGetUrl,
            { getUploadMediaUrlHeader(userId, mediaDuration, uploadId) },
            uploadName
        )
        liveDataGetUrl.observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                mInstagramRepository.uploadMedia(
                    liveDataUploadMedia,
                    uploadName,
                    {
                        getUploadMediaHeader(
                            byteLength,
                            userId,
                            mediaDuration,
                            uploadId,
                            uploadName,
                            type
                        )
                    },
                    getMediaRequestBody(filePath)
                )
            }
        }
        liveDataUploadMedia.observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                val finishUploadData = HashMap<String, Any>()
                finishUploadData["timezone_offset"] = TimeUtils.getTimeZoneOffset()
                finishUploadData["_csrftoken"] = cookie.csrftoken!!
                finishUploadData["source_type"] = "4"
                finishUploadData["_uid"] = user.pk.toString()
                finishUploadData["device_id"] = cookie.deviceID
                finishUploadData["_uuid"] = cookie.adid
                finishUploadData["upload_id"] = uploadId
                finishUploadData["device"] = HashMap<String, String>().apply {
                    put("manufacturer", android.os.Build.MANUFACTURER)
                    put("model", android.os.Build.MODEL)
                    put("android_release", Build.VERSION.RELEASE)
                    put("android_version", Build.VERSION.SDK_INT.toString())
                }
                mInstagramRepository.uploadFinish(
                    liveDataUploadFinish,
                    { getUploadFinishHeader() },
                    getSignaturePayload(finishUploadData)
                )
            }
        }

        liveDataUploadFinish.observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                val sendMediaVoiceData = HashMap<String, String>()
                sendMediaVoiceData["action"] = "send_item"
                sendMediaVoiceData["client_context"] = clientContext
                sendMediaVoiceData["_csrftoken"] = cookie.csrftoken!!
                sendMediaVoiceData["device_id"] = cookie.deviceID
                sendMediaVoiceData["mutation_token"] = clientContext
                sendMediaVoiceData["_uuid"] = cookie.adid
                sendMediaVoiceData["waveform"] = "[]"
                sendMediaVoiceData["waveform_sampling_frequency_hz"] = "10"
                sendMediaVoiceData["upload_id"] = uploadId
                sendMediaVoiceData["thread_ids"] = "[$threadId]"
                mInstagramRepository.sendMediaVoice(
                    liveDataResult,
                    { getUploadFinishHeader() },
                    formUrlEncode(sendMediaVoiceData)
                )
            }
        }
        return liveDataResult
    }


    private fun getMediaRequestBody(filePath: String): RequestBody {
        val `in`: InputStream = FileInputStream(File(filePath))
        val buf = ByteArray(`in`.available())
        while (`in`.read(buf) != -1);

        return RequestBody.create(
            okhttp3.MediaType.parse("application/octet-stream"),
            buf
        )
    }

    fun getLastLoginData(): InstagramLoginPayload? {
        return StorageUtils.getLastLoginData(application)
    }

    fun markAsSeenRavenMedia(threadId: String,messageClientContext:String,itemId: String): MutableLiveData<Resource<ResponseBody>> {
        val result = MutableLiveData<Resource<ResponseBody>>()
        val cookie = StorageUtils.getCookie(application)!!
        val user = StorageUtils.getUserData(application)
        val data = HashMap<String,String>().apply {
            put("_csrftoken",cookie.csrftoken!!)
            put("_uid",user!!.pk.toString())
            put("_uuid",cookie.adid)
            put("original_message_client_context",messageClientContext)
            put("item_ids","[$itemId]")
            put("target_item_type","raven_media")
        }
        mInstagramRepository.markAsSeenRavenMedia(result,{getHeaders()},threadId,data,{t -> getSignaturePayload(t)})
        return result
    }
    fun markAsSeen(
        threadId: String,
        itemId: String,
        clientContext: String = InstagramHashUtils.getClientContext()
    ): MutableLiveData<Resource<ResponseDirectAction>> {
        val result = MutableLiveData<Resource<ResponseDirectAction>>()
        val cookie = StorageUtils.getCookie(application)
        val data = HashMap<String, Any>().apply {
            put("thread_id", threadId)
            put("action", "mark_seen")
            put("client_context", clientContext)
            put("_csrftoken", cookie!!.csrftoken!!)
            put("_uuid", cookie!!.adid)
            put("offline_threading_id", clientContext)
        }
        mInstagramRepository.markAsSeen(
            result,
            { getHeaders() },
            threadId,
            itemId,
            data,
            { t -> formUrlEncode(t) })
        return result
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

    private fun getHeaders(): HashMap<String, String> {
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


    private fun getUploadImageUrlHeader(userId: String, uploadId: String): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[InstagramConstants.X_INSTAGRAM_RUPLOAD_PARAMS] =
            gson.toJson(HashMap<String, Any>().apply {
                put("xsharing_user_ids", userId)
                put("image_compression", gson.toJson(HashMap<String,String>().apply {
                    this["lib_name"] = "moz"
                    this["lib_version"] = "3.1.m"
                    this["quality"] = "0"
                }))
                put("upload_id", uploadId)
                put("retry_context", getRetryContext())
                put("media_type", "1")
            })
        map[InstagramConstants.X_FB_VIDEO_WATERFALL_ID] = UUID.randomUUID().toString()
        val header = getHeaders()
        header.putAll(map)
        return header
    }

    private fun getUploadImageHeader(
        byteLength: Int,
        userId: String,
        uploadId: String,
        uploadName: String,
        type: String
    ): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[InstagramConstants.X_ENTITY_LENGTH] = byteLength.toString()
        map[InstagramConstants.X_ENTITY_NAME] = uploadName
        map[InstagramConstants.X_ENTITY_TYPE] = type
        map[InstagramConstants.X_FB_VIDEO_WATERFALL_ID] = UUID.randomUUID().toString()
        map[InstagramConstants.OFFSET] = 0.toString()
        map[InstagramConstants.ACCEPT_ENCODING] = "gzip"
        map[InstagramConstants.CONTENT_TYPE] = "application/octet-stream"
        map[InstagramConstants.X_INSTAGRAM_RUPLOAD_PARAMS] =
            gson.toJson(HashMap<String, Any>().apply {
                put("xsharing_user_ids", userId)
                put("image_compression", gson.toJson(HashMap<String,String>().apply {
                    this["lib_name"] = "moz"
                    this["lib_version"] = "3.1.m"
                    this["quality"] = "0"
                }))
                put("upload_id", uploadId)
                put("retry_context", getRetryContext())
                put("media_type", "1")
            })
        val header = getHeaders()
        header.putAll(map)
        return header
    }

    private fun getUploadVideoUrlHeader(
        userId: String,
        uploadMediaHeight: Int,
        uploadMediaWidth: Int,
        uploadMediaDuration: Int,
        uploadId: String
    ): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[InstagramConstants.X_INSTAGRAM_RUPLOAD_PARAMS] =
            gson.toJson(HashMap<String, Any>().apply {
                put("xsharing_user_ids", userId)
                put("direct_v2", "1")
                put("rotate", "0")
                put("upload_media_width", uploadMediaWidth)
                put("upload_media_height", uploadMediaHeight)
                put("hflip", "false")
                put("upload_media_duration_ms", uploadMediaDuration)
                put("upload_id", uploadId)
                put("retry_context", getRetryContext())
                put("media_type", "2")
            })
        map[InstagramConstants.X_FB_VIDEO_WATERFALL_ID] = UUID.randomUUID().toString()
        val header = getHeaders()
        header.putAll(map)
        return header
    }


    private fun getUploadVideoHeader(
        byteLength: Int,
        userId: String,
        uploadMediaDuration: Int,
        uploadId: String,
        uploadName: String,
        uploadMediaWidth: Int,
        uploadMediaHeight: Int,
        type: String
    ): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[InstagramConstants.X_ENTITY_LENGTH] = byteLength.toString()
        map[InstagramConstants.X_ENTITY_NAME] = uploadName
        map[InstagramConstants.X_ENTITY_TYPE] = type
        map[InstagramConstants.X_FB_VIDEO_WATERFALL_ID] = UUID.randomUUID().toString()
        map[InstagramConstants.OFFSET] = 0.toString()
        map[InstagramConstants.ACCEPT_ENCODING] = "gzip"
        map[InstagramConstants.CONTENT_TYPE] = "application/octet-stream"
        map[InstagramConstants.X_INSTAGRAM_RUPLOAD_PARAMS] =
            gson.toJson(HashMap<String, Any>().apply {
                put("xsharing_user_ids", userId)
                put("direct_v2", "1")
                put("rotate", "0")
                put("upload_media_width", uploadMediaWidth)
                put("upload_media_height", uploadMediaHeight)
                put("hflip", "false")
                put("upload_media_duration_ms", uploadMediaDuration)
                put("upload_id", uploadId)
                put("retry_context", getRetryContext())
                put("media_type", "2")
            })
        map[InstagramConstants.X_FB_VIDEO_WATERFALL_ID] = UUID.randomUUID().toString()
        val header = getHeaders()
        header.putAll(map)
        return header
    }

    private fun getUploadMediaUrlHeader(
        userId: String,
        uploadMediaDuration: Int,
        uploadId: String
    ): Map<String, String> {
        val map = HashMap<String, String>()
        map[InstagramConstants.X_INSTAGRAM_RUPLOAD_PARAMS] =
            gson.toJson(HashMap<String, Any>().apply {
                put("xsharing_user_ids", userId)
                put("is_direct_voice", true.toString())
                put("upload_media_duration_ms", uploadMediaDuration)
                put("upload_id", uploadId)
                put("retry_context", getRetryContext())
                put("media_type", 11)
            })
        map[InstagramConstants.X_FB_VIDEO_WATERFALL_ID] = UUID.randomUUID().toString()
        val header = getHeaders()
        header.putAll(map)
        return header
    }

    private fun getUploadMediaHeader(
        byteLength: Int,
        userId: String,
        uploadMediaDuration: Int,
        uploadId: String,
        uploadName: String,
        type: String
    ): Map<String, String> {
        val map = HashMap<String, String>()
        map[InstagramConstants.X_ENTITY_LENGTH] = byteLength.toString()
        map[InstagramConstants.X_ENTITY_NAME] = uploadName
        map[InstagramConstants.X_ENTITY_TYPE] = type
        map[InstagramConstants.X_FB_VIDEO_WATERFALL_ID] = UUID.randomUUID().toString()
        map[InstagramConstants.OFFSET] = 0.toString()
        map[InstagramConstants.ACCEPT_ENCODING] = "gzip"
        map[InstagramConstants.CONTENT_TYPE] = "application/octet-stream"
        map[InstagramConstants.X_INSTAGRAM_RUPLOAD_PARAMS] =
            gson.toJson(HashMap<String, Any>().apply {
                put("xsharing_user_ids", userId)
                put("is_direct_voice", true.toString())
                put("upload_media_duration_ms", uploadMediaDuration)
                put("upload_id", uploadId)
                put("retry_context", getRetryContext())
                put("media_type", 11)
            })
        val header = getHeaders()
        header.putAll(map)
        return header
    }


    private fun getRetryContext(): HashMap<String, Any> {
        val retryContext = HashMap<String, Any>()
        retryContext["num_reupload"] = 0
        retryContext["num_step_auto_retry"] = 0
        retryContext["num_step_manual_retry"] = 0
        return retryContext
    }

    private fun getUploadFinishHeader(): Map<String, String> {
        val map = HashMap<String, String>()
        map["retry_context"] = gson.toJson(getRetryContext())
        val header = getHeaders()
        header.putAll(map)
        return header
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

    fun generateFilePath(filename: String): String {
        val wallpaperDirectory = File(StorageUtils.APPLICATION_DIR)
        if(!wallpaperDirectory.exists()){
            wallpaperDirectory.mkdirs()
        }
        return StorageUtils.APPLICATION_DIR+File.separator + filename
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

    fun loadMoreChats(
        result: MediatorLiveData<Resource<InstagramChats>>,
        cursor: String,
        threadId: String,
        seqId: Int
    ) {
        mInstagramRepository.loadMoreChats(result, cursor, threadId, seqId, { getHeaders() })
    }

    fun searchUser(result: MediatorLiveData<Resource<ResponseBody>>, query: String) {
        mInstagramRepository.searchUser(result, query) { getHeaders() }
    }

    fun getRecipients(
        result: MediatorLiveData<Resource<InstagramRecipients>>,
        query: String? = null
    ) {
        mInstagramRepository.getRecipients(result, query, { getHeaders() })
    }

    fun getUserByRecipients(result:MutableLiveData<Resource<ResponseBody>>,userId: Int,seqId:Int){
        mInstagramRepository.getByParticipants(result,{getHeaders()},"[[$userId]]",seqId)
    }

}
