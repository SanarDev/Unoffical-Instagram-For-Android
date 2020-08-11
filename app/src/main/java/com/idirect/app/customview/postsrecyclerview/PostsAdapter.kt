package com.idirect.app.customview.postsrecyclerview

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.customview.customtextview.HyperTextView
import com.idirect.app.databinding.LayoutCarouselImageBinding
import com.idirect.app.databinding.LayoutCarouselVideoBinding
import com.idirect.app.databinding.LayoutUserDetailPostBinding
import com.idirect.app.datasource.model.CarouselMedia
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.di.module.GlideApp
import com.idirect.app.extensions.color
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.extentions.dpToPx
import com.idirect.app.manager.PlayManager
import com.idirect.app.ui.posts.PostsFragmentDirections
import com.idirect.app.utils.DisplayUtils
import com.tylersuehr.chips.CircleImageView

class PostsAdapter(
    var context: Context,
    var mHyperTextClick: HyperTextView.OnHyperTextClick,
    var mPlayManager: PlayManager,
    viewLifecycleOwner:LifecycleOwner,
    var items: MutableList<UserPost>
) : BaseAdapter() {

    private val dataSource: DataSource.Factory = DefaultHttpDataSourceFactory(Util.getUserAgent(context, "Instagram"))
    private val mGlide = GlideApp.with(context)
    var currentMediaPosition: Int = PlayManager.NONE
    private var displayWidth = DisplayUtils.getScreenWidth()
    private var displayHeight = DisplayUtils.getScreenHeight()

    val adapters = HashMap<Int,CollectionMediaAdapter>()

    init {
        mPlayManager.playChangeLiveData.observe(viewLifecycleOwner, Observer {
            if (!it.isPlay) {
                for (index in items.indices) {
                    val item = items[index]
                    if (item.mediaType == InstagramConstants.MediaType.VIDEO.type && item.id == it.currentPlayId) {
                        item.videoVersions[0].isPlay = false
                        item.videoVersions[0].playPosition =
                            mPlayManager.player.currentPosition
                        notifyItemChanged(index)
                        return@Observer
                    }
                    if (item.mediaType == InstagramConstants.MediaType.CAROUSEL_MEDIA.type) {
                        for (indexCarousel in item.carouselMedias.indices) {
                            val carousel = item.carouselMedias[indexCarousel]
                            if (carousel.id == it.currentPlayId) {
                                carousel.videoVersions[0].isPlay = false
                                carousel.videoVersions[0].playPosition = mPlayManager.player.currentPosition
                                adapters[index]?.notifyItemChanged(indexCarousel)
                                return@Observer
                            }
                        }
                    }
                }
            } else if (it.previousPlayId != null) {
                for (index in items.indices) {
                    val item = items[index]
                    if (item.mediaType == InstagramConstants.MediaType.VIDEO.type && it.previousPlayId == item.id) {
                        item.videoVersions[0].isPlay = false
                        item.videoVersions[0].playPosition = 0
                        notifyItemChanged(index)
                        return@Observer
                    } else if (item.mediaType == InstagramConstants.MediaType.CAROUSEL_MEDIA.type) {
                        for (indexCarousel in item.carouselMedias.indices) {
                            val carousel = item.carouselMedias[indexCarousel]
                            if (carousel.id == it.previousPlayId) {
                                carousel.videoVersions[0].isPlay = false
                                carousel.videoVersions[0].playPosition = 0
                                adapters[index]?.notifyItemChanged(indexCarousel)
                                return@Observer
                            }
                        }
                    }
                }
            }
        })
    }
    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)

        adapters.remove(holder.adapterPosition)
    }

    override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
        Log.i(InstagramConstants.DEBUG_TAG, "create layout for position $position")
        val item = items[position]
        val dataBinding = holder.binding as LayoutUserDetailPostBinding
        dataBinding.layoutLikersProfile.removeAllViews()
        val showLikersClickListener = View.OnClickListener {

        }
        val showCommentClickListener = View.OnClickListener {
            val action = PostsFragmentDirections.actionPostsFragmentToCommentsFragment(item)
            it.findNavController().navigate(action)
        }

        if (item.location == null) {
            dataBinding.txtLocationName.visibility = View.GONE
        } else {
            dataBinding.txtLocationName.text = item.location.shortName
            dataBinding.txtLocationName.visibility = View.VISIBLE
        }
        if (item.facepileTopLikers != null && item.facepileTopLikers.isNotEmpty()) {
            visible(dataBinding.layoutLikes)
            gone(dataBinding.txtLikesCount)
            dataBinding.btnTopLikerUsername.text = item.facepileTopLikers[0].username
            dataBinding.btnTopLikerUsername.setOnClickListener {
                item.facepileTopLikers?.let {
                    item.facepileTopLikers[0].pk
                }
            }
            dataBinding.btnOthers.text =
                String.format(context.getString(R.string.others_liker_count), item.likeCount)
            dataBinding.btnOthers.setOnClickListener(showLikersClickListener)
            for (liker in item.facepileTopLikers) {
                val likerProfile = CircleImageView(context)
                likerProfile.layoutParams = android.widget.LinearLayout.LayoutParams(
                    context.resources.dpToPx(20f),
                    context.resources.dpToPx(20f)
                )
                mGlide.load(liker.profilePicUrl).into(likerProfile)
                dataBinding.layoutLikersProfile.addView(likerProfile)
            }
        } else {
            visible(dataBinding.txtLikesCount)
            gone(dataBinding.layoutLikes)
            dataBinding.txtLikesCount.text =
                String.format(context.getString(R.string.liker_count), item.likeCount)
            dataBinding.txtLikesCount.setOnClickListener(showLikersClickListener)
        }
        dataBinding.txtUsername.text = item.user.username
        if (item.caption != null) {
            dataBinding.txtCaption.setText(
                item.caption.user.username,
                item.caption.user.pk,
                item.caption.text
            )
            dataBinding.txtCaption.mHyperTextClick = mHyperTextClick
        }
        mGlide.load(item.user.profilePicUrl).into(dataBinding.imgProfile)
        dataBinding.layoutMedia.removeAllViews()
        when (item.mediaType) {
            InstagramConstants.MediaType.IMAGE.type -> {
                val photoView = AppCompatImageView(context)
                photoView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                photoView.adjustViewBounds = true
                mGlide
                    .load(item.imageVersions2.candidates[0].url)
                    .placeholder(R.drawable.post_load_place_holder)
                    .into(photoView)
                dataBinding.layoutMedia.addView(photoView)
            }
            InstagramConstants.MediaType.VIDEO.type -> {
                val video = item.videoVersions[0]!!
                val previewImage = item.imageVersions2.candidates[0]!!
                val videoLayout: View =
                    LayoutInflater.from(context).inflate(R.layout.layout_post_video, null, false)
                val videoView: PlayerView = videoLayout.findViewById(R.id.video_view)
                val layoutParent: FrameLayout = videoLayout.findViewById(R.id.layout_parent)
                val photoView: AppCompatImageView =
                    videoLayout.findViewById(R.id.photo_view)
                val imgPlay: AppCompatImageView = videoLayout.findViewById(R.id.img_play)
                val size = getStandardVideoSize(video.width, video.height)
                layoutParent.layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size[1])
                if (video.isPlay) {
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSource)
                            .createMediaSource(Uri.parse(video.url))
                    videoView.player = mPlayManager.player
                    photoView.visibility = View.GONE
                    imgPlay.visibility = View.GONE
                    mPlayManager.startPlay(mediaSource, item.id)
                    videoView.showController()
                    currentMediaPosition = position
                } else {
                    if (mPlayManager.currentPlayerId == item.id) {
                        mPlayManager.stopPlay()
                    }
                    videoView.player = null
                    photoView.visibility = View.VISIBLE
                    imgPlay.visibility = View.VISIBLE

                    mGlide.load(previewImage.url).centerCrop().into(photoView)
                }
                dataBinding.layoutMedia.setOnClickListener {
                    video.isPlay = true
                    notifyItemChanged(position)
                }
                videoView.setOnClickListener {
                    if (mPlayManager.currentPlayerId == video.id) {
                        if (mPlayManager.isSoundEnable()) {
                            mPlayManager.disableSound()
                        } else {
                            mPlayManager.enableSound()
                        }
                    }
                }
                dataBinding.layoutMedia.addView(videoLayout)
            }
            InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                val itemView =
                    LayoutInflater.from(context)
                        .inflate(R.layout.layout_post_carousel_media, null, false)
                val pagePosition =
                    itemView.findViewById<AppCompatTextView>(R.id.txt_page_position)
                val recyclerview = itemView.findViewById<RecyclerView>(R.id.view_pager)
                val carouselMedias = item.carouselMedias
                pagePosition.text =
                    String.format(context.getString(R.string.page_position), 1, carouselMedias.size)
                recyclerview.layoutParams.apply {
                    width = displayWidth
                    height = displayWidth
                }
                val snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(recyclerview)
                adapters.put(position, CollectionMediaAdapter(position, carouselMedias))
                recyclerview.adapter = adapters[position]
                recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int
                    ) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            val visibleItem =
                                (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            pagePosition.text = String.format(
                                context.getString(R.string.page_position),
                                visibleItem + 1,
                                carouselMedias.size
                            )
                            if (carouselMedias[visibleItem].mediaType == InstagramConstants.MediaType.VIDEO.type) {
                                carouselMedias[visibleItem].videoVersions[0].isPlay = true
                                recyclerView.adapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                })
                dataBinding.layoutMedia.addView(itemView)
            }
        }
        if (!item.isCommentThreadingEnabled) {
            dataBinding.btnComment.visibility = View.GONE
        } else {
            dataBinding.btnComment.visibility = View.VISIBLE
        }
        dataBinding.layoutComment.removeAllViews()
        if (item.previewComments != null && item.previewComments.isNotEmpty()) {
            visible(dataBinding.btnViewComments)
            dataBinding.btnViewComments.text =
                String.format(context.getString(R.string.view_all_comments), item.commentCount)
            dataBinding.btnViewComments.setOnClickListener(showCommentClickListener)
            for (comment in item.previewComments) {
                val layoutComment =
                    if (comment.type == InstagramConstants.CommentType.NORMAL.type) {
                        LayoutInflater.from(context).inflate(
                            R.layout.layout_preview_comment,
                            null,
                            false
                        )
                    } else {
                        LayoutInflater.from(context).inflate(
                            R.layout.layout_reply_preview_comment,
                            null,
                            false
                        )
                    }
                val txtComment: HyperTextView = layoutComment.findViewById(R.id.txt_comment)
                val imgLike: AppCompatImageView = layoutComment.findViewById(R.id.img_like)

                txtComment.setText(comment.user.username, comment.user.pk, comment.text)
                txtComment.mHyperTextClick = mHyperTextClick

                if (comment.hasLikedComment) {
                    imgLike.setImageResource(R.drawable.instagram_heart_filled_24)
                    imgLike.setColorFilter(context.resources.color(R.color.red))
                } else {
                    imgLike.setImageResource(R.drawable.instagram_heart_outline_24)
                    imgLike.setColorFilter(context.resources.color(R.color.text_light))
                }
                imgLike.setOnClickListener {
                    if (!comment.hasLikedComment) {
                        imgLike.setImageResource(R.drawable.instagram_heart_filled_24)
                        imgLike.setColorFilter(context.resources.color(R.color.red))
//                        viewModel.likeComment(comment.pk)
                    } else {
                        imgLike.setImageResource(R.drawable.instagram_heart_outline_24)
                        imgLike.setColorFilter(context.resources.color(R.color.text_light))
//                        viewModel.unlikeComment(comment.pk)
                    }
                    comment.hasLikedComment = !comment.hasLikedComment
                }
                dataBinding.layoutComment.addView(layoutComment)
            }
        } else {
            gone(dataBinding.btnViewComments)
        }
        if (item.isHasLiked) {
            dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
            dataBinding.btnLike.setColorFilter(context.resources.color(R.color.red))
        } else {
            dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
            dataBinding.btnLike.setColorFilter(context.resources.color(R.color.white))
        }
        dataBinding.btnComment.setOnClickListener(showCommentClickListener)
        dataBinding.btnLike.setOnClickListener {
            if (item.isHasLiked) {
                dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
                dataBinding.btnLike.setColorFilter(context.resources.color(R.color.white))
                item.isHasLiked = false
//                viewModel.unlikePost(item.id)
            } else {
                dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
                dataBinding.btnLike.setColorFilter(context.resources.color(R.color.red))
                item.isHasLiked = true
//                viewModel.likePost(item.id)
            }
        }
        return item
    }

    fun getStandardVideoSize(width: Int, height: Int): Array<Int> {
        var standardHeight = (height * displayWidth) / width
        if (standardHeight > width && standardHeight > displayHeight / 3) {
            standardHeight = (displayHeight.toFloat() / 1.7f).toInt()
        }
        return arrayOf(displayWidth, standardHeight)
    }

    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.layout_user_detail_post
    }

    override fun getItemCount(): Int {
        return items.size
    }


    inner class CollectionMediaAdapter constructor(var positionOfAdapter:Int,var items: List<CarouselMedia>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            if (item.mediaType == InstagramConstants.MediaType.IMAGE.type) {
                val dataBinding = holder.binding as LayoutCarouselImageBinding
                mGlide.load(item.imageVersions2.candidates[0].url)
                    .into(dataBinding.imgPhoto)
            } else {
                val dataBinding = holder.binding as LayoutCarouselVideoBinding
                dataBinding.videoView.visibility = View.GONE
                val video = item.videoVersions[0]
                val image = item.imageVersions2.candidates[0]
                mGlide
                    .asBitmap()
                    .load(image.url)
                    .placeholder(R.drawable.post_load_place_holder)
                    .into(dataBinding.photoView)
                val dataSource =
                    DefaultHttpDataSourceFactory(
                        Util.getUserAgent(
                            context!!,
                            "Instagram"
                        )
                    )
                dataBinding.videoView.setOnClickListener {
                    if (mPlayManager.isSoundEnable()) {
                        mPlayManager.disableSound()
                    } else {
                        mPlayManager.enableSound()
                    }
                }

                mGlide.load(item.imageVersions2.candidates[0].url)
                    .into(dataBinding.photoView)

                if (item.videoVersions[0].isPlay) {
                    val uri = Uri.parse(video.url)
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSource)
                            .createMediaSource(uri)
                    dataBinding.photoView.visibility = View.GONE
                    dataBinding.imgPlay.visibility = View.GONE
                    dataBinding.videoView.visibility = View.VISIBLE
                    dataBinding.videoView.player = mPlayManager.player
                    mPlayManager.startPlay(mediaSource, item.id)
                    dataBinding.videoView.showController()
                    currentMediaPosition = positionOfAdapter
                } else {
                    if (mPlayManager.currentPlayerId == item.id) {
                        mPlayManager.stopPlay()
                    }
                    dataBinding.videoView.player = null
                    dataBinding.videoView.visibility = View.GONE
                    dataBinding.photoView.visibility = View.VISIBLE
                    dataBinding.imgPlay.visibility = View.VISIBLE
                }

                dataBinding.root.setOnClickListener {
                    item.videoVersions[0].isPlay = true
                    this@CollectionMediaAdapter.notifyItemChanged(position)
                }
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            val item = items[position]
            if (item.mediaType == InstagramConstants.MediaType.IMAGE.type) {
                return R.layout.layout_carousel_image
            } else {
                return R.layout.layout_carousel_video
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }
}