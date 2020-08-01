package com.idirect.app.datasource.remote.requests

import com.idirect.app.datasource.remote.InstagramRequest
import com.idirect.app.datasource.remote.response.LoginResponse
import okhttp3.Headers
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

class LoginRequest(var username: String, var password: String) : InstagramRequest() {

    var csrftoken: String? = null
    override fun getMethod(): String {
        return "POST"
    }

    override fun getEndPoint(): String {
        return "accounts/login/"
    }

    override fun getHeaders(): Headers? {
        return Headers.of(emptyMap())
    }

    override fun isSignature(): Boolean {
        return true
    }

    override fun getData(): Any {
        return HashMap<String, Any>().apply {
            put("username", username)
            put("password", password)
            put("_csrftoken", csrftoken!!)
            put("phone_id", UUID.randomUUID().toString())
            put("guid", UUID.randomUUID().toString())
            put("adid", UUID.randomUUID().toString())
            put("device_id", UUID.randomUUID().toString())
            put("login_attempt_account", 0)
            put(
                "country_codes",
                "[{\"country_code\":\"1\",\"source\":[\"default\"]},{\"country_code\":\"7\",\"source\":[\"uig_via_phone_id\"]}]"
            )

        }
    }

    override fun getResponseClass(): KClass<*> {
        return LoginResponse::class
    }


}