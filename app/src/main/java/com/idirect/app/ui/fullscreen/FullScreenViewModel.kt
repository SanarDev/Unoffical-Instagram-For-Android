package com.idirect.app.ui.fullscreen

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.response.IGMediaResponse
import javax.inject.Inject

class FullScreenViewModel @Inject constructor(application: Application,val mUseCase: UseCase): BaseViewModel(application) {

    val liveDataPost = MediatorLiveData<Resource<IGMediaResponse>>()

    fun getMediaById(mediaId:String){
        mUseCase.getMediaById(mediaId)
            .subscribe({
                liveDataPost.value = Resource.success(it)
            },{},{})
    }

}