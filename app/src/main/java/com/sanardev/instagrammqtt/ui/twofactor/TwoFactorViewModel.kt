package com.sanardev.instagrammqtt.ui.twofactor

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseViewModel
import com.sanardev.instagrammqtt.core.BaseApplication
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.datasource.model.response.InstagramTwoFactorInfo
import com.sanardev.instagrammqtt.utils.Resource
import com.sanardev.instagrammqtt.usecase.UseCase
import java.lang.StringBuilder
import javax.inject.Inject

class TwoFactorViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    val isLoading = MutableLiveData<Boolean>(false)

    private val _result = MediatorLiveData<Resource<InstagramLoginResult>>()
    val result = Transformations.map(_result) {
        if (it.status == Resource.Status.SUCCESS && it.data?.status == "ok") {
            mUseCase.saveUserData(it.data?.loggedInUser,it.headers)
        }

        if (it.apiError?.data == null)
            return@map it
        if(it.status == Resource.Status.ERROR) {
            val gson = Gson()
            val instagramLoginResult =
                gson.fromJson(it.apiError.data!!.string(), InstagramLoginResult::class.java)
            it.data = instagramLoginResult
        }

        return@map it
    }

    val isEnableResendButton = ObservableField<Boolean>(false)
    val endOfPhoneNumber = ObservableField<String>("")
    val textResendButton = ObservableField<String>()

    val edtCodeOneRequestFocus = ObservableField<Boolean>(false)
    val edtCodeTwoRequestFocus = ObservableField<Boolean>(false)
    val edtCodeThreeRequestFocus = ObservableField<Boolean>(false)
    val edtCodeFourRequestFocus = ObservableField<Boolean>(false)
    val edtCodeFiveRequestFocus = ObservableField<Boolean>(false)
    val edtCodeSixRequestFocus = ObservableField<Boolean>(false)

    val textCodeOne = ObservableField<String>()
    val textCodeTwo = ObservableField<String>()
    val textCodeFour = ObservableField<String>()
    val textCodeFive = ObservableField<String>()
    val textCodeSix = ObservableField<String>()
    val textCodeThree = ObservableField<String>()

    var timer: Int = 0
    lateinit var instagramTwoFactorInfo: InstagramTwoFactorInfo

    fun initData(instagramTwoFactorInfo: InstagramTwoFactorInfo) {
        this@TwoFactorViewModel.instagramTwoFactorInfo = instagramTwoFactorInfo
        endOfPhoneNumber.set(instagramTwoFactorInfo.obfuscatedPhoneNumber)
        if (instagramTwoFactorInfo.phoneVerificationSettings != null) {
            timer = instagramTwoFactorInfo.phoneVerificationSettings!!.resendSmsDelaySec
            startTimer()
        }
    }

    private fun startTimer() {
        isEnableResendButton.set(false)
        Thread {
            while (true) {
                Thread.sleep(1000)
                timer--
                val h: Int = timer / 3600
                val min: Int = (timer - h * 3600) / 60
                val s: Int = timer - (h * 3600 + min * 60)
                var strH = if (h < 10) {
                    "0$h"
                } else {
                    h.toString()
                }
                val strMin = if (min < 10) {
                    "0$min"
                } else {
                    min.toString()
                }
                val strS = if (s < 10) {
                    "0$s"
                } else {
                    s.toString()
                }
                textResendButton.set(
                    String.format(
                        getApplication<BaseApplication>().getString(
                            R.string.time_to_resend_code
                        ),
                        strMin,
                        strS
                    )
                )
                if (timer <= 0) {
                    isEnableResendButton.set(true)
                    return@Thread
                }
            }
        }.start()
    }

    fun onResendCodeClick(v: View) {

    }

    fun onVerifyButtonClick(v: View? = null) {
        val code = StringBuilder()
            .append(textCodeOne)
            .append(textCodeTwo)
            .append(textCodeThree)
            .append(textCodeFour)
            .append(textCodeFive)
            .append(textCodeSix)
            .toString()
        mUseCase.checkTwoFactorCode(_result, instagramTwoFactorInfo, code)
    }

    fun edtOneTextChange(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isBlank())
            return
        edtCodeTwoRequestFocus.set(false)
        edtCodeTwoRequestFocus.set(true)
    }

    fun edtTwoTextChange(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isBlank())
            return
        edtCodeThreeRequestFocus.set(false)
        edtCodeThreeRequestFocus.set(true)
    }

    fun edtThreeTextChange(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isBlank())
            return
        edtCodeFourRequestFocus.set(false)
        edtCodeFourRequestFocus.set(true)
    }

    fun edtFourTextChange(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isBlank())
            return
        edtCodeFiveRequestFocus.set(false)
        edtCodeFiveRequestFocus.set(true)
    }

    fun edtFiveTextChange(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isBlank())
            return
        edtCodeSixRequestFocus.set(false)
        edtCodeSixRequestFocus.set(true)
    }

    fun edtSixTextChange(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isBlank())
            return
        onVerifyButtonClick()
    }


}