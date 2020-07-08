package com.sanardev.instagrammqtt.ui.startmessage

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.response.InstagramRecipients
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject

class StartMessageViewModel @Inject constructor(application: Application,var mUseCase: UseCase): BaseViewModel(application) {


    private val result = MediatorLiveData<Resource<InstagramRecipients>>()
    val liveData = Transformations.map(result){
        return@map it
    }
    init {
        mUseCase.getRecipients(result)
    }


    fun onSearch(s: CharSequence, start: Int, before: Int, count: Int){
        if(s.isBlank()){
            mUseCase.getRecipients(result)
        }else{
            mUseCase.getRecipients(result,s.toString())
        }
    }
}