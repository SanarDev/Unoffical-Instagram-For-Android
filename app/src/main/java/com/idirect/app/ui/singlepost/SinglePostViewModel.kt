package com.idirect.app.ui.singlepost

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.usecase.UseCase
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.model.timeline.MediaOrAd
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class SinglePostViewModel @Inject constructor(application: Application,val mUseCase: UseCase):BaseViewModel(application){

    val mediaPost =  MutableLiveData<List<MediaOrAd>>()
    fun getMediaPost(mediaId:String){
        mUseCase.getMediaById(mediaId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mediaPost.value = it.items
            },{
                Log.i("TEST","TEST")
            },{})
    }
}