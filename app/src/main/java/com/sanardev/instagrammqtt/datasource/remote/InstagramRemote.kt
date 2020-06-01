package com.sanardev.instagrammqtt.datasource.remote

import com.google.gson.annotations.SerializedName
import com.sanardev.instagrammqtt.datasource.model.payload.InstagramLoginPayload
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface InstagramRemote {
/*
@Field("username")
              username :String,
              @Field("phone_id")
              phone_id :String,
              @Field("_csrftoken")
              _csrftoken :String,
              @Field("guid")
              guid :String,
              @Field("adid")
              adid :String,
              @Field("device_id")
              device_id :String,
              @Field("password")
              password :String,
              @Field("country_codes")
              country_codes :String = "country_codes=[{\"country_code\":\"1\",\"source\":[\"default\",\"sim\"]}]",
              @Field("login_attempt_account")
              login_attempt_account :Int = 0
 */
    @POST("accounts/login/")
    fun login(@HeaderMap header:Map<String,String>, @Body payload: RequestBody): Call<InstagramLoginResult>

    @GET("/")
    fun getToken():Call<ResponseBody>

    @POST("accounts/two_factor_login/")
    fun twoFactorLogin(@HeaderMap header:Map<String,String>,@Body payload: RequestBody):Call<InstagramLoginResult>


}