package com.sanardev.instagrammqtt.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.hovans.android.global.GlobalAppHolder
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.databinding.ActivityMainBinding
import com.sanardev.instagrammqtt.mqtt.service.NettyIntent
import com.sanardev.instagrammqtt.ui.login.LoginActivity
import com.sanardev.instagrammqtt.utils.Resource


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(){
    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalAppHolder.get().init(application)
        connect("")


        viewModel.liveData.observe(this, Observer {
            if(it.status == Resource.Status.ERROR){
                if(it.data!!.message == InstagramConstants.Error.LOGIN_REQUIRED.msg){
                    viewModel.resetUserData()
                    LoginActivity.open(this)
                    finish()
                }
            }
        })

    }

    @Throws(Exception::class)
    fun connect(protogle: String) {
        startService(Intent(NettyIntent.ACTION_CONNECT_SESSION).setPackage("com.sanardev.instagrammqtt"));
    }

}