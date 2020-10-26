package com.idirect.app.customview.postsrecyclerview

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.customview.customtextview.HyperTextView
import com.idirect.app.datasource.model.CarouselMedia
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.datasource.model.event.LoadingEvent
import com.idirect.app.extensions.color
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.extentions.color
import com.idirect.app.extentions.dpToPx
import com.idirect.app.manager.PlayManager
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.TextUtil
import com.tylersuehr.chips.CircleImageView

class PostsAdapter2(
    val context: Context,
    val mHyperTextClick: HyperTextView.OnHyperTextClick,
    val mPlayManager: PlayManager,
    val mGlide: RequestManager,
    viewLifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        const val LOADGING = 100
    }

    private val dataSource: DataSource.Factory =
        DefaultHttpDataSourceFactory(Util.getUserAgent(context, "Instagram"))
    var currentMediaPosition: Int = PlayManager.NONE
    private var displayWidth = DisplayUtils.getScreenWidth()
    private var displayHeight = DisplayUtils.getScreenHeight()
    private var adapters = HashMap<Int, CollectionMediaAdapter>()
    var mListener: PostsRecyclerListener? = null
    private var videoView: PlayerView

    var items: MutableList<Any> = ArrayList<Any>().toMutableList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        videoView = LayoutInflater.from(context)
            .inflate(R.layout.layout_video_player, null, false) as PlayerView
        videoView.player = mPlayManager.player

        mPlayManager.playChangeLiveData.observe(viewLifecycleOwner, Observer {
            if (!it.isPlay) {
                for (index in items.indices) {
                    val item = items[index]
                    if (item is UserPost && item.mediaType == InstagramConstants.MediaType.VIDEO.type && item.id == it.currentPlayId) {
                        item.videoVersions[0].isPlay = false
                        item.videoVersions[0].playPosition =
                            mPlayManager.player.currentPosition
                        notifyItemChanged(index)
                        return@Observer
                    }
                    if (item is UserPost && item.mediaType == InstagramConstants.MediaType.CAROUSEL_MEDIA.type) {
                        for (indexCarousel in item.carouselMedias.indices) {
                            val carousel = item.carouselMedias[indexCarousel]
                            if (carousel.id == it.currentPlayId) {
                                carousel.videoVersions[0].isPlay = false
                                carousel.videoVersions[0].playPosition =
                                    mPlayManager.player.currentPosition
                                adapters[index]?.notifyItemChanged(indexCarousel)
                                return@Observer
                            }
                        }
                    }
                }
            } else if (it.previousPlayId != null) {
                for (index in items.indices) {
                    val item = items[index]
                    if (item is UserPost && item.mediaType == InstagramConstants.MediaType.VIDEO.type && it.previousPlayId == item.id) {
                        item.videoVersions[0].isPlay = false
                        item.videoVersions[0].playPosition = 0
                        notifyItemChanged(index)
                        return@Observer
                    } else if (item is UserPost && item.mediaType == InstagramConstants.MediaType.CAROUSEL_MEDIA.type) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cardView = CardView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            setCardBackgroundColor(context.color(R.color.theme_item))
            radius = context.resources.dpToPx(10f).toFloat()
            useCompatPadding = true
        }

        return when (viewType) {
            LOADGING -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_loading, parent, false)
                LoadingViewHolder(itemView as ViewGroup)
            }
            InstagramConstants.MediaType.IMAGE.type -> {
                PostImageHolder(cardView)
            }
            InstagramConstants.MediaType.VIDEO.type -> {
                PostVideoHolder(cardView)
            }
            InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                PostCarouselMediaHolder(cardView)
            }
            else -> {
                PostImageHolder(cardView)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        if (item is LoadingEvent) {
            return LOADGING
        }
        item as UserPost
        return item.mediaType
    }


    fun setLoading(isLoading: Boolean, recyclerView: RecyclerView? = null) {
        if (isLoading) {
            items.add(LoadingEvent())
            notifyItemInserted(items.size - 1)
            recyclerView?.scrollToPosition(items.size - 1)
        } else {
            for (i in items.indices) {
                if (items[i] is LoadingEvent) {
                    items.removeAt(i)
                    notifyItemRemoved(i)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (item is LoadingEvent) {
            return
        }
        item as UserPost
        val txtLocationName: AppCompatTextView
        val layoutLikersProfile: LinearLayout
        val layoutLikes: LinearLayout
        val layoutComment: LinearLayout
        val txtLikesCount: HyperTextView
        val btnViewComments: AppCompatTextView
        val btnTopLikerUsername: AppCompatTextView
        val btnOthers: AppCompatTextView
        val txtUsername: HyperTextView
        val txtCaption: AppCompatTextView
        val btnComment: AppCompatImageView
        val btnLike: AppCompatImageView
        val btnShare: AppCompatImageView
        val imgProfile: CircleImageView

        when (item.mediaType) {
            InstagramConstants.MediaType.IMAGE.type -> {
                val mHolder = holder as PostImageHolder

                txtLocationName = mHolder.txtLocation
                layoutLikersProfile = mHolder.layoutLikersProfile
//                layoutLikes = mHolder.layoutLikes
                txtLikesCount = mHolder.txtLikeCount
//                btnTopLikerUsername = mHolder.btnTopLikerUsername
//                btnOthers = mHolder.btnOthers
                txtUsername = mHolder.txtUsername
                txtCaption = mHolder.txtCaption
                imgProfile = mHolder.imgProfile
                btnComment = mHolder.btnComment
                layoutComment = mHolder.layoutComment
                btnViewComments = mHolder.btnViewComment
                btnShare = mHolder.btnShare
                btnLike = mHolder.btnLike

                val candidate = item.imageVersions2.candidates[0]
                loadImage(
                    candidate.width,
                    candidate.height,
                    candidate.url,
                    mHolder.imgPhoto,
                    InstagramConstants.MediaType.IMAGE.type
                )
            }
            InstagramConstants.MediaType.VIDEO.type -> {
                val mHolder = holder as PostVideoHolder

                txtLocationName = mHolder.txtLocation
                layoutLikersProfile = mHolder.layoutLikersProfile
//                layoutLikes = mHolder.layoutLikes
                txtLikesCount = mHolder.txtLikeCount
//                btnTopLikerUsername = mHolder.btnTopLikerUsername
//                btnOthers = mHolder.btnOthers
                txtUsername = mHolder.txtUsername
                txtCaption = mHolder.txtCaption
                imgProfile = mHolder.imgProfile
                btnShare = mHolder.btnShare
                btnComment = mHolder.btnComment
                layoutComment = mHolder.layoutComment
                btnViewComments = mHolder.btnViewComment
                btnLike = mHolder.btnLike

                val video = item.videoVersions[0]!!
                val previewImage = item.imageVersions2.candidates[0]!!
                val size = getStandardVideoSize(video.width, video.height)
                mHolder.frameLayoutMedia.layoutParams.apply {
                    width = FrameLayout.LayoutParams.MATCH_PARENT
                    height = size[1]
                }

                if (previewImage.height == video.height) {
                    mHolder.photoView.scaleType = ImageView.ScaleType.FIT_CENTER
                } else {
                    mHolder.photoView.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                loadImage(
                    previewImage.width,
                    previewImage.height,
                    previewImage.url,
                    mHolder.photoView,
                    InstagramConstants.MediaType.VIDEO.type
                )
                /* comment icon start*/
                if (!item.isCommentThreadingEnabled) {
                    mHolder.btnComment.visibility = View.GONE
                } else {
                    mHolder.btnComment.visibility = View.VISIBLE
                }
                /* comment icon end*/

                if (video.isPlay) {
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSource)
                            .createMediaSource(Uri.parse(video.url))
                    mHolder.photoView.visibility = View.GONE
                    mHolder.imgPlay.visibility = View.GONE
                    removeVideoView(videoView)
                    mHolder.mediaContainer.addView(videoView)
                    mPlayManager.startPlay(mediaSource, item.id)
                    currentMediaPosition = position
                    mGlide.clear(mHolder.photoView)
                } else {
                    if (mPlayManager.currentPlayerId == item.id) {
                        mPlayManager.stopPlay()
                    }
                    mHolder.mediaContainer.removeAllViews()
                    mHolder.photoView.visibility = View.VISIBLE
                    mHolder.imgPlay.visibility = View.VISIBLE
                }

                mHolder.frameLayoutMedia.setOnClickListener {
                    video.isPlay = true
                    notifyItemChanged(position)
                }

            }
            InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                val mHolder = holder as PostCarouselMediaHolder

                txtLocationName = mHolder.txtLocation
                layoutLikersProfile = mHolder.layoutLikersProfile
//                layoutLikes = mHolder.layoutLikes
                txtLikesCount = mHolder.txtLikeCount
//                btnTopLikerUsername = mHolder.btnTopLikerUsername
//                btnOthers = mHolder.btnOthers
                txtUsername = mHolder.txtUsername
                txtCaption = mHolder.txtCaption
                imgProfile = mHolder.imgProfile
                btnComment = mHolder.btnComment
                btnShare = mHolder.btnShare
                layoutComment = mHolder.layoutComment
                btnViewComments = mHolder.btnViewComment
                btnLike = mHolder.btnLike

                mHolder.txtPagePosition.text =
                    String.format(
                        context.getString(R.string.page_position),
                        1,
                        item.carouselMedias.size
                    )
                var maxWidth = 0
                var maxHeight = 0
                for (media in item.carouselMedias) {
                    if (media.originalWidth > maxWidth) {
                        maxWidth = media.originalWidth
                    }
                    if (media.originalHeight > maxHeight) {
                        maxHeight = media.originalHeight
                    }
                }
                val size = getStandardVideoSize(maxWidth, maxHeight)
                mHolder.recyclerviewMedia.layoutParams.apply {
                    width = displayWidth
                    height = size[1]
                }

                adapters.put(position, CollectionMediaAdapter(position, item.carouselMedias))
                mHolder.recyclerviewMedia.adapter = adapters[position]

                val mediasSize = item.carouselMedias.size
                mHolder.recyclerviewMedia.addOnScrollListener(object :
                    RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int
                    ) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            val visibleItem =
                                (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                            if (visibleItem >= mediasSize) {
                                return
                            }
                            mHolder.txtPagePosition.text = String.format(
                                context.getString(R.string.page_position),
                                visibleItem + 1,
                                mediasSize
                            )
                            if (item.carouselMedias[visibleItem].mediaType == InstagramConstants.MediaType.VIDEO.type &&
                                mPlayManager.currentPlayerId != item.carouselMedias[visibleItem].id
                            ) {
                                item.carouselMedias[visibleItem].videoVersions[0].isPlay = true
                                recyclerView.adapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                })
            }
            else -> {
                val mHolder = holder as PostImageHolder

                txtLocationName = mHolder.txtLocation
                layoutLikersProfile = mHolder.layoutLikersProfile
//                layoutLikes = mHolder.layoutLikes
                txtLikesCount = mHolder.txtLikeCount
//                btnTopLikerUsername = mHolder.btnTopLikerUsername
//                btnOthers = mHolder.btnOthers
                txtUsername = mHolder.txtUsername
                txtCaption = mHolder.txtCaption
                imgProfile = mHolder.imgProfile
                btnComment = mHolder.btnComment
                btnShare = mHolder.btnShare
                layoutComment = mHolder.layoutComment
                btnViewComments = mHolder.btnViewComment
                btnLike = mHolder.btnLike
            }
        }


        val onClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                when (v!!.id) {
                    txtUsername.id -> {
                        mListener?.userProfile(v, item.user.pk, item.user.username)
                    }
                    btnComment.id -> {
                        mListener?.showComments(v!!, item)
                    }
                    btnViewComments.id -> {
                        mListener?.showComments(v!!, item)
                    }
                    btnShare.id -> {
                        mListener?.shareMedia(v, item.id, item.mediaType)
                    }
                    btnLike.id -> {
                        if (item.isHasLiked) {
                            btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
                            btnLike.setColorFilter(context.resources.color(R.color.white))
                            item.likeCount -= 1
                            mListener?.unlikePost(v, item.id)
                        } else {
                            btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
                            btnLike.setColorFilter(context.resources.color(R.color.red))
                            item.likeCount += 1
                            mListener?.likePost(v, item.id)
                        }
                        item.isHasLiked = !item.isHasLiked
                    }
                    txtLocationName.id -> {
                        mListener?.onLocationClick(v, location = item.location)
                    }
                }
            }
        }

        /* Location Start*/
        if (item.location == null) {
            txtLocationName.visibility = View.GONE
        } else {
            txtLocationName.text = item.location.shortName
            txtLocationName.visibility = View.VISIBLE
            txtLocationName.setOnClickListener(onClickListener)
        }
        /* Location End*/


        /* Profile top likers Start*/
        layoutLikersProfile.removeAllViews()
        if (item.facepileTopLikers != null && item.facepileTopLikers.isNotEmpty()) {
            layoutLikersProfile.visibility = View.VISIBLE
            txtLikesCount.setText(
                HyperTextView.getLikedByHyperText(
                    item.facepileTopLikers[0].username,
                    item.facepileTopLikers[0].pk,
                    TextUtil.getStringNumber(context, item.likeCount)
                )
            )
            for (liker in item.facepileTopLikers) {
                val likerProfile = CircleImageView(context)
                likerProfile.layoutParams = android.widget.LinearLayout.LayoutParams(
                    context.resources.dpToPx(20f),
                    context.resources.dpToPx(20f)
                )
                mGlide.load(liker.profilePicUrl).into(likerProfile)
                layoutLikersProfile.addView(likerProfile)
            }
        } else {
            layoutLikersProfile.visibility = View.GONE
            txtLikesCount.setText(
                HyperTextView.getLikersCountHyperText(
                    TextUtil.getStringNumber(
                        context,
                        item.likeCount
                    )
                )
            )
        }
        txtLikesCount.mHyperTextClick = mHyperTextClick
        /* Profile top likers End*/


        txtUsername.setText(item.user.username, item.user.pk, item.user.isVerified)
        txtUsername.mHyperTextClick = mHyperTextClick

        if (item.caption != null) {
            txtCaption.setText(
                item.user.username,
                item.user.pk,
                item.user.isVerified,
                item.caption.text,
                100
            )
            txtCaption.mHyperTextClick = mHyperTextClick
        }
        mGlide.load(item.user.profilePicUrl).into(imgProfile)
        if (!item.isCommentThreadingEnabled) {
            btnComment.visibility = View.GONE
        } else {
            btnComment.visibility = View.VISIBLE
        }
        layoutComment.removeAllViews()
        if (item.previewComments != null && item.previewComments.isNotEmpty()) {
            visible(btnViewComments)
            btnViewComments.text =
                String.format(
                    context.getString(R.string.view_all_comments),
                    TextUtil.getStringNumber(context, item.commentCount)
                )
            btnViewComments.setOnClickListener(onClickListener)
            for (comment in item.previewComments) {
                val layoutPreviewComment =
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
                val txtComment: HyperTextView = layoutPreviewComment.findViewById(R.id.txt_comment)
                val imgLike: AppCompatImageView = layoutPreviewComment.findViewById(R.id.img_like)

                txtComment.setText(
                    comment.user.username,
                    comment.user.pk,
                    comment.user.isVerified,
                    comment.text,
                    100

                )
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
                        mListener?.likeComment(it, comment.pk)
                    } else {
                        imgLike.setImageResource(R.drawable.instagram_heart_outline_24)
                        imgLike.setColorFilter(context.resources.color(R.color.text_light))
                        mListener?.unlikeComment(it, comment.pk)
                    }
                    comment.hasLikedComment = !comment.hasLikedComment
                }
                layoutComment.addView(layoutPreviewComment)
            }
        } else {
            gone(btnViewComments)
        }

        if (item.isHasLiked) {
            btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
            btnLike.setColorFilter(context.resources.color(R.color.red))
        } else {
            btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
            btnLike.setColorFilter(context.resources.color(R.color.white))
        }
        btnComment.setOnClickListener(onClickListener)
        btnShare.setOnClickListener(onClickListener)
        btnLike.setOnClickListener(onClickListener)
    }

    private fun removeVideoView(videoView: PlayerView) {
        val parent = videoView.parent ?: return
        parent as ViewGroup
        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
        }
    }

    private fun loadImage(
        width: Int,
        height: Int,
        url: String,
        imgPhoto: AppCompatImageView,
        mediaType: Int = InstagramConstants.MediaType.IMAGE.type
    ) {
        var imageWidth = displayWidth
        var imageHeight = height

        if (mediaType == InstagramConstants.MediaType.IMAGE.type) {
            imgPhoto.layoutParams.apply {
                if (height < displayWidth && width < displayWidth) {
                    this.height = displayWidth
                    imageHeight = displayWidth
                } else {
                    this.height = height
                    imageHeight = height
                }
            }
        }
        mGlide
            .load(url)
            .override(imageWidth,imageHeight)
            .priority(Priority.IMMEDIATE)
            .into(imgPhoto)
    }


    private fun getStandardVideoSize(width: Int, height: Int): Array<Int> {
        var standardHeight = (height * displayWidth) / width
        if (standardHeight > width && standardHeight > displayHeight / 1.5) {
            standardHeight = (displayHeight.toFloat() / 1.5f).toInt()
            Log.i(InstagramConstants.DEBUG_TAG, "line 358 height: $standardHeight")
        } else {

        }
        return arrayOf(displayWidth, standardHeight)
    }

    inner class LoadingViewHolder(itemView: ViewGroup) : RecyclerView.ViewHolder(itemView) {}
    inner class PostVideoHolder(itemView: ViewGroup) : RecyclerView.ViewHolder(itemView) {

        val imgProfile: CircleImageView
        val txtUsername: HyperTextView
        val txtLocation: AppCompatTextView
        val txtLikeCount: HyperTextView
        val btnViewComment: AppCompatTextView
        val txtCaption: HyperTextView
        val mediaContainer: FrameLayout
        val photoView: AppCompatImageView
        val imgPlay: AppCompatImageView
        val btnLike: AppCompatImageView
        val btnComment: AppCompatImageView
        val btnShare: AppCompatImageView
        val frameLayoutMedia: FrameLayout
        val layoutComment: LinearLayout
        val layoutLikersProfile: LinearLayout


        init {
            val linearLayoutParent = LinearLayout(itemView.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                setPadding(0, context.resources.dpToPx(10f), 0, context.resources.dpToPx(10f))
            }
            itemView.addView(linearLayoutParent)

            val linearLayoutHeader = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            linearLayoutParent.addView(linearLayoutHeader)

            imgProfile = CircleImageView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    context.resources.dpToPx(40f),
                    context.resources.dpToPx(40f)
                ).apply {
                    this.marginStart = context.resources.dpToPx(10f)
                }
                id = View.generateViewId()
            }
            linearLayoutHeader.addView(imgProfile)

            val linearLayoutUsernameLocation = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = context.resources.dpToPx(10f)
                }
                this.gravity = Gravity.CENTER_VERTICAL
                this.orientation = LinearLayout.VERTICAL
            }
            linearLayoutHeader.addView(linearLayoutUsernameLocation)

            txtUsername = HyperTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.color(R.color.text_very_light))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                id = View.generateViewId()
            }
            linearLayoutUsernameLocation.addView(txtUsername)


            txtLocation = AppCompatTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.color(R.color.text_very_light))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                id = View.generateViewId()
            }
            linearLayoutUsernameLocation.addView(txtLocation)

            frameLayoutMedia = FrameLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.topMargin = resources.dpToPx(10f)
                }
                id = View.generateViewId()
            }
            linearLayoutParent.addView(frameLayoutMedia)

            mediaContainer = FrameLayout(itemView.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            frameLayoutMedia.addView(mediaContainer)

            photoView = AppCompatImageView(itemView.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            frameLayoutMedia.addView(photoView)

            imgPlay = AppCompatImageView(itemView.context).apply {
                layoutParams =
                    FrameLayout.LayoutParams(resources.dpToPx(50f), resources.dpToPx(50f)).apply {
                        this.gravity = Gravity.CENTER
                    }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageResource(R.drawable.ic_play_circle)
                setColorFilter(context.color(R.color.white))
                id = View.generateViewId()
            }
            frameLayoutMedia.addView(imgPlay)


            val relativeLayoutPostOptions = RelativeLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(
                    context.resources.dpToPx(5f),
                    context.resources.dpToPx(5f),
                    context.resources.dpToPx(5f),
                    context.resources.dpToPx(5f)
                )
            }
            linearLayoutParent.addView(relativeLayoutPostOptions)


            btnLike = AppCompatImageView(itemView.context).apply {
                id = View.generateViewId()
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.alignWithParent = true
                    setPadding(
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f)
                    )
                    setColorFilter(context.color(R.color.white))
                    setImageResource(R.drawable.instagram_heart_outline_24)
                }
                id = View.generateViewId()
            }
            relativeLayoutPostOptions.addView(btnLike)


            btnComment = AppCompatImageView(itemView.context).apply {
                id = View.generateViewId()
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.END_OF, btnLike.id)
                    setPadding(
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f)
                    )
                    setColorFilter(context.color(R.color.white))
                    setImageResource(R.drawable.instagram_comment_outline_24)
                }
                id = View.generateViewId()
            }
            relativeLayoutPostOptions.addView(btnComment)


            btnShare = AppCompatImageView(itemView.context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.addRule(RelativeLayout.END_OF, btnComment.id)
                }
                setPadding(
                    resources.dpToPx(5f),
                    resources.dpToPx(5f),
                    resources.dpToPx(5f),
                    resources.dpToPx(5f)
                )
                setColorFilter(context.color(R.color.white))
                setImageResource(R.drawable.instagram_direct_outline_24)
                id = View.generateViewId()
            }
            relativeLayoutPostOptions.addView(btnShare)

            val linearLayoutLikes = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                }
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
            }
            linearLayoutParent.addView(linearLayoutLikes)

            val linearLayoutContainerLike = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                }
                orientation = LinearLayout.HORIZONTAL
            }
            linearLayoutParent.addView(linearLayoutContainerLike)

            layoutLikersProfile = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = resources.dpToPx(5f)
                }
                id = View.generateViewId()
                orientation = LinearLayout.HORIZONTAL
            }
            linearLayoutContainerLike.addView(layoutLikersProfile)

            txtLikeCount = HyperTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.color(R.color.text_very_light))
                id = View.generateViewId()
            }
            linearLayoutContainerLike.addView(txtLikeCount)

            txtCaption = HyperTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                    this.topMargin = resources.dpToPx(5f)
                }
                setTextColor(context.color(R.color.text_very_light))
                id = View.generateViewId()
            }
            linearLayoutParent.addView(txtCaption)

            btnViewComment = AppCompatTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                    this.topMargin = resources.dpToPx(5f)
                }
                setTextColor(context.color(R.color.text_light))
                id = View.generateViewId()
            }
            linearLayoutParent.addView(btnViewComment)


            layoutComment = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                    this.topMargin = resources.dpToPx(5f)
                }
                orientation = LinearLayout.VERTICAL
                id = View.generateViewId()
            }
            linearLayoutParent.addView(layoutComment)
        }
    }

    inner class PostImageHolder(itemView: ViewGroup) : RecyclerView.ViewHolder(itemView) {

        val imgProfile: CircleImageView
        val txtUsername: HyperTextView
        val txtLocation: AppCompatTextView
        val txtLikeCount: HyperTextView
        val btnViewComment: AppCompatTextView
        val txtCaption: HyperTextView
        val imgPhoto: AppCompatImageView
        val btnLike: AppCompatImageView
        val btnComment: AppCompatImageView
        val btnShare: AppCompatImageView
        val layoutComment: LinearLayout
        val layoutLikersProfile: LinearLayout

        init {
            val linearLayoutParent = LinearLayout(itemView.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                setPadding(0, context.resources.dpToPx(5f), 0, context.resources.dpToPx(5f))
            }
            itemView.addView(linearLayoutParent)

            val linearLayoutHeader = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            linearLayoutParent.addView(linearLayoutHeader)

            imgProfile = CircleImageView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    context.resources.dpToPx(40f),
                    context.resources.dpToPx(40f)
                ).apply {
                    this.marginStart = context.resources.dpToPx(10f)
                }
                id = View.generateViewId()
            }
            linearLayoutHeader.addView(imgProfile)

            val linearLayoutUsernameLocation = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = context.resources.dpToPx(10f)
                    this.gravity = Gravity.CENTER_VERTICAL
                }
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
            }
            linearLayoutHeader.addView(linearLayoutUsernameLocation)

            txtUsername = HyperTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.color(R.color.text_very_light))
                id = View.generateViewId()
            }
            linearLayoutUsernameLocation.addView(txtUsername)


            txtLocation = AppCompatTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.color(R.color.text_very_light))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                id = View.generateViewId()
            }
            linearLayoutUsernameLocation.addView(txtLocation)

            imgPhoto = AppCompatImageView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.topMargin = context.resources.dpToPx(10f)
                }
                id = View.generateViewId()
            }
            linearLayoutParent.addView(imgPhoto)

            val relativeLayoutPostOptions = RelativeLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(
                    context.resources.dpToPx(5f),
                    context.resources.dpToPx(5f),
                    context.resources.dpToPx(5f),
                    context.resources.dpToPx(5f)
                )
            }
            linearLayoutParent.addView(relativeLayoutPostOptions)


            btnLike = AppCompatImageView(itemView.context).apply {
                id = View.generateViewId()
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.alignWithParent = true
                    setPadding(
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f)
                    )
                    setColorFilter(context.color(R.color.white))
                    setImageResource(R.drawable.instagram_heart_outline_24)
                }
                id = View.generateViewId()
            }
            relativeLayoutPostOptions.addView(btnLike)


            btnComment = AppCompatImageView(itemView.context).apply {
                id = View.generateViewId()
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.addRule(RelativeLayout.END_OF, btnLike.id)
                }
                setPadding(
                    resources.dpToPx(5f),
                    resources.dpToPx(5f),
                    resources.dpToPx(5f),
                    resources.dpToPx(5f)
                )
                setColorFilter(context.color(R.color.white))
                setImageResource(R.drawable.instagram_comment_outline_24)
                id = View.generateViewId()
            }
            relativeLayoutPostOptions.addView(btnComment)


            btnShare = AppCompatImageView(itemView.context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.addRule(RelativeLayout.END_OF, btnComment.id)
                }
                setPadding(
                    resources.dpToPx(5f),
                    resources.dpToPx(5f),
                    resources.dpToPx(5f),
                    resources.dpToPx(5f)
                )
                setColorFilter(context.color(R.color.white))
                setImageResource(R.drawable.instagram_direct_outline_24)
                id = View.generateViewId()
            }
            relativeLayoutPostOptions.addView(btnShare)

            val linearLayoutLikes = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                }
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
            }
            linearLayoutParent.addView(linearLayoutLikes)

            val linearLayoutContainerLike = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                }
                orientation = LinearLayout.HORIZONTAL
            }
            linearLayoutParent.addView(linearLayoutContainerLike)

            layoutLikersProfile = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = resources.dpToPx(5f)
                }
                orientation = LinearLayout.HORIZONTAL
                id = View.generateViewId()
            }
            linearLayoutContainerLike.addView(layoutLikersProfile)

            txtLikeCount = HyperTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.color(R.color.text_very_light))
                id = View.generateViewId()
            }
            linearLayoutContainerLike.addView(txtLikeCount)

            txtCaption = HyperTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                    this.topMargin = resources.dpToPx(5f)
                }
                setTextColor(context.color(R.color.text_very_light))
                id = View.generateViewId()
            }
            linearLayoutParent.addView(txtCaption)

            btnViewComment = AppCompatTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                    this.topMargin = resources.dpToPx(5f)
                }
                setTextColor(context.color(R.color.text_light))
                id = View.generateViewId()
            }
            linearLayoutParent.addView(btnViewComment)


            layoutComment = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                    this.topMargin = resources.dpToPx(5f)
                }
                orientation = LinearLayout.VERTICAL
                id = View.generateViewId()
            }
            linearLayoutParent.addView(layoutComment)
        }
    }

    inner class PostCarouselMediaHolder(itemView: ViewGroup) : RecyclerView.ViewHolder(itemView) {

        val imgProfile: CircleImageView
        val txtUsername: HyperTextView
        val txtLocation: AppCompatTextView
        val txtLikeCount: HyperTextView
        val txtPagePosition: AppCompatTextView
        val btnViewComment: AppCompatTextView
        val txtCaption: HyperTextView
        val recyclerviewMedia: RecyclerView
        val btnLike: AppCompatImageView
        val btnComment: AppCompatImageView
        val btnShare: AppCompatImageView
        val layoutComment: LinearLayout
        val layoutLikersProfile: LinearLayout

        init {
            val linearLayoutParent = LinearLayout(itemView.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                setPadding(0, context.resources.dpToPx(5f), 0, context.resources.dpToPx(5f))
            }
            itemView.addView(linearLayoutParent)

            val linearLayoutHeader = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            linearLayoutParent.addView(linearLayoutHeader)

            imgProfile = CircleImageView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    context.resources.dpToPx(40f),
                    context.resources.dpToPx(40f)
                ).apply {
                    this.marginStart = context.resources.dpToPx(10f)
                }
                id = View.generateViewId()
            }
            linearLayoutHeader.addView(imgProfile)

            val linearLayoutUsernameLocation = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = context.resources.dpToPx(10f)
                    this.gravity = Gravity.CENTER_VERTICAL
                }
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
            }
            linearLayoutHeader.addView(linearLayoutUsernameLocation)

            txtUsername = HyperTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.color(R.color.text_very_light))
                id = View.generateViewId()
            }
            linearLayoutUsernameLocation.addView(txtUsername)


            txtLocation = AppCompatTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.color(R.color.text_very_light))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                id = View.generateViewId()
            }
            linearLayoutUsernameLocation.addView(txtLocation)

            val layoutMedia = RelativeLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.topMargin = resources.dpToPx(10f)
                }
            }
            linearLayoutParent.addView(layoutMedia)

            recyclerviewMedia = RecyclerView(itemView.context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                id = View.generateViewId()
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            try {
                val snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(recyclerviewMedia)
            } catch (e: Exception) {
            }
            layoutMedia.addView(recyclerviewMedia)

            txtPagePosition = AppCompatTextView(itemView.context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.addRule(RelativeLayout.ALIGN_PARENT_END)
                    this.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                    this.topMargin = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                }
                setBackgroundResource(R.drawable.bg_circluar)
                setPadding(
                    resources.dpToPx(10f),
                    resources.dpToPx(5f),
                    resources.dpToPx(10f),
                    resources.dpToPx(5f)
                )
                setTextColor(context.color(R.color.text_very_light))
                id = View.generateViewId()
            }
            layoutMedia.addView(txtPagePosition)

            val relativeLayoutPostOptions = RelativeLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(
                    context.resources.dpToPx(5f),
                    context.resources.dpToPx(5f),
                    context.resources.dpToPx(5f),
                    context.resources.dpToPx(5f)
                )
            }
            linearLayoutParent.addView(relativeLayoutPostOptions)


            btnLike = AppCompatImageView(itemView.context).apply {
                id = View.generateViewId()
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.alignWithParent = true
                    setPadding(
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f)
                    )
                    setColorFilter(context.color(R.color.white))
                    setImageResource(R.drawable.instagram_heart_outline_24)
                }
                id = View.generateViewId()
            }
            relativeLayoutPostOptions.addView(btnLike)


            btnComment = AppCompatImageView(itemView.context).apply {
                id = View.generateViewId()
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.END_OF, btnLike.id)
                    setPadding(
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f)
                    )
                    setColorFilter(context.color(R.color.white))
                    setImageResource(R.drawable.instagram_comment_outline_24)
                }
                id = View.generateViewId()
            }
            relativeLayoutPostOptions.addView(btnComment)


            btnShare = AppCompatImageView(itemView.context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.END_OF, btnComment.id)
                    setPadding(
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f),
                        resources.dpToPx(5f)
                    )
                    setColorFilter(context.color(R.color.white))
                    setImageResource(R.drawable.instagram_direct_outline_24)
                }
                id = View.generateViewId()
            }
            relativeLayoutPostOptions.addView(btnShare)

            val linearLayoutLikes = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                }
                gravity = Gravity.CENTER_VERTICAL
                orientation = LinearLayout.HORIZONTAL
            }
            linearLayoutParent.addView(linearLayoutLikes)

            val linearLayoutContainerLike = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                }
                orientation = LinearLayout.HORIZONTAL
            }
            linearLayoutParent.addView(linearLayoutContainerLike)

            layoutLikersProfile = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = resources.dpToPx(5f)
                }
                orientation = LinearLayout.HORIZONTAL
                id = View.generateViewId()
            }
            linearLayoutContainerLike.addView(layoutLikersProfile)

            txtLikeCount = HyperTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(context.color(R.color.text_very_light))
                id = View.generateViewId()
            }
            linearLayoutContainerLike.addView(txtLikeCount)

            txtCaption = HyperTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                    this.topMargin = resources.dpToPx(5f)
                }
                setTextColor(context.color(R.color.text_very_light))
                id = View.generateViewId()
            }
            linearLayoutParent.addView(txtCaption)

            btnViewComment = AppCompatTextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                    this.topMargin = resources.dpToPx(5f)
                }
                setTextColor(context.color(R.color.text_light))
                id = View.generateViewId()
            }
            linearLayoutParent.addView(btnViewComment)

            layoutComment = LinearLayout(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.marginStart = resources.dpToPx(10f)
                    this.marginEnd = resources.dpToPx(10f)
                    this.topMargin = resources.dpToPx(5f)
                }
                orientation = LinearLayout.VERTICAL
                id = View.generateViewId()
            }
            linearLayoutParent.addView(layoutComment)
        }
    }

    inner class CollectionMediaAdapter constructor(
        var positionOfAdapter: Int,
        var items: List<CarouselMedia>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val frameLayout = FrameLayout(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.MATCH_PARENT
                )
            }
            if (viewType == InstagramConstants.MediaType.IMAGE.type) {
                return ImageViewHolder(frameLayout)
            } else {
                return VideoViewHolder(frameLayout)
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            if (item.mediaType == InstagramConstants.MediaType.IMAGE.type) {
                val condidate = item.imageVersions2.candidates[0]
                loadImage(
                    condidate.width,
                    condidate.height,
                    condidate.url,
                    (holder as ImageViewHolder).imgPhoto,
                    InstagramConstants.MediaType.CAROUSEL_MEDIA.type
                )
            } else {
                val viewHolder = holder as VideoViewHolder
                val video = item.videoVersions[0]
                val image = item.imageVersions2.candidates[0]

                loadImage(
                    image.width,
                    image.height,
                    image.url,
                    viewHolder.imgPhoto,
                    InstagramConstants.MediaType.CAROUSEL_MEDIA.type
                )
//
//                mPicasso
//                    .load(image.url)
//                    .placeholder(R.drawable.post_load_place_holder)
//                    .into(dataBinding.photoView)
                val dataSource =
                    DefaultHttpDataSourceFactory(
                        Util.getUserAgent(
                            context!!,
                            "Instagram"
                        )
                    )
//                viewHolder.mediaContainer.setOnClickListener {
//                    if (mPlayManager.isSoundEnable()) {
//                        mPlayManager.disableSound()
//                    } else {
//                        mPlayManager.enableSound()
//                    }
//                }

                if (item.videoVersions[0].isPlay) {
                    val uri = Uri.parse(video.url)
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSource)
                            .createMediaSource(uri)
                    viewHolder.imgPhoto.visibility = View.GONE
                    viewHolder.imgPlay.visibility = View.GONE
                    removeVideoView(videoView)
                    viewHolder.mediaContainer.addView(videoView)
                    mPlayManager.startPlay(mediaSource, item.id)
                    currentMediaPosition = positionOfAdapter
                } else {
                    if (mPlayManager.currentPlayerId == item.id) {
                        mPlayManager.stopPlay()
                    }
                    viewHolder.mediaContainer.removeAllViews()
                    viewHolder.imgPhoto.visibility = View.VISIBLE
                    viewHolder.imgPlay.visibility = View.VISIBLE
                }

                viewHolder.itemView.setOnClickListener {
                    item.videoVersions[0].isPlay = true
                    this@CollectionMediaAdapter.notifyItemChanged(position)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            val item = items[position]
            return item.mediaType
        }

        inner class ImageViewHolder constructor(itemView: ViewGroup) :
            RecyclerView.ViewHolder(itemView) {
            val imgPhoto: AppCompatImageView

            init {
                imgPhoto = AppCompatImageView(itemView.context).apply {
                    id = View.generateViewId()
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    adjustViewBounds = true
                }
                itemView.addView(imgPhoto)
            }
        }

        inner class VideoViewHolder constructor(itemView: ViewGroup) :
            RecyclerView.ViewHolder(itemView) {
            val imgPhoto: AppCompatImageView
            val imgPlay: AppCompatImageView
            val mediaContainer: FrameLayout

            init {
                mediaContainer = FrameLayout(itemView.context)
                mediaContainer.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                itemView.addView(mediaContainer)

                imgPhoto = AppCompatImageView(itemView.context).apply {
                    id = View.generateViewId()
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = true
                }
                itemView.addView(imgPhoto)

                imgPlay = AppCompatImageView(itemView.context).apply {
                    id = View.generateViewId()
                    layoutParams =
                        FrameLayout.LayoutParams(resources.dpToPx(50f), resources.dpToPx(50f))
                            .apply {
                                this.gravity = Gravity.CENTER
                            }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = true
                    isClickable = false
                    isFocusable = false
                    setImageResource(R.drawable.ic_play_circle)
                    setColorFilter(Color.WHITE)
                }
                itemView.addView(imgPlay)
            }
        }
        /*
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            if (item.mediaType == InstagramConstants.MediaType.IMAGE.type) {
                val dataBinding = holder.binding as LayoutCarouselImageBinding

                val condidate = item.imageVersions2.candidates[0]
                loadImage(condidate.width, condidate.height, condidate.url, dataBinding.imgPhoto,InstagramConstants.MediaType.CAROUSEL_MEDIA.type)
//                mPicasso.load(item.imageVersions2.candidates[1].url)
//                    .into(dataBinding.imgPhoto)
            } else {

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
        }*/

    }

}