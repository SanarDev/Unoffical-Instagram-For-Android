package com.sanardev.instagrammqtt.ui.fullscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.databinding.ActivityFullScreenBinding
import com.squareup.picasso.Picasso
import java.io.File

class FullScreenActivity : BaseActivity<ActivityFullScreenBinding,FullScreenViewModel>(){

    companion object{
        fun open(context:Context,url:String){
            context.startActivity(Intent(context,FullScreenActivity::class.java).apply {
                putExtra("url",url)
            })
        }
        fun openFile(context:Context,filePath:String){
            context.startActivity(Intent(context,FullScreenActivity::class.java).apply {
                putExtra("filePath",filePath)
            })
        }
    }
    override fun layoutRes(): Int {
        return R.layout.activity_full_screen
    }

    override fun getViewModelClass(): Class<FullScreenViewModel> {
        return FullScreenViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.extras!!.getString("url")
        val filePath = intent.extras!!.getString("filePath")
        if(url != null){
            Glide.with(applicationContext).load(url).into(binding.imageView)
        }else{
            Glide.with(applicationContext).load(File(filePath!!)).into(binding.imageView)
        }
    }

}