package com.sanardev.instagrammqtt.ui.direct

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.response.InstagramInbox
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class DirectViewModel @Inject constructor(application: Application,var mUseCase: UseCase): BaseViewModel(application) {

    val isEnableSendButton = ObservableField<Boolean>(false)

    fun edtMessageChange(s: CharSequence, start: Int, before: Int, count: Int){
        if(s.isBlank()){
            isEnableSendButton.set(false)
        }else{
            isEnableSendButton.set(true)
        }
    }

}