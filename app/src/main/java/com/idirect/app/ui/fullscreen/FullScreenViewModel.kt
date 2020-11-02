package com.idirect.app.ui.fullscreen

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.datasource.model.response.InstagramPost
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.response.IGMediaResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class FullScreenViewModel @Inject constructor(application: Application): BaseViewModel(application) {

    val instaClient = InstaClient.getInstanceCurrentUser(application.applicationContext)
    val liveDataPost = MediatorLiveData<Resource<IGMediaResponse>>()

    fun getMediaById(mediaId:String){
        instaClient.mediaProcessor.getMediaById(mediaId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                liveDataPost.value = Resource.success(it)
            },{},{})
    }

}