package com.sanardev.instagrammqtt.repository

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sanardev.instagrammqtt.datasource.local.MessageDataSource
import com.sanardev.instagrammqtt.datasource.model.PresenceResponse
import com.sanardev.instagrammqtt.datasource.model.ResponseDirectAction
import com.sanardev.instagrammqtt.datasource.model.event.MessageResponse
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginTwoFactorPayload
import com.sanardev.instagrammqtt.datasource.model.response.*
import com.sanardev.instagrammqtt.datasource.remote.InstagramRemote
import com.sanardev.instagrammqtt.datasource.remote.NetworkCall
import com.sanardev.instagrammqtt.utils.Resource
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
    private val request = com.sanardev.instagrammqtt.datasource.remote.Request()

    fun login(
        liveData: MutableLiveData<Resource<InstagramLoginResult>>,
        instagramLoginPayload: InstagramLoginPayload,
        headersGenerator: () -> Map<String, String>,
        encrypter: (InstagramLoginPayload) -> RequestBody
    ) {
        NetworkCall<InstagramLoginResult>()
            .makeCall(
                mInstagramRemote.login(
                    headersGenerator.invoke(),
                    encrypter.invoke(instagramLoginPayload)
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
        instagramLoginTwoFactorPayload: InstagramLoginTwoFactorPayload,
        headersGenerator: () -> Map<String, String>,
        encrypter: (InstagramLoginTwoFactorPayload) -> RequestBody
    ) {
            NetworkCall<InstagramLoginResult>()
                .makeCall(
                    mInstagramRemote.twoFactorLogin(
                        headersGenerator(),
                        encrypter(instagramLoginTwoFactorPayload)
                    )
                ).observeForever {
                liveData.postValue(it)
            }
    }

    fun getDirectInbox(
        responseLiveData: MediatorLiveData<Resource<InstagramDirects>>,
        headersGenerator: () -> Map<String, String>,
        limit: Int = 20
    ) {
        responseLiveData.addSource(
            NetworkCall<InstagramDirects>()
                .makeCall(
                    mInstagramRemote.getDirectIndex(
                        headersGenerator.invoke(),
                        limit = limit
                    )
                ), Observer {
                responseLiveData.postValue(it)
            }
        )
    }


    fun loadMoreDirects(
        responseLiveData: MediatorLiveData<Resource<InstagramDirects>>,
        headersGenerator: () -> Map<String, String>,
        seqId: Int,
        cursor: String,
        threadMessageLimit: Int = 10,
        limit: Int = 10
    ) {
        responseLiveData.addSource(NetworkCall<InstagramDirects>().makeCall(
            mInstagramRemote.loadMoreDirects(
                header = headersGenerator.invoke(),
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
        headersGenerator: () -> Map<String, String>
    ) {
        responseLiveData.addSource(NetworkCall<PresenceResponse>().makeCall(
            mInstagramRemote.getDirectPresence(
                headersGenerator.invoke()
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
        function: () -> HashMap<String, String>
    ) {
        responseLiveData.value = Resource.loading(null)
        responseLiveData.addSource(
            NetworkCall<InstagramChats>()
                .makeCall(
                    mInstagramRemote.getChats(
                        function.invoke(),
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
        registerPush: Map<String, *>,
        encrypter: (Map<String, *>) -> okhttp3.RequestBody,
        function: () -> HashMap<String, String>
    ) {
        result.addSource(NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.sendPushRegister(
                function.invoke(),
                encrypter.invoke(registerPush)
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
        result: MediatorLiveData<Resource<InstagramChats>>,
        cursor: String,
        threadId: String,
        seqId: Int,
        headersGenerator: () -> Map<String, String>
    ) {
        result.addSource(NetworkCall<InstagramChats>().makeCall(
            mInstagramRemote.loadMoreChats(
                header = headersGenerator.invoke(),
                cursor = cursor,
                threadId = threadId,
                seqID = seqId
            )
        ),
            Observer {
                result.postValue(it)
            })
    }

    fun searchUser(
        responseLiveData: MediatorLiveData<Resource<ResponseBody>>,
        query: String,
        headersGenerator: () -> Map<String, String>
    ) {
        responseLiveData.addSource(NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.searchUser(headersGenerator.invoke(), query = query)
        ),
            Observer {
                responseLiveData.postValue(it)
            })
    }

    fun getRecipients(
        result: MediatorLiveData<Resource<InstagramRecipients>>,
        query: String? = null,
        headersGenerator: () -> Map<String, String>
    ) {
        result.addSource(NetworkCall<InstagramRecipients>().makeCall(
            if (query == null || query.isEmpty()) mInstagramRemote.getRecipients(
                headersGenerator.invoke()
            ) else mInstagramRemote.searchRecipients(
                headersGenerator.invoke(),
                query = query
            )
        )
            , Observer {
                result.postValue(it)
            })

    }

    fun sendReaction(
        result: MutableLiveData<Resource<ResponseDirectAction>>,
        headersGenerator: () -> Map<String, String>,
        data: Map<*, *>,
        encryptor: (Map<*, *>) -> okhttp3.RequestBody
    ) {
        NetworkCall<ResponseDirectAction>().makeCall(
            mInstagramRemote.sendReaction(
                headersGenerator.invoke(),
                encryptor.invoke(data)
            )
        ).observeForever {
            result.value = it
        }

    }

    fun markAsSeen(
        result: MutableLiveData<Resource<ResponseDirectAction>>,
        headersGenerator: () -> Map<String, String>,
        threadId: String,
        itemId: String,
        data: Map<*, *>,
        encryptor: (Map<*, *>) -> okhttp3.RequestBody
    ) {
        NetworkCall<ResponseDirectAction>().makeCall(
            mInstagramRemote.markAsSeen(
                headersGenerator.invoke(),
                threadId,
                itemId,
                encryptor.invoke(data)
            )
        ).observeForever {
            result.value = it
        }
    }

    fun markAsSeenRavenMedia(
        result: MutableLiveData<Resource<ResponseBody>>,
        headersGenerator: () -> Map<String, String>,
        threadId: String,
        data: Map<*, *>,
        encryptor: (Map<*, *>) -> okhttp3.RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.markAsSeenRavenMedia(
                headersGenerator.invoke(),
                threadId,
                encryptor.invoke(data)
            )
        ).observeForever {
            result.value = it
        }
    }

    fun getMediaUploadUrl(
        result: MutableLiveData<Resource<ResponseBody>>,
        headersGenerator: () -> Map<String, String>,
        uploadName: String
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.getMediaUploadUrl(
                headersGenerator.invoke(),
                uploadName
            )
        ).observeForever {
            result.value = it
        }
    }

    fun getMediaImageUploadUrl(
        result: MutableLiveData<Resource<ResponseBody>>,
        headersGenerator: () -> Map<String, String>,
        uploadName: String
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.getMediaImageUploadUrl(
                headersGenerator.invoke(),
                uploadName
            )
        ).observeForever {
            result.value = it
        }
    }

    fun uploadMedia(
        liveDataUploadMedia: MutableLiveData<Resource<ResponseBody>>,
        uploadName: String,
        header: () -> Map<String, String>,
        mediaRequestBody: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.uploadMedia(
                header.invoke(),
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
        header: () -> Map<String, String>,
        mediaRequestBody: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.uploadMediaImage(
                header.invoke(),
                uploadName,
                mediaRequestBody
            )
        ).observeForever {
            liveDataUploadMedia.value = it
        }
    }

    fun uploadFinish(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: () -> Map<String, String>,
        requestBody: RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.uploadFinish(
                header.invoke(),
                requestBody
            )
        ).observeForever {
            result.value = it
        }
    }

    fun sendMediaVoice(
        result: MutableLiveData<Resource<InstagramSendItemResponse>>,
        header: () -> Map<String, String>,
        requestBody: RequestBody
    ) {
        NetworkCall<InstagramSendItemResponse>().makeCall(
            mInstagramRemote.sendMediaVoice(
                header.invoke(),
                requestBody
            )
        ).observeForever {
            result.value = it
        }
    }

    fun sendMediaVideo(
        result: MutableLiveData<Resource<MessageResponse>>,
        header: () -> Map<String, String>,
        requestBody: RequestBody
    ) {
        NetworkCall<MessageResponse>().makeCall(
            mInstagramRemote.sendMediaVideo(
                header.invoke(),
                requestBody
            )
        ).observeForever {
            result.value = it
        }
    }

    fun sendMediaImage(
        result: MutableLiveData<Resource<MessageResponse>>,
        header: () -> Map<String, String>,
        requestBody: RequestBody
    ) {
        NetworkCall<MessageResponse>().makeCall(
            mInstagramRemote.sendMediaImage(
                header.invoke(),
                requestBody
            )
        ).observeForever {
            result.value = it
        }
    }

    fun getByParticipants(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: () -> Map<String, String>,
        userId: String,
        seqId: Int,
        limit: Int = 20
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.getByParticipants(
                header.invoke(),
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
        header: () -> Map<String, String>,
        data: Map<*, *>,
        encryptor: (Map<*, *>) -> RequestBody
    ) {
        NetworkCall<MessageResponse>().makeCall(
            mInstagramRemote.sendLinkMessage(
                header.invoke(),
                encryptor.invoke(data)
            )
        ).observeForever {
            result.value = it
        }
    }


    fun getMediaById(
        result: MediatorLiveData<Resource<InstagramPost>>,
        header: () -> Map<String, String>,
        mediaId: String
    ) {
        result.addSource(NetworkCall<InstagramPost>().makeCall(
            mInstagramRemote.getMediaById(
                header.invoke(),
                mediaId
            )
        ),
            Observer {
                result.postValue(it)
            })
    }

    fun getUserInfo(
        result: MutableLiveData<Resource<InstagramUserInfo>>,
        header: () -> Map<String, String>,
        userId: Long
    ) {
        NetworkCall<InstagramUserInfo>().makeCall(
            mInstagramRemote.getUserInfo(
                header.invoke(),
                userId
            )
        ).observeForever {
            result.postValue(it)
        }
    }

    fun logout(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: () -> Map<String, String>,
        data: Map<*, *>,
        encryptor: (Map<*, *>) -> RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.logout(
                header.invoke(),
                encryptor.invoke(data)
            )
        ).observeForever {
            result.postValue(it)
        }
    }

    fun unsendMessage(
        result: MutableLiveData<Resource<ResponseBody>>,
        header: () -> Map<String, String>,
        threadId: String,
        itemId: String,
        data: Map<*, *>,
        encryptor: (Map<*, *>) -> RequestBody
    ) {
        NetworkCall<ResponseBody>().makeCall(
            mInstagramRemote.unsendMessage(
                header.invoke(),
                threadId,
                itemId,
                encryptor.invoke(data)
            )
        ).observeForever {
            result.postValue(it)
        }
    }
}
