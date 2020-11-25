package com.idirect.app.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.idirect.app.core.BaseActivity
import com.idirect.app.R
import com.idirect.app.ui.customview.toast.CustomToast
import com.idirect.app.databinding.ActivityLoginBinding
import com.idirect.app.utils.Resource
import com.idirect.app.ui.twofactor.TwoFactorActivity
import com.idirect.app.extentions.hideKeyboard
import com.idirect.app.extentions.toast
import com.idirect.app.ui.main.MainActivity
import com.sanardev.instagramapijava.IGConstants

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    companion object {
        fun open(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }

    override fun layoutRes(): Int {
        return R.layout.activity_login
    }

    override fun getViewModelClass(): Class<LoginViewModel> {
        return LoginViewModel::class.java
    }

    private lateinit var password: String
    private lateinit var username: String
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

                    toast(getString(R.string.error_internet_connection))
                }
                Resource.Status.SUCCESS -> {
                    binding.progressbar.visibility = View.INVISIBLE
                    binding.btnLogin.visibility = View.VISIBLE
                    binding.layoutButton.isEnabled = true

                    if (it.data!!.status == IGConstants.STATUS_SUCCESS) {
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                MainActivity::class.java
                            )
                        )
                    }else{
                        when(it.data!!.errorType){
                            IGConstants.Errors.LOGIN_BAD_PASSWORD ->{
                                CustomToast.show(applicationContext,getString(R.string.bad_password),Toast.LENGTH_SHORT)
                            }
                            IGConstants.Errors.LOGIN_TOO_MANY_TRIED ->{
                                CustomToast.show(applicationContext,getString(R.string.too_many_tried),Toast.LENGTH_SHORT)
                            }
                            IGConstants.Errors.LOGIN_REQUIRE_TWO_STEP_AUTH ->{
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        TwoFactorActivity::class.java
                                    ).apply {
                                        putExtra("username",username)
                                        putExtra("password",password)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        })
        binding.btnLogin.setOnClickListener {
            username = binding.edtUsername.text.toString()
            password = binding.edtPassword.text.toString()
            viewModel.login(username, password)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ActivityCompat.finishAffinity(this)
    }

}