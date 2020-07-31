package com.sanardev.instagrammqtt.ui.login

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sanardev.instagrammqtt.core.BaseViewModel
import com.sanardev.instagrammqtt.core.BaseApplication
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.utils.Resource
import com.sanardev.instagrammqtt.ui.main.MainActivity
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.extentions.toast
import javax.inject.Inject
import kotlin.reflect.KClass
import com.google.gson.Gson


class LoginViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    val username = ObservableField<String>()
    val password = ObservableField<String>()
    val isLoading = ObservableField<Boolean>(false)
    val intentEvent = MutableLiveData<Pair<KClass<out AppCompatActivity>, Bundle?>>()

    val result =  MutableLiveData<Resource<InstagramLoginResult>>()

    init {
        if (mUseCase.isLogged()) {
            intentEvent.value = (Pair(MainActivity::class, null))
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
        mUseCase.instagramLogin(_username, _password).observeForever {
            result.value = it
        }
    }

}