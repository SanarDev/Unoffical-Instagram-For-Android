package com.idirect.app.utils

import com.idirect.app.datasource.model.APIErrors
import okhttp3.Headers

class Resource<T> private constructor(var status: Resource.Status, var data: T?, var headers: Headers?=null, val apiError:APIErrors<T>?) {
    enum class Status {
        SUCCESS, ERROR, LOADING
    }
    companion object {
        fun <T> success(data: T?=null,headers: Headers?=null): Resource<T> {
            return Resource(Status.SUCCESS, data, headers,null)
        }
        fun <T> error(apiError: APIErrors<T>?=null): Resource<T> {
            return Resource(Status.ERROR, null,null, apiError)
        }
        fun <T> loading(data: T?=null): Resource<T> {
            return Resource(Status.LOADING, data, null,null)
        }
    }
}