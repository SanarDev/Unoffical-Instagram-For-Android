package com.idirect.app.datasource.remote

import com.idirect.app.constants.InstagramConstants
import com.idirect.app.datasource.model.PresenceResponse
import com.idirect.app.datasource.model.ResponseDirectAction
import com.idirect.app.datasource.model.event.MessageResponse
import com.idirect.app.datasource.model.response.*
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
        @Query("thread_message_limit") threadMessageLimit: Int = 20,
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

    @GET(InstagramConstants.API_VERSION  + "media/{media_id}/info/")
    fun getMediaById(@HeaderMap header: Map<String, String>,@Path("media_id") mediaId:String):Call<InstagramPost>

    @GET(InstagramConstants.API_VERSION + "users/{user_id}/info/")
    fun getUserInfo(@HeaderMap header: Map<String, String>, @Path("user_id")userId:Long):Call<InstagramUserInfo>

    @POST(InstagramConstants.API_VERSION  + "accounts/logout/")
    fun logout(@HeaderMap header: Map<String, String>,@Body requestBody: RequestBody):Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/{thread_id}/items/{item_id}/delete/")
    fun unsendMessage(@HeaderMap header: Map<String, String>, @Path("thread_id") threadId: String,@Path("item_id") itemId: String,@Body requestBody: RequestBody):Call<ResponseBody>

    @GET(InstagramConstants.API_VERSION + "feed/user/{user_id}/")
    fun getUserPosts(@HeaderMap header: Map<String, String>,
                     @Path("user_id") userId:Long,
                     @Query("exclude_comment") excludeComment:Boolean = false,
                     @Query("only_fetch_first_carousel_media") onlyFetchFirstCarouselMedia:Boolean = false):Call<InstagramPostsResponse>

    @GET(InstagramConstants.API_VERSION + "feed/user/{user_id}")
    fun getMorePosts(@HeaderMap header: Map<String, String>,
                     @Path("user_id")userId: Long,
                     @Query("exclude_comment") excludeComment:Boolean = false,
                     @Query("only_fetch_first_carousel_media") onlyFetchFirstCarouselMedia:Boolean = false,
                     @Query("max_id") previousPostId:String
                     ):Call<InstagramPostsResponse>

    @POST(InstagramConstants.API_VERSION + "media/{media_id}/like/")
    fun likePost(@HeaderMap header: Map<String, String>,
                 @Path("media_id") mediaId:String,
                 @Body requestBody: RequestBody
    ):Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "media/{media_id}/unlike/")
    fun unlikePost(@HeaderMap header: Map<String, String>,
                 @Path("media_id") mediaId:String,
                 @Body requestBody: RequestBody
    ):Call<ResponseBody>

    @GET(InstagramConstants.API_VERSION + "media/{media_id}/comments/")
    fun getPostComments(
        @HeaderMap header: Map<String, String>,
        @Path("media_id") mediaId: String,
        @Query("inventory_source") inventorySource:String = "media_or_ad",
        @Query("carousel_index") carouselIndex:Int=1,
        @Query("analytics_module") analyticsModule:String = "comments_v2",
        @Query("can_support_threading") canSupportThreading:Boolean = true,
        @Query("is_carousel_bumped_post") isCarouselBumpedPost:Boolean = true
    ):Call<InstagramCommentResponse>

    @GET(InstagramConstants.API_VERSION + "media/{media_id}/comments/")
    fun loadMorePostComments(
        @HeaderMap header: Map<String, String>,
        @Path("media_id") mediaId: String,
        @Query("min_id") minId:String,
        @Query("inventory_source") inventorySource:String = "media_or_ad",
        @Query("analytics_module") analyticsModule:String = "comments_v2",
        @Query("can_support_threading") canSupportThreading:Boolean = true,
        @Query("is_carousel_bumped_post") isCarouselBumpedPost:Boolean = true
    ):Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION  + "media/{media_id}/comment_like/")
    fun likeComment(@HeaderMap header: Map<String, String>, @Path("media_id")mediaId: String,@Body requestBody: RequestBody):Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION  + "media/{media_id}/comment_unlike/")
    fun unlikeComment(@HeaderMap header: Map<String, String>, @Path("media_id")mediaId: String,@Body requestBody: RequestBody):Call<ResponseBody>

    @GET(InstagramConstants.API_VERSION + "users/{user_name}/usernameinfo/")
    fun getUsernameInfo(@HeaderMap header: Map<String, String>,@Path("user_name") userUsername:String,@Query("from_module") fromModule:String = "feed_timeline"):Call<InstagramUserInfo>

    @POST(InstagramConstants.API_VERSION + "feed/timeline/")
    fun getFeedTimeline(@HeaderMap header: Map<String, String>, @Body requestBody: RequestBody):Call<InstagramFeedTimeLineResponse>
    
    @POST(InstagramConstants.API_VERSION + "feed/reels_tray/")
    fun getStoryTimeline(@HeaderMap header: Map<String, String>, @Body requestBody: RequestBody):Call<InstagramStoriesResponse>

    @POST(InstagramConstants.API_VERSION + "feed/reels_media/")
    fun getStoryMedia(@HeaderMap header: Map<String, String>, @Body requestBody: RequestBody):Call<InstagramStoryMediaResponse>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/broadcast/reel_react/")
    fun sendStoryReaction(@HeaderMap header: Map<String, String>, @Body requestBody: RequestBody):Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/broadcast/reel_share/")
    fun sendStoryReplyMessage(@HeaderMap header: Map<String, String>, @Body requestBody: RequestBody):Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/broadcast/media_share/")
    fun shareMedia(@HeaderMap header: Map<String, String>, @Body requestBody: RequestBody,@Query("media_type") mediaType:String):Call<ResponseBody>

    @POST(InstagramConstants.API_VERSION + "direct_v2/threads/broadcast/story_share/")
    fun shareStory(@HeaderMap header: Map<String, String>, @Body requestBody: RequestBody,@Query("media_type") mediaType:String):Call<ResponseBody>
}