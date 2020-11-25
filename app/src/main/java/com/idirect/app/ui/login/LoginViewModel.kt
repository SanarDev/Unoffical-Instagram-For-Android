package com.idirect.app.ui.login

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.idirect.app.R
import com.idirect.app.core.BaseViewModel
import com.idirect.app.core.BaseApplication
import com.idirect.app.ui.customview.toast.CustomToast
import com.idirect.app.utils.Resource
import com.idirect.app.usecase.UseCase
import com.idirect.app.ui.main.MainActivity
import com.sanardev.instagramapijava.response.IGLoginResponse
import javax.inject.Inject
import kotlin.reflect.KClass


class LoginViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    val intentEvent = MutableLiveData<Pair<KClass<out AppCompatActivity>, Bundle?>>()

    val result =  MutableLiveData<Resource<IGLoginResponse>>()

    init {
        if (mUseCase.getCurrentUser() != null) {
            intentEvent.value = (Pair(MainActivity::class, null))
        }
    }

    fun login(username:String,password:String) {
        val context = getApplication() as BaseApplication
        if (username.isEmpty()) {
            CustomToast.show(context,context.getString(R.string.enter_username), Toast.LENGTH_LONG)
        }

        if (password.isEmpty()) {
            CustomToast.show(context,context.getString(R.string.enter_password), Toast.LENGTH_LONG)
        }
        result.value = Resource.loading()
        mUseCase.login(username,password)
            .subscribe({
            result.value = Resource.success(it)
        },{
            result.value = Resource.error()
        },{

        })

    }

}