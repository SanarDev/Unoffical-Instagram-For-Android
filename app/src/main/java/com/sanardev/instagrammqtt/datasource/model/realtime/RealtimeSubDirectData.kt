package com.sanardev.instagrammqtt.datasource.model.realtime

import com.google.gson.annotations.SerializedName

data class RealtimeSubDirectData(@SerializedName("timestamp")
                                 var timestamp:String,
                                 @SerializedName("sender_id")
                                 var senderId:String, var ttl:Int,
                                 @SerializedName("activity_status")
                                 var activityStatus:Int)