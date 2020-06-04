package com.sanardev.instagrammqtt.ui.main

import android.content.Intent
import android.os.Bundle
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.databinding.ActivityMainBinding


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(){
    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnTest.setOnClickListener {
            connect("")
        }
    }

    @Throws(Exception::class)
    fun connect(protogle: String) {
        startService(Intent("com.sanardev.instagrammqtt.ACTION_CONNECT").setPackage("com.sanardev.instagrammqtt"));
    }

}