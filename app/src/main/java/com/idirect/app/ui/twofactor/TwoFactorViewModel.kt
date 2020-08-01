package com.idirect.app.ui.twofactor

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.idirect.app.R
import com.idirect.app.core.BaseViewModel
import com.idirect.app.core.BaseApplication
import com.idirect.app.datasource.model.response.InstagramLoginResult
import com.idirect.app.datasource.model.response.InstagramTwoFactorInfo
import com.idirect.app.utils.Resource
import com.idirect.app.usecase.UseCase
import java.lang.StringBuilder
import javax.inject.Inject

class TwoFactorViewModel @Inject constructor(application: Application, var mUseCase: UseCase) :
    BaseViewModel(application) {

    val isLoading = MutableLiveData<Boolean>(false)
    val result = MutableLiveData<Resource<InstagramLoginResult>>()

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
            .append(textCodeOne.get())
            .append(textCodeTwo.get())
            .append(textCodeThree.get())
            .append(textCodeFour.get())
            .append(textCodeFive.get())
            .append(textCodeSix.get())
            .toString()

        mUseCase.checkTwoFactorCode(instagramTwoFactorInfo, code).observeForever {
            if(it.status == Resource.Status.ERROR){
                textCodeOne.set("")
                textCodeTwo.set("")
                textCodeThree.set("")
                textCodeFour.set("")
                textCodeFive.set("")
                textCodeSix.set("")
            }
            result.value = it
        }
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
    }


}