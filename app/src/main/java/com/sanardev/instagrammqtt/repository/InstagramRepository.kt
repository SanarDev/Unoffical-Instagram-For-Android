package com.sanardev.instagrammqtt.repository

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginTwoFactorPayload
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.datasource.remote.InstagramRemote
import com.sanardev.instagrammqtt.utils.Resource
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.RequestBody


class InstagramRepository(private var mInstagramRemote: InstagramRemote) {


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


}
