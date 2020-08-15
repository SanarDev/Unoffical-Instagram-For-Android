package com.idirect.app.ui.posts

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseApplication
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentCollectionBinding
import com.idirect.app.datasource.model.CarouselMedia
import com.idirect.app.extensions.drawable
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.manager.PlayManager
import javax.inject.Inject


class FragmentCollection constructor(var media: CarouselMedia) :
    BaseFragment<FragmentCollectionBinding, FragmentCollectionViewModel>() {

    @Inject
    lateinit var mGlideRequestManager: RequestManager

    @Inject
    lateinit var mPlayManager: PlayManager

    private var mediaSource: MediaSource? = null

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        when (media.mediaType) {
            InstagramConstants.MediaType.IMAGE.type -> {
                val image = media.imageVersions2.candidates[0]
                visible(binding.photoView)
                gone(binding.imgPlay,binding.videoView)
                mGlideRequestManager.load(image.url)
                    .placeholder(R.drawable.post_load_place_holder)
                    .into(binding.photoView)
            }
            InstagramConstants.MediaType.VIDEO.type -> {
                binding.videoView.visibility = View.GONE
                val video = media.videoVersions[0]
                val image = media.imageVersions2.candidates[0]
                binding.photoView.apply {
                    isClickable = false
                    isFocusable = false
                    isZoomable = false
                }
                binding.videoView.apply {
                    isClickable = false
                    isFocusable = false
                }
                binding.layoutParent.apply {
                    isClickable = true
                    isFocusable = true
                }
                binding.layoutParent.setOnClickListener {
                    if (mPlayManager.currentPlayerId != media.id) {
                       play()
                    } else {
                        stop()
                    }
                }
                mGlideRequestManager
                    .asBitmap()
                    .load(image.url)
                    .placeholder(R.drawable.post_load_place_holder)
                    .into(binding.photoView)
                    .onLoadFailed(drawable(R.drawable.post_load_place_holder))
                val dataSource =
                    DefaultHttpDataSourceFactory(
                        Util.getUserAgent(
                            requireContext(),
                            "Instagram"
                        )
                    )
                val uri = Uri.parse(video.url)
                mediaSource =
                    ProgressiveMediaSource.Factory(dataSource)
                        .createMediaSource(uri)
            }
        }
    }

    fun play() {
        mediaSource?.let {
            gone(binding.photoView,binding.imgPlay)
            binding.videoView.visibility = View.VISIBLE
            mPlayManager.startPlay(mediaSource!!,media.id)
            binding.videoView.player = mPlayManager.player
            binding.videoView.showController()
        }
    }

    fun stop() {
        visible(binding.imgPlay)
        mPlayManager.stopPlay()
    }

    fun isPlaying(): Boolean {
        return mPlayManager.currentPlayerId == media.id
    }

    override fun getViewModelClass(): Class<FragmentCollectionViewModel> {
        return FragmentCollectionViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_collection
    }

    override fun onStop() {
        super.onStop()
        stop()
    }

    override fun getNameTag(): String {
        return "fragment_collection"
    }
}