package com.idirect.app.customview.mediacollection

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.databinding.LayoutCommentBinding
import com.idirect.app.datasource.model.CarouselMedia
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.manager.PlayManager
import com.idirect.app.ui.posts.PostsFragment

class MediaCollection constructor(
    context: Context,
    var media: CarouselMedia,
    var mGlideRequestManager: RequestManager,
    var mPlayManager: PlayManager
) : FrameLayout(context, null) {
//
//    interface OnItemCollectionClickListener {
//        fun onClick(v: View, media: CarouselMedia)
//    }
//
//    private var mediaSource: MediaSource? = null
//    private val photoView: PhotoView
//    private val videoView: PlayerView
//    private val layoutParent: FrameLayout
//    private val imgPlay: AppCompatImageView
//    var onItemClickListener: OnItemCollectionClickListener? = null
//
//    private val playChangeListener: PlayManager.OnPlayChangeListener
//
//    init {
//
//        playChangeListener = object : PlayManager.OnPlayChangeListener {
//            override fun onStart() {
//                if (media.mediaType == InstagramConstants.MediaType.VIDEO.type) {
//                    videoView.player = mPlayManager.player
//                    videoView.showController()
//                    this@MediaCollection.imgPlay.visibility = View.GONE
//                    this@MediaCollection.photoView.visibility = View.GONE
//                    this@MediaCollection.videoView.visibility = View.VISIBLE
//                }
//            }
//
//            override fun onStop() {
//                if (media.mediaType == InstagramConstants.MediaType.VIDEO.type) {
//                    videoView.player = null
//                    this@MediaCollection.imgPlay.visibility = View.VISIBLE
//                    this@MediaCollection.photoView.visibility = View.VISIBLE
//                    this@MediaCollection.videoView.visibility = View.GONE
//                }
//            }
//        }
//
//
//        addView(itemView)
//    }
//
//    fun play() {
//        stop()
//        onItemClickListener?.onClick(layoutParent, media)
//        mediaSource?.let {
//            mPlayManager.startPlay(mediaSource!!, media.id)
//        }
//    }
//
//    fun stop() {
//        mPlayManager.stopPlay()
//    }
}