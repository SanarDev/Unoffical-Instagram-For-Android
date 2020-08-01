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
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.utils.Resource
import com.idirect.app.utils.dialog.DialogHelper
import com.idirect.app.utils.dialog.DialogListener

class TwoFactorActivity : BaseActivity<ActivityTwoFactorBinding, TwoFactorViewModel>() {
    override fun layoutRes(): Int {
        return R.layout.activity_two_factor
    }

    override fun getViewModelClass(): Class<TwoFactorViewModel> {
        return TwoFactorViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras != null && intent.extras!!.getParcelable<InstagramTwoFactorInfo>("two_factor_info")!= null) {
            viewModel.initData(intent.extras!!.getParcelable("two_factor_info")!!)
        }else{
            finish()
        }

       viewModel.result.observe(this, Observer {
           when(it.status){
               Resource.Status.LOADING ->{
                    binding.btnVerify.visibility = View.INVISIBLE
                    binding.progressbar.visibility = View.VISIBLE
               }
               Resource.Status.ERROR ->{
                   binding.btnVerify.visibility = View.VISIBLE
                   binding.progressbar.visibility = View.INVISIBLE

                   if(it.data?.errorType == InstagramConstants.Error.INVALID_TWO_FACTOR_CODE.msg){
                       DialogHelper.createDialog(
                           this@TwoFactorActivity,
                           layoutInflater,
                           getString(R.string.error),
                           getString(R.string.invalid_two_factor_code),
                           positiveText = getString(R.string.try_again),
                           positiveListener = object :DialogListener.Positive{
                               override fun onPositiveClick() {
                                   finish()
                               }
                           }
                       )
                   }else if(it.data?.errorType == InstagramConstants.Error.INVALID_CODE_VALIDATION.msg){
                       DialogHelper.createDialog(
                           this@TwoFactorActivity,
                           layoutInflater,
                           getString(R.string.error),
                           getString(R.string.invalid_code_validation),
                           positiveText = getString(R.string.try_again)
                       )
                   }
               }
               Resource.Status.SUCCESS ->{
                   startActivity(
                       Intent(
                           this@TwoFactorActivity,
                           MainActivity::class.java
                       )
                   )
                   finish()
               }
           }
       })
        viewModel.isLoading.observe(this, Observer {
            if(it){
                binding.btnVerify.visibility = View.INVISIBLE
                binding.progressbar.visibility = View.VISIBLE
            }else{
                binding.btnVerify.visibility = View.VISIBLE
                binding.progressbar.visibility = View.INVISIBLE
            }
        })
    }
}