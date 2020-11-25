package com.idirect.app.usecase

import android.app.Application
import android.content.Context
import android.os.Handler
import com.google.gson.Gson
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseApplication
import com.idirect.app.datasource.model.*
import com.idirect.app.extentions.isServiceRunning
import com.idirect.app.datasource.repository.InstagramRepository
import com.idirect.app.realtime.service.RealTimeService
import com.idirect.app.utils.*
import com.sanardev.instagramapijava.model.direct.IGThread
import com.sanardev.instagramapijava.model.login.IGLoggedUser
import com.sanardev.instagramapijava.model.login.IGTwoFactorInfo
import com.sanardev.instagramapijava.model.user.CurrentUserCache
import com.sanardev.instagramapijava.request.IGLogoutRequest
import com.sanardev.instagramapijava.response.*
import io.reactivex.Observable
import okhttp3.ResponseBody
import java.io.InputStream
import kotlin.collections.HashMap
import kotlin.random.Random


class UseCase(
    var application: Application,
    var mInstagramRepository: InstagramRepository,
    var mHandler: Handler,
    var gson: Gson
) {

    var isNotificationEnable: Boolean
        get() {
            return application.getSharedPreferences(
                InstagramConstants.SharedPref.USER.name,
                Context.MODE_PRIVATE
            )
                .getBoolean("is_notification_enabled", true)
        }
        set(value) {
            application.getSharedPreferences(
                InstagramConstants.SharedPref.USER.name,
                Context.MODE_PRIVATE
            )
                .edit()
                .putBoolean("is_notification_enabled", value)
                .apply()
        }

    var isSeenMessageEnable: Boolean
        get() {
            return application.getSharedPreferences(
                InstagramConstants.SharedPref.USER.name,
                Context.MODE_PRIVATE
            )
                .getBoolean("is_seen_message_enabled", true)
        }
        set(value) {
            application.getSharedPreferences(
                InstagramConstants.SharedPref.USER.name,
                Context.MODE_PRIVATE
            )
                .edit()
                .putBoolean("is_seen_message_enabled", value)
                .apply()
        }

    private val audioList = HashMap<String, InputStream>()

    fun generateFilePath(filename: String): String {
        return StorageUtils.generateFileInInternalStorage(application, filename).path
    }

    fun saveFbnsAuthData(fbnsAuth: FbnsAuth) {
        StorageUtils.saveFbnsAuth(application, fbnsAuth)
    }

    fun getFbnsAuthData(): FbnsAuth {
        return StorageUtils.getFbnsAuth(application)
    }

    fun isUserLogged(): Boolean {
        return false
    }

    fun pushRegister(token: String) {
//        val res = MediatorLiveData<Resource<ResponseBody>>()
//        val cookie = getCookie()
//        val user = getUserData()
//
//        val map = HashMap<String, String>().apply {
//            put("device_type", "android_mqtt")
//            put("is_main_push_channel", "true")
//            put("phone_id", cookie.phoneID)
//            put("device_sub_type", 2.toString())
//            put("device_token", token)
//            put("_csrftoken", cookie.csrftoken!!)
//            put("guid", cookie.guid)
//            put("_uuid", cookie.guid)
//            put("users", user!!.pk!!.toString())
//        }
//
//        /*
//       put(InstagramConstants.DEVICE_TYPE,"android_mqtt")
//         put(InstagramConstants.IS_MAIN_PUSH_CHANNEL,true.toString())
//         put(InstagramConstants.PHONE_ID,cookie.phoneID)
//         put(InstagramConstants.DEVICE_SUB_TYPE,2.toString())
//         put(InstagramConstants.DEVICE_TOKEN,token)
//         put(InstagramConstants.CSRFTOKEN,cookie.csrftoken!!)
//         put(InstagramConstants.GUID,cookie.phoneID)
//         put(InstagramConstants.UUID,cookie.guid)
//         put(InstagramConstants.USERS,user!!.pk!!.toString())
//      */
//        mHandler.post {
//            mInstagramRepository.sendPushRegister(
//                res,
//                formUrlEncode(map),
//                getHeaders()
//            )
//            res.observeForever {
//                if (it.status == Resource.Status.SUCCESS) {
//                    saveFbnsRegisterToken(token)
//                }
//            }
//        }
    }


    fun notifyDirectMessage(notification: NotificationContentJson?) {
//        notification?.let {
//            mHandler.post {
//                application.toast(it.notificationContent.collapseKey)
//            }
//        }
        if (notification == null ||
            !isNotificationEnable ||
            !notification.notificationContent.collapseKey.contains("direct") ||
            (!BaseApplication.isAppInOnStop && application.isServiceRunning(RealTimeService::class.java))
        ) {
            return
        }
        val nc = notification.notificationContent
        val senderName = nc.message.split(":")[0].trim()
        var message = nc.message.split(":")[1].trim()
        val notificationDataPref =
            application.getSharedPreferences(
                InstagramConstants.SharedPref.NOTIFICATION_DATA.name,
                Context.MODE_PRIVATE
            )

        val keyMessages = senderName.replace(" ", "_") + "_Messages"
        val keyNotificationId = senderName.replace(" ", "_") + "_NotificationId"
        var oldMessage = notificationDataPref.getString(keyMessages, "")
        var notificationId = notificationDataPref.getInt(keyNotificationId, 0)
        if (notificationId == 0)
            notificationId = Random.nextInt()

        var isLighLevel = true

        oldMessage = oldMessage + "\n" + message
//            isLighLevel = false
//        }
        notificationDataPref.edit()
            .putString(keyMessages, oldMessage)
            .putInt(keyNotificationId, notificationId)
            .apply()

        val channelId = senderName.hashCode().toString()
        NotificationUtils.notify(
            application = application,
            channelId = channelId,
            notificationId = notificationId,
            channelName = senderName,
            title = senderName,
            message = message,
            oldMessage = oldMessage.split("\n"),
            photoUrl = notification.notificationContent.optionAvatarUrl,
            isHighLevel = isLighLevel
        )
    }

    fun dismissAllNotification() {
        NotificationUtils.dismissAllNotification(application)
        application.getSharedPreferences(
            InstagramConstants.SharedPref.NOTIFICATION_DATA.name,
            Context.MODE_PRIVATE
        ).edit().clear()
            .apply()
    }

    fun getLastFbnsRegisterToken() =
        application.getSharedPreferences(
            InstagramConstants.SharedPref.FBNS_DATA.name,
            Context.MODE_PRIVATE
        )
            .getString("register_token", null)

    fun getLastFbnsTokenTimeStamp() =
        application.getSharedPreferences(
            InstagramConstants.SharedPref.FBNS_DATA.name,
            Context.MODE_PRIVATE
        )
            .getLong("time_stamp", 0)


    fun saveFbnsRegisterToken(token: String) {
        application.getSharedPreferences(
            InstagramConstants.SharedPref.FBNS_DATA.name,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString("register_token", token)
            .putLong("time_stamp", System.currentTimeMillis())
            .apply()
    }

    private fun clearAllData() {
        StorageUtils.removeFiles(
            application,
            StorageUtils.LAST_LOGIN_DATA_FILE_NAME,
            StorageUtils.USER_DATA_FILE_NAME,
            StorageUtils.COOKIE_BEFORE_LOGIN,
            StorageUtils.FBNS_AUTH
        )

        application.getSharedPreferences(
            InstagramConstants.SharedPref.USER.name,
            Context.MODE_PRIVATE
        ).edit().clear().apply()
        application.getSharedPreferences(
            InstagramConstants.SharedPref.FBNS_DATA.name,
            Context.MODE_PRIVATE
        ).edit().clear().apply()
        application.getSharedPreferences(
            InstagramConstants.SharedPref.NOTIFICATION_DATA.name,
            Context.MODE_PRIVATE
        ).edit().clear().apply()
    }

    fun getCurrentUser(): CurrentUserCache? {
        return mInstagramRepository.getCurrentUser()
    }

    fun getTimelinePosts(): Observable<IGTimeLinePostsResponse> {
        return mInstagramRepository.getTimelinePosts()
    }

    fun getTimelineStory(): Observable<IGTimeLineStoryResponse> {
        return mInstagramRepository.getTimelineStory()
    }

    fun getStoryMedia(userId: Long): Observable<IGStoryMediaResponse> {
        return mInstagramRepository.getStoryMedia(userId)
    }

    fun unlikePost(id: String): Observable<IGUnlikePostResponse> {
        return mInstagramRepository.unlikePost(id)
    }

    fun likePost(id: String): Observable<IGLikePostResponse> {
        return mInstagramRepository.likePost(id)
    }

    fun loadMorePosts(nextMaxId: String): Observable<IGTimeLinePostsResponse> {
        return mInstagramRepository.loadMorePosts(nextMaxId)
    }

    fun likeComment(id: Long): Observable<ResponseBody> {
        return mInstagramRepository.likeComment(id)
    }

    fun unlikeComment(id: Long): Observable<ResponseBody> {
        return mInstagramRepository.unlikeComment(id)
    }

    fun login(username: String, password: String): Observable<IGLoginResponse> {
        return mInstagramRepository.login(username, password)
    }

    fun getRecipient(query: String? = null): Observable<IGRecipientsResponse> {
        return mInstagramRepository.getRecipient(query)
    }

    fun getLoggedUser(): IGLoggedUser? {
        return mInstagramRepository.getLoggedUser()
    }

    fun shareStory(
        threadId: String,
        mediaId: String,
        mediaType: Int,
        reelId: Long
    ): Observable<IGShareStoryResponse> {
        return mInstagramRepository.shareStory(threadId, mediaId, mediaType, reelId)
    }

    fun shareMedia(
        threadId: String,
        mediaId: String,
        mediaType: Int
    ): Observable<IGShareMediaResponse> {
        return mInstagramRepository.shareMedia(threadId, mediaId, mediaType)
    }

    fun getMediaById(mediaId: String): Observable<IGMediaResponse> {
        return mInstagramRepository.getMediaById(mediaId)
    }

    fun getDirectPresence(): Observable<IGPresenceResponse> {
        return mInstagramRepository.getDirectPresence()
    }

    fun sendMediaVoice(
        threadId: String,
        userPks: List<Long>,
        filePath: String,
        clientContext: String
    ): Observable<IGMediaVoiceResponse> {
        return mInstagramRepository.sendMediaVoice(threadId, userPks, filePath, clientContext)
    }

    fun sendMediaImage(
        threadId: String,
        userId: List<Long>,
        filePath: String,
        clientContext: String
    ): Observable<IGDirectActionResponse> {
        return mInstagramRepository.sendMediaImage(threadId, userId, filePath, clientContext)
    }

    fun sendMediaVideo(
        threadId: String,
        userId: List<Long>,
        filePath: String,
        clientContext: String
    ): Observable<IGDirectActionResponse> {
        return mInstagramRepository.sendMediaVideo(threadId, userId, filePath, clientContext)
    }

    fun sendLikeReaction(
        itemId: String,
        threadId: String,
        clientContext: String
    ): Observable<IGDirectActionResponse> {
        return mInstagramRepository.sendLikeReaction(itemId, threadId, clientContext)
    }

    fun markAsSeenRavenMedia(
        itemId: String,
        threadId: String,
        messageClientContext: String
    ): Observable<ResponseBody> {
        return mInstagramRepository.markAsSeenRavenMedia(itemId, threadId, messageClientContext)
    }

    fun unsendMessage(
        threadId: String,
        itemId: String,
        clientContext: String
    ): Observable<ResponseBody> {
        return mInstagramRepository.unsendMessage(threadId, itemId, clientContext)
    }

    fun markAsSeenMessage(threadId: String, itemId: String): Observable<IGDirectActionResponse> {
        return mInstagramRepository.markAsSeenMessage(threadId, itemId)
    }

    fun getDirectMoreChats(
        threadId: String,
        seqId: Int,
        cursor: String
    ): Observable<IGDirectChatResponse> {
        return mInstagramRepository.getDirectMoreChats(threadId, seqId, cursor)
    }

    fun logout(): Observable<IGLogoutRequest> {
        return mInstagramRepository.logout()
    }

    fun getDirectInbox(
        limit: Int = 20,
        threadMessageLimit: Int = 20
    ): Observable<IGDirectsResponse> {
        return mInstagramRepository.getDirectInbox(limit, threadMessageLimit)
    }

    fun getMoreDirectInbox(seqId: Int, cursor: String): Observable<IGDirectsResponse> {
        return mInstagramRepository.getMoreDirectInbox(seqId, cursor)
    }

    fun getThreadByParticipants(userId: Long, seqId: Int): Observable<IGParticipantsResponse> {
        return mInstagramRepository.getThreadByParticipants(userId, seqId)
    }

    fun createFakeThread(userId: Long, threadTitle: String, profileImage: String): IGThread {
        return mInstagramRepository.createFakeThread(userId, threadTitle, profileImage)
    }

    fun getPostsComments(mediaId: String): Observable<IGCommentsResponse> {
        return mInstagramRepository.getPostsComments(mediaId)
    }

    fun getPostsMoreComments(mediaId: String, nextMaxId: String): Observable<IGCommentsResponse> {
        return mInstagramRepository.getPostsMoreComments(mediaId, nextMaxId)
    }

    fun getPosts(userId: Long): Observable<IGPostsResponse> {
        return mInstagramRepository.getPosts(userId)
    }

    fun getMorePosts(userId: Long, previousPostId: String): Observable<IGPostsResponse> {
        return mInstagramRepository.getMorePosts(userId, previousPostId)
    }

    fun getMe(): Observable<IGUserInfoResponse> {
        return mInstagramRepository.getMe()
    }

    fun sendStoryReaction(
        threadId: String,
        mediaId: String,
        reaction: String,
        reelId: Long
    ): Observable<IGSendStoryReactionResponse> {
        return mInstagramRepository.sendStoryReaction(threadId, mediaId, reaction, reelId)
    }

    fun replyStory(
        threadId: String,
        mediaId: String,
        mediaType: Int,
        text: String,
        reelId: Long
    ): Observable<IGStoryReplyResponse> {
        return mInstagramRepository.replyStory(threadId, mediaId, mediaType, text, reelId)
    }

    fun voteSlider(
        vote: Float,
        sliderId: Long,
        id: String
    ): Observable<IGStoryUpdateResponse> {
        return mInstagramRepository.voteSlider(id, sliderId, vote)
    }

    fun markStoryAsSeen(trayId: Long, id: String, takenAt: Long): Observable<ResponseBody> {
        return mInstagramRepository.markStoryAsSeen(trayId, id, takenAt)
    }

    fun sendStoryQuestionResponse(
        id: String,
        questionId: Long,
        response: String
    ): Observable<ResponseBody> {
        return mInstagramRepository.sendStoryQuestionResponse(id, questionId, response)
    }

    fun twoStepAuth(username: String, password: String, code: String): Observable<IGLoginResponse> {
        return mInstagramRepository.twoStepAuth(username, password, code)
    }

    fun getTwoStepAuthInfo(username: String, password: String): IGTwoFactorInfo {
        return mInstagramRepository.getTwoStepAuthInfo(username, password)
    }

    fun getUserInfoByUsername(username: String): Observable<IGUserInfoResponse> {
        return mInstagramRepository.getUserInfoByUsername(username)
    }

    fun getUserInfo(userId: Long): Observable<IGUserInfoResponse> {
        return mInstagramRepository.getUserInfo(userId)
    }

    fun storyQuizAnswer(mediaId: String, quizId: Long, index: Int): Observable<IGStoryUpdateResponse> {
        return mInstagramRepository.storyQuizAnswer(mediaId,quizId,index)
    }
}
