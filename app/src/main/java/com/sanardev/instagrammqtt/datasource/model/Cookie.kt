package com.sanardev.instagrammqtt.datasource.model

data class Cookie (
    var username : String,
    var password : String,
    var phoneID:String,
    var deviceID:String,
    var mid:String?=null,
    var rur:String?=null,
    var guid:String,
    var adid:String,
    var csrftoken:String,
    var sessionID:String
){
}