package com.sanardev.instagrammqtt.utils

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.sanardev.instagrammqtt.datasource.model.APIErrors
import com.sanardev.instagrammqtt.datasource.model.payload.StatusResult
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import retrofit2.Response

class ErrorUtils<T> {

    companion object {
        fun <T> parseError(response: Response<T>): APIErrors<T> {
            return APIErrors(response.code(), response.message(), data = response.errorBody())
        }
    }
}