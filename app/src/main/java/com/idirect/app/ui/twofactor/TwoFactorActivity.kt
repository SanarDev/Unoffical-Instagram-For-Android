package com.idirect.app.ui.twofactor

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.idirect.app.core.BaseActivity
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.databinding.ActivityTwoFactorBinding
import com.idirect.app.datasource.model.response.InstagramTwoFactorInfo
import com.idirect.app.extentions.toast
import com.idirect.app.ui.inbox.FragmentInbox
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.utils.Resource
import com.idirect.app.utils.dialog.DialogHelper
import com.idirect.app.utils.dialog.DialogListener
import com.sanardev.instagramapijava.IGConstants

class TwoFactorActivity : BaseActivity<ActivityTwoFactorBinding, TwoFactorViewModel>() {
    override fun layoutRes(): Int {
        return R.layout.activity_two_factor
    }

    override fun getViewModelClass(): Class<TwoFactorViewModel> {
        return TwoFactorViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras!!
        viewModel.initData(bundle.getString("username")!!, bundle.getString("password")!!)

        viewModel.result.observe(this, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    binding.btnVerify.visibility = View.INVISIBLE
                    binding.progressbar.visibility = View.VISIBLE
                }
                Resource.Status.ERROR -> {
                    binding.btnVerify.visibility = View.VISIBLE
                    binding.progressbar.visibility = View.INVISIBLE
                    toast(getString(R.string.error_internet_connection))
                }
                Resource.Status.SUCCESS -> {
                    if (it.data!!.status == IGConstants.STATUS_FAIL) {
                        if (it.data?.errorType == IGConstants.Errors.INVALID_TWO_FACTOR_CODE) {
                            DialogHelper.createDialog(
                                this@TwoFactorActivity,
                                layoutInflater,
                                getString(R.string.error),
                                getString(R.string.invalid_two_factor_code),
                                positiveText = getString(R.string.try_again),
                                positiveListener = object : DialogListener.Positive {
                                    override fun onPositiveClick() {
                                        finish()
                                    }
                                }
                            )
                            return@Observer
                        } else if (it.data?.errorType == IGConstants.Errors.LOGIN_INVALID_SMS_CODE) {
                            DialogHelper.createDialog(
                                this@TwoFactorActivity,
                                layoutInflater,
                                getString(R.string.error),
                                getString(R.string.invalid_code_validation),
                                positiveText = getString(R.string.try_again)
                            )
                            return@Observer
                        }
                    }else {
                        startActivity(
                            Intent(
                                this@TwoFactorActivity,
                                MainActivity::class.java
                            )
                        )
                        finish()
                    }
                }
            }
        })
        viewModel.isLoading.observe(this, Observer {
            if (it) {
                binding.btnVerify.visibility = View.INVISIBLE
                binding.progressbar.visibility = View.VISIBLE
            } else {
                binding.btnVerify.visibility = View.VISIBLE
                binding.progressbar.visibility = View.INVISIBLE
            }
        })
    }
}