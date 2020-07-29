package com.sanardev.instagrammqtt.ui.fullscreen

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.core.BaseApplication
import com.sanardev.instagrammqtt.core.BaseFragment
import com.sanardev.instagrammqtt.databinding.FragmentCollectionBinding
import com.sanardev.instagrammqtt.extensions.gone
import com.sanardev.instagrammqtt.extensions.visible

class FragmentCollection constructor(var itemType:InstagramConstants.MediaType,var url:String): BaseFragment<FragmentCollectionBinding,FragmentCollectionViewModel>() {
    private var mediaSource: MediaSource?=null

    override fun getViewModelClass(): Class<FragmentCollectionViewModel> {
        return FragmentCollectionViewModel::class.java
    }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_collection
    }

    override fun onResume() {
        super.onResume()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(itemType){
            InstagramConstants.MediaType.IMAGE ->{
                visible(binding.photoView)
                gone(binding.videoView)
                Glide.with(context!!).load(url).into(binding.photoView)
            }
            InstagramConstants.MediaType.VIDEO ->{
                visible(binding.videoView)
                gone(binding.photoView)
                val dataSource = DefaultHttpDataSourceFactory(Util.getUserAgent(context!!, "Instagram"))
                val uri = Uri.parse(url)
                mediaSource =
                    ProgressiveMediaSource.Factory(dataSource)
                        .createMediaSource(uri)
            }
        }
    }

    fun play(){
        BaseApplication.startPlay(mediaSource!!)
        binding.videoView.player = BaseApplication.player
    }
}