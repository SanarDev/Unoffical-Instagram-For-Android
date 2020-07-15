package com.sanardev.instagrammqtt.datasource.remote

import com.sanardev.instagrammqtt.datasource.model.PresenceResponse
import com.sanardev.instagrammqtt.datasource.model.ResponseDirectAction
import com.sanardev.instagrammqtt.datasource.model.response.InstagramChats
import com.sanardev.instagrammqtt.datasource.model.response.InstagramDirects
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.datasource.model.response.InstagramRecipients
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface InstagramRemote {

    @POST("accounts/login/")
    fun login(
        @HeaderMap header: Map<String, String>,
        @Body payload: RequestBody
    ): Call<InstagramLoginResult>

    @GET("/")
    fun getToken(): Call<ResponseBody>

    @POST("accounts/two_factor_login/")
    fun twoFactorLogin(
        @HeaderMap header: Map<String, String>,
        @Body payload: RequestBody
    ): Call<InstagramLoginResult>

    @GET("direct_v2/inbox/")
    fun getDirectIndex(
        @HeaderMap header: Map<String, String>,
        @Query("visual_message_return_type") visualMessageReturnType: String = "unseen",
        @Query("thread_message_limit") threadMessageLimit: Int = 10,
        @Query("persistentBadging") persistentBadging: Boolean = true,
        @Query("limit") limit: Int = 50
    ): Call<InstagramDirects>

    @GET("direct_v2/threads/{threadId}/")
    fun getChats(
        @HeaderMap header: Map<String, String>,
        @Path("threadId") threadId: String,
        @Query("visual_message_return_type") visualMessageReturnType: String = "unseen",
        @Query("limit") limit: Int = 10,
        @Query("seq_id") seqID: Int = 0
    ): Call<InstagramChats>

    @GET("direct_v2/threads/{threadId}/")
    fun loadMoreChats(
        @HeaderMap header: Map<String, String>,
        @Path("threadId") threadId: String,
        @Query("visual_message_return_type") visualMessageReturnType: String = "unseen",
        @Query("direction") direction: String = "older",
        @Query("cursor") cursor: String,
        @Query("limit") limit: Int = 20,
        @Query("seq_id") seqID: Int = 0
    ): Call<InstagramChats>

    @POST("push/register/")
    fun sendPushRegister(
        @HeaderMap header: Map<String, String>,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @GET("direct_v2/get_presence/")
    fun getDirectPresence(@HeaderMap header: Map<String, String>): Call<PresenceResponse>

    @GET("users/search/")
    fun searchUser(
        @HeaderMap header: Map<String, String>,
        @Query("search_surface") searchSurface: String = "user_search_page",
        @Query("timezone_offset") timeZoneOffset:Int = 16200,
        @Query("count") count:Int=30,
        @Query("q") query: String
        ):Call<ResponseBody>

    @GET("direct_v2/ranked_recipients/")
    fun getRecipients(@HeaderMap header: Map<String, String>, @Query("mode") mode:String = "raven",@Query("show_threads")showThreads:Boolean=true):Call<InstagramRecipients>

    @GET("direct_v2/ranked_recipients/")
    fun searchRecipients(@HeaderMap header: Map<String, String>, @Query("mode") mode:String = "raven",@Query("show_threads")showThreads:Boolean=true,@Query("query")query:String):Call<InstagramRecipients>

    @POST("direct_v2/threads/broadcast/reaction/")
    fun sendReaction(@HeaderMap header: Map<String, String>, @Body requestBody: RequestBody):Call<ResponseDirectAction>

    @POST("direct_v2/threads/{thread_id}/items/{item_id}/seen/")
    fun markAsSeen(@HeaderMap header: Map<String, String>,@Path("thread_id") threadId: String,@Path("item_id") itemId:String,@Body requestBody: RequestBody):Call<ResponseDirectAction>
}