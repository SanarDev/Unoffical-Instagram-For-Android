package com.sanardev.instagrammqtt.datasource.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.sanardev.instagrammqtt.datasource.model.MessageMetaData
import com.sanardev.instagrammqtt.datasource.model.payload.StatusResult

class InstagramSendItemResponse : StatusResult() {
    @SerializedName("message_metadata")
    @Expose
    lateinit var messageMetaDatas: List<MessageMetaData>

    @SerializedName("upload_id")
    @Expose
    lateinit var uploadId: String

}