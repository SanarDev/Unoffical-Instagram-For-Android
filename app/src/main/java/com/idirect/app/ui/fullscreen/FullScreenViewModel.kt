package com.idirect.app.ui.fullscreen

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramPost
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import javax.inject.Inject

class FullScreenViewModel @Inject constructor(application: Application,var mUseCase: UseCase): BaseViewModel(application) {

    val liveDataPost = MediatorLiveData<Resource<InstagramPost>>()

    fun getMediaById(mediaId:String){
        mUseCase.getMediaById(mediaId,liveDataPost)
    }

}