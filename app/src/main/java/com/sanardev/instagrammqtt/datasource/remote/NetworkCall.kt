package com.sanardev.instagrammqtt.datasource.remote

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.APIErrors
import com.sanardev.instagrammqtt.utils.ErrorUtils
import com.sanardev.instagrammqtt.utils.Resource
import io.netty.handler.codec.mqtt.MqttDecoder
import io.netty.handler.codec.mqtt.MqttPubAckMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class NetworkCall<T>{
    lateinit var call:Call<T>

    fun makeCall(call:Call<T>): MutableLiveData<Resource<T>> {
        this.call = call
        val callBackKt =
            CallBackKt<T>()
        callBackKt.result.value = Resource.loading(null)
        this.call.clone().enqueue(callBackKt)
        return callBackKt.result
    }

    class CallBackKt<T>: Callback<T> {
        var result: MutableLiveData<Resource<T>> = MutableLiveData()

        override fun onFailure(call: Call<T>, t: Throwable) {
            result.value = Resource.error(APIErrors(InstagramConstants.ErrorCode.INTERNET_CONNECTION.code,t.message))
            t.printStackTrace()
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if(response.isSuccessful) {
                result.value =
                    Resource.success(data = response.body(), headers = response.headers())
            }else{
                result.value = Resource.error(
                    ErrorUtils.parseError(response))
            }
        }
    }

    fun cancel(){
        if(::call.isInitialized){
            call.cancel()
        }
    }
}