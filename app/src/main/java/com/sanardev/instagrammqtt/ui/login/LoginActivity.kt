package com.sanardev.instagrammqtt.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.databinding.ActivityLoginBinding
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoginResult
import com.sanardev.instagrammqtt.ui.main.MainActivity
import com.sanardev.instagrammqtt.utils.Resource
import com.sanardev.instagrammqtt.utils.dialog.DialogHelper
import com.sanardev.instagrammqtt.ui.twofactor.TwoFactorActivity
import run.tripa.android.extensions.hideKeyboard

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override fun layoutRes(): Int {
        return R.layout.activity_login
    }

    override fun getViewModelClass(): Class<LoginViewModel> {
        return LoginViewModel::class.java
    }

    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.intentEvent.observe(this, Observer {
            startActivity(Intent(this@LoginActivity, it.first.java))
            finish()
        })

        viewModel.result.observe(this, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    binding.progressbar.visibility = View.VISIBLE
                    binding.btnLogin.visibility = View.INVISIBLE
                    binding.layoutButton.isEnabled = false
                    this@LoginActivity.hideKeyboard()
                }
                Resource.Status.ERROR -> {
                    binding.progressbar.visibility = View.INVISIBLE
                    binding.btnLogin.visibility = View.VISIBLE
                    binding.layoutButton.isEnabled = true

                    when(it.data?.error_type){
                        InstagramConstants.Error.BAD_PASSWORD.msg ->{
                            DialogHelper.createDialog(
                                this@LoginActivity,
                                layoutInflater,
                                getString(R.string.error),
                                getString(R.string.incorrect_password),
                                getString(R.string.try_again),
                                positiveFun = {

                                }
                            )
                            return@Observer
                        }
                        InstagramConstants.Error.RATE_LIMIT.msg ->{
                            DialogHelper.createDialog(
                                this@LoginActivity,
                                layoutInflater,
                                getString(R.string.error),
                                it.data!!.message!!,
                                getString(R.string.Ok),
                                positiveFun = {

                                }
                            )
                            return@Observer
                        }
                    }

                    if (it.data?.twoFactorRequired != null && it.data?.twoFactorRequired!!) {
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                TwoFactorActivity::class.java
                            ).putExtras(Bundle().apply {
                                putParcelable("two_factor_info", it.data!!.two_factor_info)
                            })
                        )
                    }
                }
                Resource.Status.SUCCESS -> {
                    binding.progressbar.visibility = View.INVISIBLE
                    binding.btnLogin.visibility = View.VISIBLE
                    binding.layoutButton.isEnabled = true

                    if (it.status == Resource.Status.SUCCESS && it.data?.status == "ok") {
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                MainActivity::class.java
                            )
                        )
                    }
                }
            }
        })
    }

}