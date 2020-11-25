package com.idirect.app.datasource.repository

import android.app.Application
import com.idirect.app.datasource.local.MessageDataSource
import com.idirect.app.extentions.toList
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.model.direct.IGThread
import com.sanardev.instagramapijava.model.login.IGLoggedUser
import com.sanardev.instagramapijava.model.login.IGTwoFactorInfo
import com.sanardev.instagramapijava.model.user.CurrentUserCache
import com.sanardev.instagramapijava.request.IGLogoutRequest
import com.sanardev.instagramapijava.response.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.*


class InstagramRepository(
    private val application: Application,
    private var mMessageDataSource: MessageDataSource
) {

    private var stories: IGTimeLineStoryResponse? = null
    private var timelinePosts: IGTimeLinePostsResponse? = null

    fun getCurrentUser(): CurrentUserCache? {
        return InstaClient.currentUser(application.applicationContext)
    }

    fun getTimelinePosts(): Observable<IGTimeLinePostsResponse> {
        if(timelinePosts != null){
            return Observable.just(timelinePosts)
        }
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .mediaProcessor
            .getTimelinePosts()
            .map {
                timelinePosts = it
                return@map it
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getTimelineStory(): Observable<IGTimeLineStoryResponse> {
        if (stories != null) {
            return Observable.just(stories)
        }
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .storyProcessor
            .getTimelineStory()
            .map {
                stories = it
                return@map it
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getStoryMedia(userId: Long): Observable<IGStoryMediaResponse> {
        stories?.let {
            for(story in it.tray){
                if(story.user.pk == userId && story.items != null){
                    return Observable.just(IGStoryMediaResponse().apply {
                        this.reels[userId] = story
                    })
                }
            }
        }
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .storyProcessor
            .getStoryMedia(userId.toList())
            .map {
                stories?.apply {
                    for(story in this.tray){
                        if(story.user.pk == userId){
                            story.items = it.reels[userId]!!.items
                        }
                    }
                }
                return@map it
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun unlikePost(id: String): Observable<IGUnlikePostResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .mediaProcessor
            .unlikePost(id)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun likePost(id: String): Observable<IGLikePostResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .mediaProcessor
            .likePost(id)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadMorePosts(nextMaxId: String): Observable<IGTimeLinePostsResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .mediaProcessor
            .getTimelinePosts(nextMaxId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun likeComment(id: Long): Observable<ResponseBody> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .commentProcessor
            .unlikeComment(id.toString())
    }

    fun unlikeComment(id: Long): Observable<ResponseBody> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .commentProcessor
            .likeComment(id.toString())
    }

    fun login(username: String, password: String): Observable<IGLoginResponse> {
        return InstaClient(application.applicationContext, username, password)
            .accountProcessor
            .login()
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getRecipient(query: String?): Observable<IGRecipientsResponse> {
        if (query.isNullOrBlank()) {
            return InstaClient.getInstanceCurrentUser(application.applicationContext)
                .directProcessor
                .getRecipient()
                .observeOn(AndroidSchedulers.mainThread())
        } else {
            return InstaClient.getInstanceCurrentUser(application.applicationContext)
                .directProcessor
                .getRecipient(query)
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun getLoggedUser(): IGLoggedUser? {
        return InstaClient.getInstanceCurrentUser(application.applicationContext).loggedUser
    }

    fun shareStory(
        threadId: String,
        mediaId: String,
        mediaType: Int,
        reelId: Long
    ): Observable<IGShareStoryResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .storyProcessor
            .shareStory(threadId, mediaId, mediaType, reelId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun shareMedia(
        threadId: String,
        mediaId: String,
        mediaType: Int
    ): Observable<IGShareMediaResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .mediaProcessor
            .shareMedia(threadId, mediaId, mediaType)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getMediaById(mediaId: String): Observable<IGMediaResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .mediaProcessor
            .getMediaById(mediaId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getDirectPresence(): Observable<IGPresenceResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor
            .directPresence
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun sendMediaVoice(
        threadId: String,
        userPks: List<Long>,
        filePath: String,
        clientContext: String
    ): Observable<IGMediaVoiceResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor.sendMediaVoice(
                threadId,
                userPks,
                filePath,
                clientContext
            )
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun sendMediaImage(
        threadId: String,
        userId: List<Long>,
        filePath: String,
        clientContext: String
    ): Observable<IGDirectActionResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor
            .sendMediaImage(threadId, userId, filePath, clientContext)
            .observeOn(AndroidSchedulers.mainThread())

    }

    fun sendMediaVideo(
        threadId: String,
        userId: List<Long>,
        filePath: String,
        clientContext: String
    ): Observable<IGDirectActionResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor
            .sendMediaVideo(threadId, userId, filePath, clientContext)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun sendLikeReaction(
        itemId: String,
        threadId: String,
        clientContext: String
    ): Observable<IGDirectActionResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor
            .sendReaction(itemId, threadId, clientContext)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun markAsSeenRavenMedia(
        itemId: String,
        threadId: String,
        messageClientContext: String
    ): Observable<ResponseBody> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor.markAsSeenRavenMedia(threadId, itemId, messageClientContext)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun unsendMessage(
        threadId: String,
        itemId: String,
        clientContext: String
    ): Observable<ResponseBody> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor.unsendMessage(threadId, itemId, clientContext)
            .observeOn(AndroidSchedulers.mainThread())
    }


    fun markAsSeenMessage(threadId: String, itemId: String): Observable<IGDirectActionResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor
            .markAsSeenMessage(threadId, itemId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getDirectMoreChats(
        threadId: String,
        seqId: Int,
        cursor: String
    ): Observable<IGDirectChatResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor
            .getDirectMoreChats(threadId, seqId, cursor)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun logout(): Observable<IGLogoutRequest> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .accountProcessor
            .logout()
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getDirectInbox(limit: Int, threadMessageLimit: Int): Observable<IGDirectsResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor
            .getInbox(limit, threadMessageLimit)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getMoreDirectInbox(seqId: Int, cursor: String): Observable<IGDirectsResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor.loadMoreInbox(seqId, cursor)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getThreadByParticipants(userId: Long, seqId: Int): Observable<IGParticipantsResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor
            .getThreadByParticipants(userId, seqId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createFakeThread(userId: Long, threadTitle: String, profileImage: String): IGThread {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .directProcessor
            .createFakeThread(userId, threadTitle, profileImage)
    }

    fun getPostsComments(mediaId: String): Observable<IGCommentsResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .commentProcessor.getPostComments(mediaId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getPostsMoreComments(mediaId: String, nextMaxId: String): Observable<IGCommentsResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .commentProcessor.getPostComments(mediaId, nextMaxId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getPosts(userId: Long): Observable<IGPostsResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .userProcessor.getPosts(userId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getMorePosts(userId: Long, previousPostId: String): Observable<IGPostsResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .userProcessor.getMorePosts(userId, previousPostId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getMe(): Observable<IGUserInfoResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .userProcessor
            .me
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun sendStoryReaction(
        threadId: String,
        mediaId: String,
        reaction: String,
        reelId: Long
    ): Observable<IGSendStoryReactionResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .storyProcessor
            .sendStoryReaction(threadId, mediaId, reaction, reelId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun replyStory(
        threadId: String,
        mediaId: String,
        mediaType: Int,
        text: String,
        reelId: Long
    ): Observable<IGStoryReplyResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .storyProcessor.sendStoryReply(threadId, mediaId, text, reelId, mediaType)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun voteSlider(id: String, sliderId: Long, vote: Float): Observable<IGStoryUpdateResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .storyProcessor.voteSlider(vote, sliderId, id)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun markStoryAsSeen(trayId:Long,id: String, takenAt: Long): Observable<ResponseBody> {
        markStoryAsSeenLocal(trayId, takenAt)
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .storyProcessor.markStoryAsSeen(id, takenAt)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun markStoryAsSeenLocal(trayId:Long, takenAt: Long) {
        stories?.let {
            for(tray in it.tray){
                if(tray.id == trayId){
                    if(tray.seen < takenAt){
                        tray.seen = takenAt
                    }
                    return
                }
            }
        }
    }

    fun sendStoryQuestionResponse(
        id: String,
        questionId: Long,
        response: String
    ): Observable<ResponseBody> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .storyProcessor.storyQuestionResponse(id, questionId, response)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun twoStepAuth(username: String, password: String, code: String): Observable<IGLoginResponse> {
        return InstaClient(application.applicationContext, username, password)
            .accountProcessor.twoStepAuth(code)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getTwoStepAuthInfo(username: String, password: String): IGTwoFactorInfo {
        return InstaClient(application.applicationContext, username, password)
            .accountProcessor.twoStepAuthInfo
    }

    fun getUserInfoByUsername(username: String): Observable<IGUserInfoResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .userProcessor.getUserInfoByUsername(username!!)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUserInfo(userId: Long): Observable<IGUserInfoResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .userProcessor
            .getUserInfo(userId)
            .observeOn(AndroidSchedulers.mainThread())
    }


    fun storyQuizAnswer(mediaId: String, quizId: Long, index: Int): Observable<IGStoryUpdateResponse> {
        return InstaClient.getInstanceCurrentUser(application.applicationContext)
            .storyProcessor
            .storyQuizAnswer(mediaId,quizId,index)
            .observeOn(AndroidSchedulers.mainThread())
    }
}
