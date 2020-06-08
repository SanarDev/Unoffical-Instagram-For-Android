package com.sanardev.instagrammqtt.repository

import android.os.Handler
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginTwoFactorPayload
import com.sanardev.instagrammqtt.datasource.model.response.InstagramChats
import com.sanardev.instagrammqtt.datasource.model.response.InstagramDirects
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.datasource.remote.InstagramRemote
import com.sanardev.instagrammqtt.utils.Resource
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result.response
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


class InstagramRepository(private var mInstagramRemote: InstagramRemote) {


    private val mHandler = Handler()
    fun login(
        liveData: MediatorLiveData<Resource<InstagramLoginResult>>,
        instagramLoginPayload: InstagramLoginPayload,
        headersGenerater: () -> Map<String, String>,
        encrypter: (InstagramLoginPayload) -> RequestBody
    ) {
        liveData.addSource(
            NetworkCall<InstagramLoginResult>().makeCall(
                mInstagramRemote.login(
                    headersGenerater.invoke(),
                    encrypter.invoke(instagramLoginPayload)
                )
            )
        ) {
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
        liveData: MediatorLiveData<Resource<InstagramLoginResult>>,
        instagramLoginTwoFactorPayload: InstagramLoginTwoFactorPayload,
        headersGenerater: () -> Map<String, String>,
        encrypter: (InstagramLoginTwoFactorPayload) -> RequestBody
    ) {
        liveData.addSource(NetworkCall<InstagramLoginResult>().makeCall(
            mInstagramRemote.twoFactorLogin(
                headersGenerater(),
                encrypter(instagramLoginTwoFactorPayload)
            )
        ), Observer {
            liveData.postValue(it)
        })
    }

    fun getDirectInbox(
        responseLiveData: MediatorLiveData<Resource<InstagramDirects>>,
        headersGenerater: () -> Map<String, String>
    ) {
        responseLiveData.addSource(
            NetworkCall<InstagramDirects>().makeCall(
                mInstagramRemote.getDirectIndex(
                    headersGenerater.invoke()
                )
            ), Observer {
                responseLiveData.postValue(it)
            }
        )
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
            NetworkCall<InstagramChats>().makeCall(
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


}
