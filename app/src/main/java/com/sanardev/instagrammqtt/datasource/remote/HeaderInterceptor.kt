package com.sanardev.instagrammqtt.datasource.remote

import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.usecase.UseCase

import java.io.IOException
import java.util.UUID

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class HeaderInterceptor( private val userAgent: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }
}
