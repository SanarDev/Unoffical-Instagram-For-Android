package com.sanardev.instagrammqtt.ui.login

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.core.BaseApplication
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.helper.Resource
import com.sanardev.instagrammqtt.ui.main.MainActivity
import com.sanardev.instagrammqtt.ui.twofactor.TwoFactorActivity
import com.sanardev.instagrammqtt.usecase.UseCase
import run.tripa.android.extensions.toast
import javax.inject.Inject
import kotlin.reflect.KClass

class LoginViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    val username = ObservableField<String>()
    val password = ObservableField<String>()
    val isLoading = ObservableField<Boolean>(false)
    private var _token = MutableLiveData<String>()
    var result = MutableLiveData<Resource<InstagramLoginResult>>()

    val intentEvent = MutableLiveData<Pair<KClass<out AppCompatActivity>, Bundle?>>()

    init {
        if (mUseCase.isLogged()) {
            intentEvent.postValue(Pair(MainActivity::class, null))
        }
    }

    fun onBtnLoginClick(v: View) {
        val _username = username.get()
        val _password = password.get()

        if (_username.isNullOrBlank()) {
            getApplication<BaseApplication>().toast("enter username")
            return
        }

        if (_password.isNullOrBlank()) {
            getApplication<BaseApplication>().toast("enter password")
            return
        }

        isLoading.set(true)
        mUseCase.instagramLogin(_username,_password,result)
    }

}