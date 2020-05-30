package com.sanardev.instagrammqtt.ui.twofactor

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.constants.AppConstants
import com.sanardev.instagrammqtt.databinding.ActivityTwoFactorBinding
import com.sanardev.instagrammqtt.datasource.model.ErrorModel
import com.sanardev.instagrammqtt.datasource.model.response.InstagramTwoFactorInfo
import com.sanardev.instagrammqtt.helper.Resource

class TwoFactorActivity : BaseActivity<ActivityTwoFactorBinding,TwoFactorViewModel>() {
    override fun layoutRes(): Int {
        return R.layout.activity_two_factor
    }

    override fun getViewModelClass(): Class<TwoFactorViewModel> {
        return TwoFactorViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras != null && intent.extras!!.getSerializable("two_factor_info")!= null) {
            viewModel.initData(intent.extras!!.getSerializable("two_factor_info") as InstagramTwoFactorInfo)
        }else{
            finish()
        }

       viewModel.result.observe(this, Observer {
           if(it.status == Resource.Status.ERROR){

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