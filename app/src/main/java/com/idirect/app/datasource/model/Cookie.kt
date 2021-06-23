package com.idirect.app.datasource.model

data class Cookie (
    var username : String?=null,
    var password : String?=null,
    var phoneID:String,
    var deviceID:String,
    var mid:String?=null,
    var rur:String?=null,
    var guid:String,
    var adid:String,
    var csrftoken:String?=null,
    var sessionID:String
)