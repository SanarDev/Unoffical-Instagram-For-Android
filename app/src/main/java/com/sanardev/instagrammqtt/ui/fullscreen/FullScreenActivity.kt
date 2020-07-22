package com.sanardev.instagrammqtt.ui.fullscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.core.BaseActivity
import com.sanardev.instagrammqtt.databinding.ActivityFullScreenBinding
import com.sanardev.instagrammqtt.extensions.visible
import java.io.File

class FullScreenActivity : BaseActivity<ActivityFullScreenBinding, FullScreenViewModel>(){

    companion object{
        private const val TYPE_URL = 1
        private const val TYPE_FILE = 2
        private const val TYPE_LIST_URL = 3

        fun openUrl(context:Context, url:String){
            context.startActivity(Intent(context,FullScreenActivity::class.java).apply {
                putExtra("type", TYPE_URL)
                putExtra("url",url)
            })
        }
        fun openFile(context:Context,filePath:String){
            context.startActivity(Intent(context,FullScreenActivity::class.java).apply {
                putExtra("type", TYPE_FILE)
                putExtra("filePath",filePath)
            })
        }
        fun openUrls(context:Context,urls:ArrayList<String>){
            context.startActivity(Intent(context,FullScreenActivity::class.java).apply {
                putExtra("type", TYPE_LIST_URL)
                putStringArrayListExtra("url_list",urls)
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

        val type = intent.extras!!.getInt("type")
        when (type){
            TYPE_URL ->{
                val url = intent.extras!!.getString("url")
                Glide.with(applicationContext).load(url).into(binding.imageView)
                visible(binding.imageView)
            }
            TYPE_FILE -> {
                val filePath = intent.extras!!.getString("filePath")
                Glide.with(applicationContext).load(File(filePath!!)).into(binding.imageView)
                visible(binding.imageView)
            }
            TYPE_LIST_URL -> {
                val urls = intent.extras!!.getStringArrayList("url_list")
                visible(binding.viewPager)
                binding.viewPager.adapter = CollectionPagerAdapter(urls!!.toList(),supportFragmentManager)
            }
        }
    }


    inner class CollectionPagerAdapter(
        var items: List<String>,
        fragmentManager: FragmentManager
    ) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        // Returns total number of pages
        override fun getCount(): Int {
            return items.size
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment {
            val fragment = FragmentCollection(items[position])
            return fragment
        }
    }

}