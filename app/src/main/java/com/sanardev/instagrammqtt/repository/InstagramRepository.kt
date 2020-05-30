package com.sanardev.instagrammqtt.repository

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sanardev.instagrammqtt.datasource.model.Cookie
import com.sanardev.instagrammqtt.datasource.model.payload.StatusResult
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.datasource.remote.InstagramRemote
import com.sanardev.instagrammqtt.helper.Resource
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Reader
import okhttp3.RequestBody




class InstagramRepository(private var mInstagramRemote: InstagramRemote) {


    fun login(
        liveData: MutableLiveData<Resource<InstagramLoginResult>>,
        headers:Map<String,String>,
        payload: String
    ) {
        val body = RequestBody.create(
            okhttp3.MediaType.parse("application/json; charset=utf-8"),
            payload
        )
        liveData.value = Resource(Resource.Status.LOADING)
        mInstagramRemote.login(headers,body)
            .enqueue(object : Callback<InstagramLoginResult> {
                override fun onFailure(call: Call<InstagramLoginResult>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<InstagramLoginResult>,
                    response: Response<InstagramLoginResult>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body().apply {
                            this?.headers = response.headers()
                        }
                        liveData.value = Resource(Resource.Status.SUCCESS,result,null)
                    } else {
                        val gson = Gson()
                        val type = object : TypeToken<InstagramLoginResult>() {}.type
                        val errorResponse: InstagramLoginResult? =
                            gson.fromJson(response.errorBody()!!.string(), type)
                        liveData.value = Resource(Resource.Status.ERROR,errorResponse,null)
                    }
                }
            })
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
        generateHeaders: Map<String, String>,
        payload: String
    ) {

        mInstagramRemote.twoFactorLogin(generateHeaders,getBodyFromPayload(payload))
            .enqueue(object :Callback<InstagramLoginResult>{
                override fun onFailure(call: Call<InstagramLoginResult>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<InstagramLoginResult>,
                    response: Response<InstagramLoginResult>
                ) {

                }
            })
    }

    fun getBodyFromPayload(payload: String):RequestBody{
        return RequestBody.create(
            okhttp3.MediaType.parse("application/json; charset=utf-8"),
            payload
        )
    }
}