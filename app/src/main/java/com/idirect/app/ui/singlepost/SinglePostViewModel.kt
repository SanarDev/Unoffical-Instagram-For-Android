package com.idirect.app.ui.singlepost

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.idirect.app.core.BaseViewModel
import com.sanardev.instagramapijava.InstaClient
import com.sanardev.instagramapijava.model.timeline.MediaOrAd
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class SinglePostViewModel @Inject constructor(application: Application):BaseViewModel(application){

    val instaClient  = InstaClient.getInstanceCurrentUser(application.applicationContext)

    val mediaPost =  MutableLiveData<List<MediaOrAd>>()
    fun getMediaPost(mediaId:String){
        instaClient.mediaProcessor.getMediaById(mediaId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mediaPost.value = it.items
            },{
                Log.i("TEST","TEST")
            },{})
    }
}