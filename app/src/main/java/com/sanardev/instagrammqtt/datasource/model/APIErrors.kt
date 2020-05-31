package com.sanardev.instagrammqtt.datasource.model

import okhttp3.ResponseBody

data class APIErrors<T>(var code: Int,var message: String?=null,var data:ResponseBody?=null) {

}