package com.idirect.app.repository

import android.os.Handler
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.idirect.app.datasource.local.MessageDataSource
import com.idirect.app.datasource.model.PresenceResponse
import com.idirect.app.datasource.model.ResponseDirectAction
import com.idirect.app.datasource.model.Tray
import com.idirect.app.datasource.model.event.MessageResponse
import com.idirect.app.datasource.model.payload.InstagramLoginPayload
import com.idirect.app.datasource.model.payload.InstagramLoginTwoFactorPayload
import com.idirect.app.datasource.model.response.*
import com.idirect.app.datasource.remote.InstagramRemote
import com.idirect.app.datasource.remote.NetworkCall
import com.idirect.app.utils.Resource
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import kotlin.collections.HashMap


class InstagramRepository(
    private var mInstagramRemote: InstagramRemote,
    private var mMessageDataSource: MessageDataSource
) {


    private val mHandler = Handler()
    private val request = com.idirect.app.datasource.remote.Request()
    var userPostCache: Pair<Long, InstagramPostsResponse>? = null
    var recipients: InstagramRecipients? = null
    var userComments: InstagramCommentResponse? = null
    var stories: InstagramStoriesResponse? = null
    var userInfos = HashMap<Long,InstagramUserInfo>()
    var searchedValue = HashMap<String,InstagramRecipients>()
    var timelinePosts:InstagramFeedTimeLineResponse?=null

    fun login(
        liveData: MutableLiveData<Resource<InstagramLoginResult>>,
        header: HashMap<String, String>,
        data: RequestBody
    ) {
        NetworkCall<InstagramLoginResult>()
            .makeCall(
                mInstagramRemote.login(
                    header,
                    data
                )
            ).observeForever {
                liveData.postValue(it)
            }
    }

    fun requestCsrfToken(liveData: MutableLiveData<Headers?>) {
        mInstagramRemote.getToken().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                liveData.value = null
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                liveData.value = response.headers()
            }
        })
    }

    fun verifyTwoFactor(
        liveData: MutableLiveData<Resource<InstagramLoginResult>>,
        header: HashMap<String, String>,
        data: RequestBody
    ) {
        NetworkCall<InstagramLoginResult>()
            .makeCall(
                mInstagramRemote.twoFactorLogin(
                    header,
                    data
                )
            ).observeForever {
                liveData.postValue(it)
            }
    }

    fun getDirectInbox(
        responseLiveData: MediatorLiveData<Resource<InstagramDirects>>,
        header: HashMap<String, String>,
        limit: Int = 20
    ) {
        responseLiveData.addSource(
            NetworkCall<InstagramDirects>()
                .makeCall(
                    mInstagramRemote.getDirectIndex(
                        header,
                        limit = limit
                    )
                ), Observer {
                responseLiveData.postValue(it)
            }
        )
    }


    fun loadMoreDirects(
        responseLiveData: MediatorLiveData<Resource<InstagramDirects>>,
        header: HashMap<String, String>,
        seqId: Int,
        cursor: String,
        threadMessageLimit: Int = 10,
        limit: Int = 10
    ) {
        responseLiveData.addSource(NetworkCall<InstagramDirects>().makeCall(
            mInstagramRemote.loadMoreDirects(
                header = header,
                seqId = seqId,
                cursor = cursor,
                threadMessageLimit = threadMessageLimit,
                limit = limit
            )
        ), Observer {
            responseLiveData.postValue(it)
        })
    }

    fun getDirectPresence(
        responseLiveData: MediatorLiveData<Resource<PresenceResponse>>,
        header: HashMap<String, String>
    ) {
        responseLiveData.addSource(NetworkCall<PresenceResponse>().makeCall(
            mInstagramRemote.getDirectPresence(
                header
            )
        ),
            Observer {
                responseLiveData.postValue(it)
            })

    }

    fun getChats(
        responseLiveData: MediatorLiveData<Resource<InstagramChats>>,
        threadId: String,
        limit: Int,
        seqID: Int,
        header: HashMap<String, String>
    ) {
        responseLiveData.value = Resource.loading(null)
        responseLiveData.addSource(
            NetworkCall<InstagramChats>()
                .makeCall(
                    mInstagramRemote.getChats(
                        header,
                        threadId = threadId,
                        limit = limit,
                        seqID = seqID
                    )
                ), Observer {
                responseLiveData.postValue(it)
            })
    }


    fun sendPushRegister(
        result: MediatorLiveData<Resource<ResponseBody>>,
        data: okhttp3.RequestBody,
        header: HashMap<String, String>
    ) {
        result.addSource(NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.sendPushRegister(
                header,
                data
            )
        ),
            Observer {
                result.postValue(it)
            })
    }

    fun downloadAudio(result: MutableLiveData<InputStream>, audioSrc: String) {
        Thread {
            val client = OkHttpClient()
            val request = Request.Builder().url(audioSrc)
                .addHeader("Content-Type", "application/json")
                .build()
            val response = client.newCall(request).execute()

            val `in`: InputStream = response.body()!!.byteStream()

            mHandler.post {
                result.value = `in`
            }
            response.body()!!.close()
        }.start()
    }

    fun loadMoreChats(
        result: MutableLiveData<Resource<InstagramChats>>,
        cursor: String,
        threadId: String,
        seqId: Int,
        header: HashMap<String, String>
    ) {
        NetworkCall<InstagramChats>().makeCall(
            mInstagramRemote.loadMoreChats(
                header = header,
                cursor = cursor,
                threadId = threadId,
                seqID = seqId
            )
        ).observeForever {
            result.postValue(it)
        }
    }

    fun searchUser(
        responseLiveData: MediatorLiveData<Resource<ResponseBody>>,
        query: String,
        header: HashMap<String, String>
    ) {
        responseLiveData.addSource(NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.searchUser(header, query = query)
        ),
            Observer {
                responseLiveData.postValue(it)
            })
    }

    fun getRecipients(
        result: MutableLiveData<Resource<InstagramRecipients>>,
        query: String? = null,
        header: HashMap<String, String>
    ) {
        if (query.isNullOrBlank()) {
            recipients?.let {
                result.value = Resource.success(recipients)
                return
            }
        }else{
            searchedValue[query]?.let {
                result.value = Resource.success(it)
                return
            }
        }
        NetworkCall<InstagramRecipients>().makeCall(
            if (query == null || query.isEmpty()) mInstagramRemote.getRecipients(
                header
            ) else mInstagramRemote.searchRecipients(
                header,
                query = query
            )
        ).observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                if(query.isNullOrBlank()){
                    recipients = it.data
                }else{
                    searchedValue[query] = it.data!!
                }
            }
            result.value = (it)
        }
    }

    fun sendReaction(
        result: MutableLiveData<Resource<ResponseDirectAction>>,
        header: HashMap<String, String>,
        data: okhttp3.RequestBody
    ) {
        NetworkCall<ResponseDirectAction>().makeCall(
            mInstagramRemote.sendReaction(
                header,
                data
            )
        ).observeForever {
            result.value = it
        }

    }

    fun markAsSeen(
        result: MutableLiveData<Resource<ResponseDirectAction>>,
        header: HashMap<String, String>,
        threadId: String,
        itemId: String,
        data: okhttp3.RequestBody
    ) {
        NetworkCall<ResponseDirectAction>().makeCall(
            mInstagramRemote.markAsSeen(
                header,
                threadId,
                itemId,
                data
            )
        ).observeForever {
            result.value = it
        }
    }

    fun markAsSeenRavenMedia(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        threadId: String,
        data: okhttp3.RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.markAsSeenRavenMedia(
                header,
                threadId,
                data
            )
        ).observeForever {
            result.value = it
        }
    }

    fun getMediaUploadUrl(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        uploadName: String
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.getMediaUploadUrl(
                header,
                uploadName
            )
        ).observeForever {
            result.value = it
        }
    }

    fun getMediaImageUploadUrl(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        uploadName: String
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.getMediaImageUploadUrl(
                header,
                uploadName
            )
        ).observeForever {
            result.value = it
        }
    }

    fun uploadMedia(
        liveDataUploadMedia: MutableLiveData<Resource<ResponseBody>>,
        uploadName: String,
        header: HashMap<String, String>,
        mediaRequestBody: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.uploadMedia(
                header,
                uploadName,
                mediaRequestBody
            )
        ).observeForever {
            liveDataUploadMedia.value = it
        }
    }

    fun uploadMediaImage(
        liveDataUploadMedia: MutableLiveData<Resource<ResponseBody>>,
        uploadName: String,
        header: HashMap<String, String>,
        mediaRequestBody: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.uploadMediaImage(
                header,
                uploadName,
                mediaRequestBody
            )
        ).observeForever {
            liveDataUploadMedia.value = it
        }
    }

    fun uploadFinish(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        requestBody: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.uploadFinish(
                header,
                requestBody
            )
        ).observeForever {
            result.value = it
        }
    }

    fun sendMediaVoice(
        result: MutableLiveData<Resource<InstagramSendItemResponse>>,
        header: HashMap<String, String>,
        requestBody: RequestBody
    ) {
        NetworkCall<InstagramSendItemResponse>().makeCall(
            mInstagramRemote.sendMediaVoice(
                header,
                requestBody
            )
        ).observeForever {
            result.value = it
        }
    }

    fun sendMediaVideo(
        result: MutableLiveData<Resource<MessageResponse>>,
        header: HashMap<String, String>,
        requestBody: RequestBody
    ) {
        NetworkCall<MessageResponse>().makeCall(
            mInstagramRemote.sendMediaVideo(
                header,
                requestBody
            )
        ).observeForever {
            result.value = it
        }
    }

    fun sendMediaImage(
        result: MutableLiveData<Resource<MessageResponse>>,
        header: HashMap<String, String>,
        requestBody: RequestBody
    ) {
        NetworkCall<MessageResponse>().makeCall(
            mInstagramRemote.sendMediaImage(
                header,
                requestBody
            )
        ).observeForever {
            result.value = it
        }
    }

    fun getByParticipants(
        result: MutableLiveData<Resource<InstagramParticipantsResponse>>,
        header: HashMap<String, String>,
        userId: String,
        seqId: Int,
        limit: Int = 20
    ) {
        NetworkCall<InstagramParticipantsResponse>().makeCall(
            mInstagramRemote.getByParticipants(
                header,
                userId,
                seqId,
                limit
            )
        ).observeForever {
            result.value = it
        }
    }

    fun sendLinkMessage(
        result: MutableLiveData<Resource<MessageResponse>>,
        header: HashMap<String, String>,
        data: RequestBody
    ) {
        NetworkCall<MessageResponse>().makeCall(
            mInstagramRemote.sendLinkMessage(
                header,
                data
            )
        ).observeForever {
            result.value = it
        }
    }


    fun getMediaById(
        result: MediatorLiveData<Resource<InstagramPost>>,
        header: HashMap<String, String>,
        mediaId: String
    ) {
        result.addSource(NetworkCall<InstagramPost>().makeCall(
            mInstagramRemote.getMediaById(
                header,
                mediaId
            )
        ),
            Observer {
                result.value = (it)
            })
    }

    fun getUserInfo(
        result: MutableLiveData<Resource<InstagramUserInfo>>,
        header: HashMap<String, String>,
        userId: Long
    ) {
        if(userInfos[userId] != null){
            result.value = Resource.success(userInfos[userId])
            return
        }
        NetworkCall<InstagramUserInfo>().makeCall(
            mInstagramRemote.getUserInfo(
                header,
                userId
            )
        ).observeForever {
            if(it.status == Resource.Status.SUCCESS){
                userInfos[userId] = it.data!!
            }
            result.value = (it)
        }
    }

    fun logout(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        data: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.logout(
                header,
                data
            )
        ).observeForever {
            result.postValue(it)
        }
    }

    fun unsendMessage(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        threadId: String,
        itemId: String,
        data: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.unsendMessage(
                header,
                threadId,
                itemId,
                data
            )
        ).observeForever {
            result.value = (it)
        }
    }

    fun getUserPosts(
        userPosts: MutableLiveData<Resource<InstagramPostsResponse>>,
        userId: Long,
        header: HashMap<String, String>
    ) {
        if (userPostCache != null && userPostCache!!.first == userId) {
            userPosts.value = Resource.success(userPostCache!!.second)
            return
        }
        NetworkCall<InstagramPostsResponse>().makeCall(
            mInstagramRemote.getUserPosts(
                header,
                userId
            )
        ).observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                userPostCache = Pair(userId, it.data!!)
            }
            userPosts.value = it
        }
    }

    fun loadMoreUserPosts(
        userPosts: MutableLiveData<Resource<InstagramPostsResponse>>,
        userId: Long,
        header: HashMap<String, String>,
        previousPostId: String
    ) {
        NetworkCall<InstagramPostsResponse>().makeCall(
            mInstagramRemote.getMorePosts(
                header,
                userId,
                previousPostId = previousPostId
            )
        ).observeForever {
            if (it.status == Resource.Status.SUCCESS &&
                userPostCache != null &&
                userPostCache!!.first == userId
            ) {
                userPostCache!!.second.apply {
                    this.userPosts.addAll(it.data!!.userPosts)
                    this.isMoreAvailable = it.data!!.isMoreAvailable
                    this.numResults += it.data!!.numResults
                    this.isAutoLoadMoreEnabled = it.data!!.isAutoLoadMoreEnabled
                }
                userPosts.value = Resource.success(userPostCache!!.second)
                return@observeForever
            }
            userPosts.value = it
        }
    }

    fun likePost(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        mediaId: String,
        data: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(mInstagramRemote.likePost(header, mediaId, data))
            .observeForever {
                result.value = it
            }
    }

    fun unlikePost(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        mediaId: String,
        data: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(mInstagramRemote.unlikePost(header, mediaId, data))
            .observeForever {
                result.value = it
            }
    }

    fun likeComment(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        mediaId: String,
        data: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(mInstagramRemote.likeComment(header, mediaId, data))
            .observeForever {
                result.value = it
            }
    }

    fun unlikeComment(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: HashMap<String, String>,
        mediaId: String,
        data: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(mInstagramRemote.unlikeComment(header, mediaId, data))
            .observeForever {
                result.value = it
            }
    }

    fun getPostComments(
        result: MutableLiveData<Resource<InstagramCommentResponse>>,
        header: HashMap<String, String>,
        mediaId: String
    ) {
        if (userComments != null && userComments!!.mediaId == mediaId) {
            result.value = Resource.success(userComments)
            return
        }
        NetworkCall<InstagramCommentResponse>().makeCall(
            mInstagramRemote.getPostComments(
                header,
                mediaId
            )
        ).observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                userComments = it.data!!.apply {
                    this.mediaId = mediaId
                }
            }
            result.value = it
        }
    }

    fun loadMoreComments(result: MutableLiveData<Resource<InstagramCommentResponse>>,
                         header: HashMap<String, String>,
                         mediaId: String,
                            minId:String){
        NetworkCall<InstagramCommentResponse>().makeCall(
            mInstagramRemote.loadMorePostComments(
                header,
                mediaId,
                minId
            )
        ).observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                userComments = it.data!!.apply {
                    this.mediaId = mediaId
                }
            }
            result.value = it
        }
    }

    fun getUserInfoFromUsername(
        result: MutableLiveData<Resource<InstagramUserInfo>>,
        header: HashMap<String, String>,
        username: String,
        fromModule: String = "feed_timeline"
    ) {
        NetworkCall<InstagramUserInfo>().makeCall(
            mInstagramRemote.getUsernameInfo(
                header,
                username,
                fromModule
            )
        ).observeForever {
            result.value = it
        }
    }

    fun getTimelinePosts(
        result: MutableLiveData<Resource<InstagramFeedTimeLineResponse>>,
        header: HashMap<String, String>,
        data: RequestBody,
        isRefresh: Boolean = false
    ) {
        if(!isRefresh){
            timelinePosts?.let {
                result.value = Resource.success(it)
                return
            }
        }
        NetworkCall<InstagramFeedTimeLineResponse>().makeCall(
            mInstagramRemote.getFeedTimeline(
                header,
                data
            )
        ).observeForever {
            if(it.status == Resource.Status.SUCCESS){
                timelinePosts = it.data
            }
            result.value = it
        }
    }

    fun loadMoreTimelinePosts(
        result: MutableLiveData<Resource<InstagramFeedTimeLineResponse>>,
        header: HashMap<String, String>,
        data: RequestBody
    ) {
        NetworkCall<InstagramFeedTimeLineResponse>().makeCall(
            mInstagramRemote.getFeedTimeline(
                header,
                data
            )
        ).observeForever {
            result.value = it
        }
    }

    fun getTimelineStory(
        result: MutableLiveData<Resource<InstagramStoriesResponse>>,
        header: HashMap<String, String>,
        data: RequestBody,
        isRefresh: Boolean = false
    ) {
        if (!isRefresh && stories != null) {
            result.value = Resource.success(stories)
            return
        }
        NetworkCall<InstagramStoriesResponse>().makeCall(
            mInstagramRemote.getStoryTimeline(
                header,
                data
            )
        ).observeForever {
            if (it.status == Resource.Status.SUCCESS) {
                stories = it.data
            }
            result.value = it
        }
    }

    fun sendStoryReaction(result: MutableLiveData<Resource<ResponseBody>>, header: HashMap<String, String>, data: RequestBody){
        NetworkCall<ResponseBody>().makeCall(mInstagramRemote.sendStoryReaction(header,data)).observeForever{
            result.value = it
        }
    }
    fun getStoryMedias(
        result: MutableLiveData<Resource<Tray>>,
        header: HashMap<String, String>,
        userId: Long,
        data: RequestBody
    ) {
        if (stories != null) {
            for (item in stories!!.tray) {
                if (item.user.pk == userId && item.items != null && item.items.isNotEmpty()) {
                    result.postValue(Resource.success(item))
                    return
                }
            }
        }
        NetworkCall<InstagramStoryMediaResponse>().makeCall(
            mInstagramRemote.getStoryMedia(
                header,
                data
            )
        )
            .observeForever {
                if (it.status == Resource.Status.SUCCESS) {
                    for (item in stories!!.tray) {
                        if (item.user.pk == userId) {
                            item.items = (it.data!!).reels[userId]!!.items
                            result.value = Resource.success(item)
                            return@observeForever
                        }
                    }
                } else if (it.status == Resource.Status.LOADING) {
                    result.value = Resource.loading()
                }
            }
    }

    fun sendStoryReply(result: MutableLiveData<Resource<ResponseBody>>, header: HashMap<String, String>, data: RequestBody,mediaType: String){
        NetworkCall<ResponseBody>().makeCall(mInstagramRemote.sendStoryReplyMessage(header,data,mediaType)).observeForever {
            result.value = it
        }
    }

    fun shareMedia(result: MutableLiveData<Resource<ResponseBody>>, header: HashMap<String, String>, data: RequestBody,mediaType: String){
        NetworkCall<ResponseBody>().makeCall(mInstagramRemote.shareMedia(header,data,mediaType)).observeForever {
            result.value = it
        }
    }
    fun shareStory(result: MutableLiveData<Resource<ResponseBody>>, header: HashMap<String, String>, data: RequestBody,mediaType: String){
        NetworkCall<ResponseBody>().makeCall(mInstagramRemote.shareStory(header,data,mediaType)).observeForever {
            result.value = it
        }
    }


}
