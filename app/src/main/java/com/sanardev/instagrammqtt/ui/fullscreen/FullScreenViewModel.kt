package com.sanardev.instagrammqtt.ui.fullscreen

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.sanardev.instagrammqtt.core.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.response.InstagramPost
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class FullScreenViewModel @Inject constructor(application: Application,var mUseCase: UseCase): BaseViewModel(application) {

    val liveDataPost = MediatorLiveData<Resource<InstagramPost>>()

    fun getMediaById(mediaId:String){
        mUseCase.getMediaById(mediaId,liveDataPost)
    }

}