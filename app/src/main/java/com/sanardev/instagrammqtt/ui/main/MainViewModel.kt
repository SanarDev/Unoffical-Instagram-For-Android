package com.sanardev.instagrammqtt.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.core.BaseApplication
import com.sanardev.instagrammqtt.datasource.model.response.InstagramDirects
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.Resource
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    fun getUsername(): String {
        val user = mUseCase.getUserData()
        return user!!.username!!
    }

    fun getPassword(): String {
        val user = mUseCase.getUserData()
        return user!!.password!!
    }

    private val result = MediatorLiveData<Resource<InstagramDirects>>()
    val liveData = Transformations.map(result) {
        if (it.status == Resource.Status.ERROR) {
            if (it.apiError?.data != null) {
                val gson = Gson()
                val instagramInboxResult =
                    gson.fromJson(it.apiError!!.data!!.string(), InstagramDirects::class.java)
                it.data = instagramInboxResult
            }
        }
        Log.i("TEST_APPLICATION", "TEST")
        return@map it
    }

    fun resetUserData() {
        mUseCase.resetUserData()
    }

    fun getDirects() {
        mUseCase.getDirectInbox(result)
    }

    fun convertTimeStampToData(lastActivityAt: Long): String {
       return mUseCase.getDifferentTimeString(lastActivityAt / 1000,false)
    }

    fun getUser(): InstagramLoggedUser {
        return mUseCase.getUserData()!!
    }

    init {
        getDirects()
    }
}