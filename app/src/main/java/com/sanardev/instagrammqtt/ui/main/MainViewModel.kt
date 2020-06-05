package com.sanardev.instagrammqtt.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.response.InstagramInbox
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.Resource
import javax.inject.Inject

class MainViewModel @Inject constructor(application: Application,var mUseCase: UseCase):BaseViewModel(application) {

    fun getUsername(): String {
        val user = mUseCase.getUserData()
        return user!!.username!!
    }

    fun getPassword():String{
        val user = mUseCase.getUserData()
        return user!!.password!!
    }

    private val result = MediatorLiveData<Resource<InstagramInbox>>()
    val liveData = Transformations.map(result){
        if(it.status == Resource.Status.ERROR){
            val gson = Gson()
            val instagramInboxResult =
                gson.fromJson(it.apiError!!.data!!.string(), InstagramInbox::class.java)
            it.data = instagramInboxResult
        }
        Log.i("TEST_APPLICATION","TEST")
        return@map it
    }

    fun resetUserData() {
        mUseCase.resetUserData()
    }

    init {
        mUseCase.getDirectInbox(result)
    }
}