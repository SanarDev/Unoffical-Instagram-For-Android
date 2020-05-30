package com.sanardev.instagrammqtt.helper

data class Resource<out T>(val status: Status, val data: T?=null, val message: String?=null) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }
    }

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}