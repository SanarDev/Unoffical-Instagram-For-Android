package com.idirect.app.datasource.remote

import com.google.gson.Gson
import com.idirect.app.utils.InstagramHashUtils
import okhttp3.Headers
import okhttp3.RequestBody
import kotlin.reflect.KClass

open abstract class InstagramRequest {

    companion object {
        const val POST = "POST"
        const val GET = "GET"
    }

    abstract fun getMethod(): String
    abstract fun getEndPoint(): String
    abstract fun getHeaders(): Headers?
    abstract fun isSignature(): Boolean
    abstract fun getData(): Any?

    abstract fun getResponseClass(): KClass<*>
    fun getPayload(): RequestBody {

        val payload = Gson().toJson(getData())
        if (isSignature()) {
            val signatureBody = InstagramHashUtils.generateSignature(payload)
            return RequestBody.create(
                okhttp3.MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
                signatureBody
            )
        } else {
            return RequestBody.create(
                okhttp3.MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"),
                payload
            )
        }
    }

}