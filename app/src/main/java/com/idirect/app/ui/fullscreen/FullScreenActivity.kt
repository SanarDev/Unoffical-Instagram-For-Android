package com.idirect.app.ui.fullscreen

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseActivity
import com.idirect.app.core.BaseApplication
import com.idirect.app.databinding.ActivityFullScreenBinding
import com.idirect.app.extensions.visible
import com.idirect.app.utils.Resource
import java.io.File

class FullScreenActivity : BaseActivity<ActivityFullScreenBinding, FullScreenViewModel>() {

    companion object {
        private const val TYPE_URL = 1
        private const val TYPE_FILE = 2
        private const val TYPE_POST = 3

        fun openUrl(context: Context, url: String) {
            context.startActivity(Intent(context, FullScreenActivity::class.java).apply {
                putExtra("type", TYPE_URL)
                putExtra("url", url)
            })
        }

        fun openFile(context: Context, filePath: String) {
            context.startActivity(Intent(context, FullScreenActivity::class.java).apply {
                putExtra("type", TYPE_FILE)
                putExtra("filePath", filePath)
            })
        }

        fun openPost(context: Context, mediaId: String) {
            context.startActivity(Intent(context, FullScreenActivity::class.java).apply {
                putExtra("type", TYPE_POST)
                putExtra("media_id", mediaId)
            })
        }
    }

    override fun layoutRes(): Int {
        return R.layout.activity_full_screen
    }

    override fun getViewModelClass(): Class<FullScreenViewModel> {
        return FullScreenViewModel::class.java
    }

    val fragments = ArrayList<FragmentCollection>().toMutableList()
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState)

        val type = intent.extras!!.getInt("type")
        when (type) {
            TYPE_URL -> {
                val url = intent.extras!!.getString("url")
                Glide.with(applicationContext).load(url).into(binding.imageView)
                visible(binding.imageView)
            }
            TYPE_FILE -> {
                val filePath = intent.extras!!.getString("filePath")
                Glide.with(applicationContext).load(File(filePath!!)).into(binding.imageView)
                visible(binding.imageView)
            }
            TYPE_POST -> {
                val mediaId = intent.extras!!.getString("media_id")!!
                viewModel.getMediaById(mediaId)
            }
        }

        viewModel.liveDataPost.observe(this, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.ERROR -> {

                }
                Resource.Status.SUCCESS -> {
                    visible(binding.viewPager)
                    val items = it.data!!.items[0].carouselMedias
                    for(item in items){
                        val fragment = if (item.videoVersions != null) {
                            FragmentCollection(InstagramConstants.MediaType.VIDEO, item.videoVersions[0].url)
                        } else {
                            FragmentCollection(InstagramConstants.MediaType.IMAGE, item.imageVersions2.candidates[0].url)
                        }
                        fragments.add(fragment)
                    }
                    binding.viewPager.adapter =
                        CollectionPagerAdapter(fragments, supportFragmentManager)

                    binding.viewPager.addOnPageChangeListener(object:ViewPager.OnPageChangeListener{
                        override fun onPageScrollStateChanged(state: Int) {
                        }

                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int
                        ) {
                            if(fragments[position].itemType == InstagramConstants.MediaType.VIDEO){
                                stopAllSounds()
                                fragments[position].play()
                            }
                        }

                        override fun onPageSelected(position: Int) {
                        }
                    })
                }
            }
        })
    }


    private fun stopAllSounds(){
        val mAudioManager =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener {
                        //Handle Focus Change
                    }.build()
            )
        } else {
            mAudioManager.requestAudioFocus(
                { focusChange: Int -> },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }
    inner class CollectionPagerAdapter(
        var items: List<Fragment>,
        fragmentManager: FragmentManager
    ) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        // Returns total number of pages
        override fun getCount(): Int {
            return items.size
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment {
            val item = items[position]
            return item
        }
    }

    override fun onStop() {
        super.onStop()
        BaseApplication.stopPlay()
    }

}