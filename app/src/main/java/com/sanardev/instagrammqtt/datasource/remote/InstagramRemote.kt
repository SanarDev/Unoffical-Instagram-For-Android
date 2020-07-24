package com.sanardev.instagrammqtt.datasource.remote

import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.PresenceResponse
import com.sanardev.instagrammqtt.datasource.model.ResponseDirectAction
import com.sanardev.instagrammqtt.datasource.model.event.MessageResponse
import com.sanardev.instagrammqtt.datasource.model.response.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface InstagramRemote {

    @POST(InstagramConstants.API_VERSION + "accounts/login/")
    fun login(
        @HeaderMap header: Map<String, String>,
        @Body payload: RequestBody
    ): Call<InstagramLoginResult>

    @GET("/")
    fun getToken(): Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "accounts/two_factor_login/")
    fun twoFactorLogin(
        @HeaderMap header: Map<String, String>,
        @Body payload: RequestBody
    ): Call<InstagramLoginResult>

    @GET(InstagramConstants.API_VERSION + "direct_v2/inbox/")
    fun getDirectIndex(
        @HeaderMap header: Map<String, String>,
        @Query("visual_message_return_type") visualMessageReturnType: String = "unseen",
        @Query("thread_message_limit") threadMessageLimit: Int = 10,
        @Query("persistentBadging") persistentBadging: Boolean = true,
        @Query("limit") limit: Int = 20
    ): Call<InstagramDirects>

    @GET(InstagramConstants.API_VERSION + "direct_v2/inbox/")
    fun loadMoreDirects(
        @HeaderMap header: Map<String, String>,
        @Query("visual_message_return_type") visualMessageReturnType: String = "unseen",
        @Query("cursor") cursor: String,
        @Query("direction") direction: String = "older",
        @Query("seq_id") seqId: Int,
        @Query("thread_message_limit") threadMessageLimit: Int = 10,
        @Query("persistentBadging") persistentBadging:Boolean = true,
        @Query("limit") limit:Int=10
    ):Call<InstagramDirects>

    @GET(InstagramConstants.API_VERSION + "direct_v2/threads/{threadId}/")
    fun getChats(
        @HeaderMap header: Map<String, String>,
        @Path("threadId") threadId: String,
        @Query("visual_message_return_type") visualMessageReturnType: String = "unseen",
        @Query("limit") limit: Int = 10,
        @Query("seq_id") seqID: Int = 0
    ): Call<InstagramChats>

    @GET(InstagramConstants.API_VERSION + "direct_v2/threads/{threadId}/")
    fun loadMoreChats(
        @HeaderMap header: Map<String, String>,
        @Path("threadId") threadId: String,
        @Query("visual_message_return_type") visualMessageReturnType: String = "unseen",
        @Query("direction") direction: String = "older",
        @Query("cursor") cursor: String,
        @Query("limit") limit: Int = 20,
        @Query("seq_id") seqID: Int = 0
    ): Call<InstagramChats>

    @POST(InstagramConstants.API_VERSION + "push/register/")
    fun sendPushRegister(
        @HeaderMap header: Map<String, String>,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @GET(InstagramConstants.API_VERSION + "direct_v2/get_presence/")
    fun getDirectPresence(@HeaderMap header: Map<String, String>): Call<PresenceResponse>

    @GET(InstagramConstants.API_VERSION + "users/search/")
    fun searchUser(
        @HeaderMap header: Map<String, String>,
        @Query("search_surface") searchSurface: String = "user_search_page",
        @Query("timezone_offset") timeZoneOffset: Int = 16200,
        @Query("count") count: Int = 30,
        @Query("q") query: String
    ): Call<ResponseBody>

    @GET(InstagramConstants.API_VERSION + "direct_v2/ranked_recipients/")
    fun getRecipients(
        @HeaderMap header: Map<String, String>,
        @Query("mode") mode: String = "raven",
        @Query("show_threads") showThreads: Boolean = true
    ): Call<InstagramRecipients>

    @GET(InstagramConstants.API_VERSION + "direct_v2/ranked_recipients/")
    fun searchRecipients(
        @HeaderMap header: Map<String, String>,
        @Query("mode") mode: String = "raven",
        @Query("show_threads") showThreads: Boolean = true,
        @Query("query") query: String
    ): Call<InstagramRecipients>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/broadcast/reaction/")
    fun sendReaction(
        @HeaderMap header: Map<String, String>,
        @Body requestBody: RequestBody
    ): Call<ResponseDirectAction>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/{thread_id}/items/{item_id}/seen/")
    fun markAsSeen(
        @HeaderMap header: Map<String, String>,
        @Path("thread_id") threadId: String,
        @Path("item_id") itemId: String,
        @Body requestBody: RequestBody
    ): Call<ResponseDirectAction>

    @POST(InstagramConstants.API_VERSION + "direct_v2/visual_threads/{thread_id}/item_seen/")
    fun markAsSeenRavenMedia(
        @HeaderMap header: Map<String, String>,
        @Path("thread_id") threadId: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @GET("rupload_igvideo/{upload_name}")
    fun getMediaUploadUrl(
        @HeaderMap header: Map<String, String>,
        @Path("upload_name") uploadName: String
    ): Call<ResponseBody>

    @GET("rupload_igphoto/{upload_name}")
    fun getMediaImageUploadUrl(
        @HeaderMap header: Map<String, String>,
        @Path("upload_name") uploadName: String
    ): Call<ResponseBody>

    @POST("rupload_igvideo/{upload_name}")
    fun uploadMedia(
        @HeaderMap header: Map<String, String>,
        @Path("upload_name") uploadName: String,
        @Body mediaRequestBody: RequestBody
    ): Call<ResponseBody>

    @POST("rupload_igphoto/{upload_name}")
    fun uploadMediaImage(
        @HeaderMap header: Map<String, String>,
        @Path("upload_name") uploadName: String,
        @Body mediaRequestBody: RequestBody
    ): Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "media/upload_finish/")
    fun uploadFinish(
        @HeaderMap header: Map<String, String>,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/broadcast/share_voice/")
    fun sendMediaVoice(
        @HeaderMap header: Map<String, String>,
        @Body requestBody: RequestBody
    ): Call<InstagramSendItemResponse>

    @POST(InstagramConstants.API_VERSION + "media/upload_finish/")
    fun videoUploadFinish(
        @HeaderMap header: Map<String, String>,
        @Body requestBody: RequestBody,
        @Query("video") isVideo: Boolean = true
    ): Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/broadcast/configure_video/")
    fun sendMediaVideo(
        @HeaderMap header: Map<String, String>,
        @Body requestBody: RequestBody
    ): Call<MessageResponse>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/broadcast/configure_photo/")
    fun sendMediaImage(
        @HeaderMap header: Map<String, String>,
        @Body requestBody: RequestBody
    ): Call<MessageResponse>

    @GET(InstagramConstants.API_VERSION + "direct_v2/threads/get_by_participants/")
    fun getByParticipants(
        @HeaderMap header: Map<String, String>,
        @Path("recipient_users") recipientUsers: String,
        seqOd: Int,
        limit: Int = 20
    ): Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/broadcast/link/")
    fun sendLinkMessage(
        @HeaderMap header: Map<String, String>,
        @Body requestBody: RequestBody
    ): Call<MessageResponse>


}