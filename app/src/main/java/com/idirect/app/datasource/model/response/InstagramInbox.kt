package com.idirect.app.datasource.model.response

import com.idirect.app.datasource.model.payload.StatusResult
import okhttp3.ResponseBody

class InstagramInbox : StatusResult() {
    var responseBody: ResponseBody? = null
}