package com.idirect.app.datasource.remote

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.utils.Resource
import okhttp3.Headers
import okhttp3.OkHttpClient
import java.lang.Exception


open class Request {

    private val httpClient = OkHttpClient()
    private val gson = Gson()
    private val mHandler = Handler()

    fun sendRequest(
        instagramRequest: InstagramRequest,
        responseListener: MutableLiveData<Resource<Any>>
    ) {
        if (instagramRequest.getMethod() == "POST") {
            post(instagramRequest, responseListener)
        }
        if (instagramRequest.getMethod() == "GET") {
            get(instagramRequest, responseListener)
        }
    }

    private fun post(
        instagramRequest: InstagramRequest,
        responseListener: MutableLiveData<Resource<Any>>
    ) {
        Thread {
            val request = okhttp3.Request.Builder()
                .url(InstagramConstants.API_URL + instagramRequest.getEndPoint())
                .post(instagramRequest.getPayload())
            if (instagramRequest.getHeaders() != null) {
                request.headers(instagramRequest.getHeaders()!!)
            }
            responseListener.value = Resource.loading()
            try {
                httpClient.newCall(request.build()).execute().use { response ->
                    // Get response headers
                    val responseHeaders: Headers = response.headers()
                    val responseObject = gson.fromJson(
                        response.body()!!.string(),
                        instagramRequest.getResponseClass().java
                    )
                    responseListener.value = Resource.success(responseObject)
                }
            } catch (e: Exception) {
                responseListener.value = Resource.error()
            }
        }.start()
    }

    private fun get(
        instagramRequest: InstagramRequest,
        responseListener: MutableLiveData<Resource<Any>>
    ) {
        Thread {
            mHandler.post {
                responseListener.value = Resource.loading()
            }
            val request = okhttp3.Request.Builder()
                .url(InstagramConstants.API_URL + instagramRequest.getEndPoint())
                .get()
            try {
                httpClient.newCall(request.build()).execute().use { response ->
                    // Get response headers
                    val responseHeaders: Headers = response.headers()
                    val responseObject = gson.fromJson(
                        response.body()!!.string(),
                        instagramRequest.getResponseClass().java
                    )
                    mHandler.post {
                        responseListener.value = Resource.success(responseObject)
                    }
                }
            } catch (e: Exception) {
                mHandler.post {
                    responseListener.value = Resource.error()
                }
            }
        }.start()
    }
//
//    private fun getCsrftoken(param: MutableLiveData<Resource<String>>) {
//        Thread {
//            try {
//                val request = okhttp3.Request.Builder()
//                    .url(InstagramConstants.BASE_API_URL)
//                    .get()
//                httpClient.newCall(request.build()).execute().use { response ->
//                    // Get response headers
//                    if (response.isSuccessful) {
//                        param.onResponse(
//                            CookieUtils.findCookie(
//                                headers = response.headers(),
//                                s = "csrftoken"
//                            )!!
//                        )
//                    }
//                }
//            }catch (e:Exception){
//                param.onError(InstagramError(-1,"Internet connecton error",31))
//            }
//        }.start()
//    }
}