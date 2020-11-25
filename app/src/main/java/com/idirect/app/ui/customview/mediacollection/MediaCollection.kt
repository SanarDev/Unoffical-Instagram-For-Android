package com.idirect.app.ui.customview.mediacollection

import android.content.Context
import android.widget.FrameLayout
import com.bumptech.glide.RequestManager
import com.idirect.app.datasource.model.CarouselMedia
import com.idirect.app.manager.PlayManager

class MediaCollection constructor(
    context: Context,
    var media: CarouselMedia,
    var mGlide: RequestManager,
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