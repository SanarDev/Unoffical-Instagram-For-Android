package com.sanardev.instagrammqtt.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.core.BaseApplication
import com.sanardev.instagrammqtt.databinding.ActivityLoginBinding
import com.sanardev.instagrammqtt.helper.Resource
import com.sanardev.instagrammqtt.helper.dialog.DialogHelper
import com.sanardev.instagrammqtt.ui.twofactor.TwoFactorActivity
import run.tripa.android.extensions.toast
import kotlin.reflect.KClass

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
                }
                Resource.Status.ERROR -> {
                    binding.progressbar.visibility = View.INVISIBLE
                    binding.btnLogin.visibility = View.VISIBLE
                    if (it.data!!.error_type == "bad_password") {
                        DialogHelper.createDialog(
                            this,
                            layoutInflater,
                            "Error",
                            "Your password is incorrect",
                            "Try again"
                        )
                        return@Observer
                    }
                    if (it.data.twoFactorRequired) {
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                TwoFactorActivity::class.java
                            ).apply {
                                this.putExtras(Bundle().apply {
                                    putSerializable("two_factor_info", it.data.two_factor_info)
                                })
                            })
                    }
                }
                Resource.Status.SUCCESS -> {
                    binding.progressbar.visibility = View.INVISIBLE
                    binding.btnLogin.visibility = View.VISIBLE
                }
            }
        })
    }

}