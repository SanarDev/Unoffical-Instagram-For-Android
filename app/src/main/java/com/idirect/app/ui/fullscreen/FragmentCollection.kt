package com.idirect.app.ui.fullscreen

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseApplication
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentCollectionBinding
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible

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