package com.sanardev.instagrammqtt.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.core.BaseActivity
import com.sanardev.instagrammqtt.databinding.ActivitySettingBinding
import com.sanardev.instagrammqtt.service.fbns.FbnsService
import com.sanardev.instagrammqtt.utils.dialog.DialogHelper
import com.sanardev.instagrammqtt.utils.dialog.DialogListener

class SettingActivity : BaseActivity<ActivitySettingBinding,SettingViewModel>() {

    companion object{
        fun open(context: Context){
            context.startActivity(Intent(context,SettingActivity::class.java))
        }
    }
    override fun layoutRes(): Int {
        return R.layout.activity_setting
    }

    override fun getViewModelClass(): Class<SettingViewModel> {
        return SettingViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnLogout.setOnClickListener {
            DialogHelper.createDialog(this,layoutInflater,getString(R.string.logout),
            getString(R.string.logout_message),
            getString(R.string.logout),
                object :DialogListener.Positive{
                    override fun onPositiveClick() {
                        viewModel.logout()
                    }
                },
            getString(R.string.cancel))
        }

        viewModel.intentEvent.observe(this, Observer {
            FbnsService.stop(this)
            startActivity(Intent(this, it.first.java))
            ActivityCompat.finishAffinity(this)
        })
    }
}