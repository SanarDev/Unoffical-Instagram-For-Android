package com.sanardev.instagrammqtt.datasource.model.response

import com.sanardev.instagrammqtt.datasource.model.payload.StatusResult
import okhttp3.ResponseBody

class InstagramInbox : StatusResult() {
    var responseBody: ResponseBody? = null
}