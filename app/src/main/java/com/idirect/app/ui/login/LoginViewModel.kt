package com.idirect.app.ui.login

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.idirect.app.core.BaseViewModel
import com.idirect.app.core.BaseApplication
import com.idirect.app.datasource.model.response.InstagramLoginResult
import com.idirect.app.utils.Resource
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.usecase.UseCase
import com.idirect.app.extentions.toast
import javax.inject.Inject
import kotlin.reflect.KClass


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