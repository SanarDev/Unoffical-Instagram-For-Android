package com.sanardev.instagrammqtt.datasource.remote.requests

import com.sanardev.instagrammqtt.datasource.remote.InstagramRequest
import okhttp3.Headers
import okhttp3.ResponseBody
import kotlin.reflect.KClass

class TokenRequest : InstagramRequest() {
    override fun getMethod(): String {
        return GET
    }

    override fun getEndPoint(): String {
        return ""
    }

    override fun getHeaders(): Headers? {
        return null
    }

    override fun isSignature(): Boolean {
        return false
    }

    override fun getData(): Any? {
        return null
    }

    override fun getResponseClass(): KClass<*> {
        return ResponseBody::class
    }
}