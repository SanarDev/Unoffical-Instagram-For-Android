package com.sanardev.instagrammqtt.ui.main

import android.app.Application
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.usecase.UseCase
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
}