package com.idirect.app.customview.postsrecyclerview

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.databinding.DataBindingUtil
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
import com.idirect.app.core.BaseAdapter
import com.idirect.app.customview.customtextview.HyperTextView
import com.idirect.app.databinding.LayoutPostCarouselMediaBinding
import com.idirect.app.databinding.LayoutPostImageBinding
import com.idirect.app.databinding.LayoutPostVideoBinding
import com.idirect.app.datasource.model.event.LoadingEvent
import com.idirect.app.extensions.color
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.extentions.color
import com.idirect.app.extentions.dpToPx
import com.idirect.app.manager.PlayManager
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.TextUtil
import com.muzafferus.imagelist.ImageList
import com.sanardev.instagramapijava.model.media.CarouselMedia
import com.sanardev.instagramapijava.model.timeline.FeedItems
import com.sanardev.instagramapijava.model.timeline.MediaOrAd
import com.tylersuehr.chips.CircleImageView

class PostsAdapter3(
    val context: Context,
    val mHyperTextClick: HyperTextView.OnHyperTextClick,
    val mPlayManager: PlayManager,
    val mGlide: RequestManager,
    viewLifecycleOwner: LifecycleOwner
) : BaseAdapter() {


    companion object {
        const val LOADGING = 100
    }

    private val dataSource: DataSource.Factory =
        DefaultHttpDataSourceFactory(Util.getUserAgent(context, "Instagram"))
    var currentMediaPosition: Int = PlayManager.NONE
    var captionLenghtLimit:Int = 100
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
                    if (item is FeedItems && item.mediaOrAd.mediaType == InstagramConstants.MediaType.VIDEO.type && item.mediaOrAd.id == it.currentPlayId) {
                        item.mediaOrAd.videoVersions[0].bundle["isPlay"] = false
                        item.mediaOrAd.videoVersions[0].bundle["playPosition"] =
                            mPlayManager.player.currentPosition
                        notifyItemChanged(index)
                        return@Observer
                    }
                    if (item is FeedItems && item.mediaOrAd.mediaType == InstagramConstants.MediaType.CAROUSEL_MEDIA.type) {
                        for (indexCarousel in item.mediaOrAd.carouselMedias.indices) {
                            val carousel = item.mediaOrAd.carouselMedias[indexCarousel]
                            if (carousel.id == it.currentPlayId) {
                                carousel.videoVersions[0].bundle["isPlay"] = false
                                carousel.videoVersions[0].bundle["playPosition"] =
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
                    if(item is FeedItems){
                        if(item.mediaOrAd == null){
                            continue
                        }
                        if (item.mediaOrAd.mediaType == InstagramConstants.MediaType.VIDEO.type && it.previousPlayId == item.mediaOrAd.id) {
                            item.mediaOrAd.videoVersions[0].bundle["isPlay"] = false
                            item.mediaOrAd.videoVersions[0].bundle["playPosition"] = 0
                            notifyItemChanged(index)
                            return@Observer
                        } else if (item.mediaOrAd.mediaType == InstagramConstants.MediaType.CAROUSEL_MEDIA.type) {
                            for (indexCarousel in item.mediaOrAd.carouselMedias.indices) {
                                val carousel = item.mediaOrAd.carouselMedias[indexCarousel]
                                if (carousel.id == it.previousPlayId) {
                                    carousel.videoVersions[0].bundle["isPlay"] = false
                                    carousel.videoVersions[0].bundle["playPosition"] = 0
                                    adapters[index]?.notifyItemChanged(indexCarousel)
                                    return@Observer
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
        val item = items[position]

        if (item is LoadingEvent) {
            return item
        }
        val mediaOrAd:MediaOrAd = if(item is FeedItems){
            item.mediaOrAd
        }else{
            item as MediaOrAd
        }
        if(mediaOrAd.videoVersions != null){
            for (video in mediaOrAd.videoVersions){
                if(video.bundle == null){
                    video.bundle = HashMap<Any,Any>().apply {
                        this["isPlay"] = false
                        this["playPosition"] = 0
                    }
                }
            }
        }else if(mediaOrAd.carouselMedias != null){
            for (media in mediaOrAd.carouselMedias){
                if(media != null && media.videoVersions != null){
                    for (video in media.videoVersions){
                        if(video.bundle == null){
                            video.bundle = HashMap<Any,Any>().apply {
                                this["isPlay"] = false
                                this["playPosition"] = 0
                            }
                        }
                    }
                }
            }
        }

        val txtLocationName: AppCompatTextView
        val imageList: ImageList
        val layoutLikes: LinearLayout
        val layoutComment: LinearLayout
        val txtLikesCount: AppCompatTextView
        val txtCommentCount: AppCompatTextView
        val btnViewComments: AppCompatTextView
        val txtUsername: HyperTextView
        val txtCaption: AppCompatTextView
        val btnComment: AppCompatImageView
        val btnLike: AppCompatImageView
        val btnShare: AppCompatImageView
        val imgProfile: CircleImageView

        when (mediaOrAd.mediaType) {
            InstagramConstants.MediaType.IMAGE.type -> {
                val binding = holder.binding as LayoutPostImageBinding
                txtLocationName = binding.txtLocationName
                imageList = binding.imageList
                txtLikesCount = binding.txtLikesCount
                txtCommentCount = binding.txtCommentCount
                txtUsername = binding.txtUsername
                txtCaption = binding.txtCaption
                imgProfile = binding.imgProfile
                btnComment = binding.btnComment
                btnShare = binding.btnShare
                btnLike = binding.btnLike


                val candidate = mediaOrAd.imageVersions2.candidates[0]
                loadImage(
                    candidate.width,
                    candidate.height,
                    candidate.url,
                    binding.imgPhoto,
                    InstagramConstants.MediaType.IMAGE.type
                )
            }
            InstagramConstants.MediaType.VIDEO.type -> {
                val binding = holder.binding as LayoutPostVideoBinding
                txtLocationName = binding.txtLocationName
                imageList = binding.imageList
                txtLikesCount = binding.txtLikesCount
                txtCommentCount = binding.txtCommentCount
                txtUsername = binding.txtUsername
                txtCaption = binding.txtCaption
                imgProfile = binding.imgProfile
                btnComment = binding.btnComment
                btnShare = binding.btnShare
                btnLike = binding.btnLike

                val video = mediaOrAd.videoVersions[0]!!
                val previewImage = mediaOrAd.imageVersions2.candidates[0]!!
                val size = getStandardVideoSize(video.width, video.height)

                /*config height of layout media. by remove this code, height of layout will be very small*/
                binding.layoutMedia.layoutParams.apply {
                    width = FrameLayout.LayoutParams.MATCH_PARENT
                    height = dpToPx(previewImage.height.toFloat(),context.resources)
                }

                if (size[0] < size[1]) {
                    binding.photoView.scaleType = ImageView.ScaleType.FIT_XY
                } else {
                    binding.photoView.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                loadImage(
                    previewImage.width,
                    previewImage.height,
                    previewImage.url,
                    binding.photoView,
                    InstagramConstants.MediaType.VIDEO.type
                )
                /* comment icon start*/
                if (!mediaOrAd.isCommentThreadingEnabled) {
                    binding.btnComment.visibility = View.GONE
                } else {
                    binding.btnComment.visibility = View.VISIBLE
                }
                /* comment icon end*/

                if (video.bundle != null && video.bundle["isPlay"] == true) {
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSource)
                            .createMediaSource(Uri.parse(video.url))
                    binding.photoView.visibility = View.GONE
                    binding.imgPlay.visibility = View.GONE
                    removeVideoView(videoView)
                    binding.layoutMedia.addView(videoView)
                    mPlayManager.startPlay(mediaSource, mediaOrAd.id)
                    currentMediaPosition = position
                    mGlide.clear(binding.photoView)
                } else {
                    if (mPlayManager.currentPlayerId == mediaOrAd.id) {
                        mPlayManager.stopPlay()
                    }
                    binding.layoutMedia.removeAllViews()
                    binding.photoView.visibility = View.VISIBLE
                    binding.imgPlay.visibility = View.VISIBLE
                }

                binding.layoutMedia.setOnClickListener {
                    video.bundle["isPlay"] = true
                    notifyItemChanged(position)
                }

            }
            InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                val binding = holder.binding as LayoutPostCarouselMediaBinding
                txtLocationName = binding.txtLocationName
                imageList = binding.imageList
                txtLikesCount = binding.txtLikesCount
                txtCommentCount = binding.txtCommentCount
                txtUsername = binding.txtUsername
                txtCaption = binding.txtCaption
                imgProfile = binding.imgProfile
                btnComment = binding.btnComment
                btnShare = binding.btnShare
                btnLike = binding.btnLike

                binding.txtPagePosition.text =
                    String.format(
                        context.getString(R.string.page_position),
                        1,
                        mediaOrAd.carouselMedias.size
                    )
                var maxWidth = 0
                var maxHeight = 0
                for (media in mediaOrAd.carouselMedias) {
                    if (media.originalWidth > maxWidth) {
                        maxWidth = media.originalWidth
                    }
                    if (media.originalHeight > maxHeight) {
                        maxHeight = media.originalHeight
                    }
                }
                val size = getStandardVideoSize(maxWidth, maxHeight)
                binding.recyclerviewMedias.layoutParams.apply {
                    width = displayWidth
                    height = size[1]
                }

                adapters.put(position, CollectionMediaAdapter(position, mediaOrAd.carouselMedias))
                binding.recyclerviewMedias.adapter = adapters[position]
                try {
                    val snapHelper = PagerSnapHelper()
                    snapHelper.attachToRecyclerView(binding.recyclerviewMedias)
                } catch (e: Exception) {
                }

                val mediasSize = mediaOrAd.carouselMedias.size
                binding.recyclerviewMedias.addOnScrollListener(object :
                    RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int
                    ) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            val visibleItem =
                                (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                            if (visibleItem >= mediasSize || visibleItem < 0) {
                                return
                            }
                            binding.txtPagePosition.text = String.format(
                                context.getString(R.string.page_position),
                                visibleItem + 1,
                                mediasSize
                            )
                            if (mediaOrAd.carouselMedias[visibleItem].mediaType == InstagramConstants.MediaType.VIDEO.type &&
                                mPlayManager.currentPlayerId != mediaOrAd.carouselMedias[visibleItem].id
                            ) {
                                mediaOrAd.carouselMedias[visibleItem].videoVersions[0].bundle.apply {
                                    this["isPlay"] = true
                                }
                                recyclerView.adapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                })
            }
            else -> {
                val binding = holder.binding as LayoutPostImageBinding
                txtLocationName = binding.txtLocationName
                imageList = binding.imageList
                txtLikesCount = binding.txtLikesCount
                txtCommentCount = binding.txtCommentCount
                txtUsername = binding.txtUsername
                txtCaption = binding.txtCaption
                imgProfile = binding.imgProfile
                btnComment = binding.btnComment
                btnShare = binding.btnShare
                btnLike = binding.btnLike

            }
        }


        val onClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                when (v!!.id) {
                    txtUsername.id -> {
                        mListener?.userProfile(v, mediaOrAd.user.pk, mediaOrAd.user.username)
                    }
                    btnComment.id -> {
                        mListener?.showComments(v!!, mediaOrAd)
                    }
                    btnShare.id -> {
                        mListener?.shareMedia(v, mediaOrAd.id, mediaOrAd.mediaType)
                    }
                    btnLike.id -> {
                        if (mediaOrAd.isHasLiked) {
                            btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
                            btnLike.setColorFilter(context.resources.color(R.color.icon))
                            mediaOrAd.likeCount -= 1
                            mListener?.unlikePost(v, mediaOrAd.id)
                        } else {
                            btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
                            btnLike.setColorFilter(context.resources.color(R.color.red))
                            mediaOrAd.likeCount += 1
                            mListener?.likePost(v, mediaOrAd.id)
                        }
                        mediaOrAd.isHasLiked = !mediaOrAd.isHasLiked
                    }
                    txtLocationName.id -> {
                        //                //#comment_code
//                        mListener?.onLocationClick(v, location = item.location)
                    }
                }
            }
        }

        /* Location Start*/
        if (mediaOrAd.location == null) {
            txtLocationName.visibility = View.GONE
        } else {
            txtLocationName.text = mediaOrAd.location.shortName
            txtLocationName.visibility = View.VISIBLE
            txtLocationName.setOnClickListener(onClickListener)
        }
        /* Location End*/


        txtUsername.setLinkTextColor(context.color(R.color.text_very_light))
        txtUsername.setText(mediaOrAd.user.username, mediaOrAd.user.pk, mediaOrAd.user.isVerified)
        txtUsername.mHyperTextClick = mHyperTextClick

        if (mediaOrAd.caption != null) {
            txtCaption.setText(
                mediaOrAd.caption.text
            )
        }
        mGlide.load(mediaOrAd.user.profilePicUrl).into(imgProfile)
        if (!mediaOrAd.isCommentThreadingEnabled) {
            btnComment.visibility = View.GONE
        } else {
            btnComment.visibility = View.VISIBLE
        }

        if (mediaOrAd.facepileTopLikers != null && mediaOrAd.facepileTopLikers.isNotEmpty()) {
            val list:ArrayList<String>  = ArrayList<String>()
            for (liker in mediaOrAd.facepileTopLikers) {
                if(list.size < 4){
                    list.add(liker.profilePicUrl)
                }
            }
            imageList.setImageCount(list.size)
            imageList.setImageList(list)
            imageList.setImageSize(30)
            imageList.setBorder(1, context.color(R.color.theme_background))
            imageList.setTextSize(10)
            imageList.setTextColor(ContextCompat.getColor(context, R.color.text_very_light))
            imageList.setTextBackground(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        }

        if (mediaOrAd.isHasLiked) {
            btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
            btnLike.setColorFilter(context.resources.color(R.color.red))
        } else {
            btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
            btnLike.setColorFilter(context.resources.color(R.color.icon))
        }
        btnComment.setOnClickListener(onClickListener)
        btnShare.setOnClickListener(onClickListener)
        btnLike.setOnClickListener(onClickListener)
        return item
    }

    override fun getLayoutIdForPosition(position: Int): Int {
        val item = items[position]
        if (item is LoadingEvent) {
            return R.layout.layout_loading
        }
        val mediaType:Int = if(item is FeedItems){
            item.mediaOrAd.mediaType
        }else{
            item as MediaOrAd
            item.mediaType
        }
        return when (mediaType) {
            InstagramConstants.MediaType.IMAGE.type -> {
                R.layout.layout_post_image
            }
            InstagramConstants.MediaType.VIDEO.type -> {
                R.layout.layout_post_video
            }
            InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                R.layout.layout_post_carousel_media
            }
            else -> {
                R.layout.layout_post_image
            }
        }
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
        imgPhoto: ImageView,
        mediaType: Int = InstagramConstants.MediaType.IMAGE.type
    ) {

//        if (mediaType == InstagramConstants.MediaType.IMAGE.type) {
//            imgPhoto.layoutParams.apply {
//                if (height < displayWidth && width < displayWidth) {
//                    this.height = displayWidth
//                    imageHeight = displayWidth
//                } else {
//                    this.height = height
//                    imageHeight = height
//                }
//            }
//        }
        mGlide
            .load(url)
            .priority(Priority.IMMEDIATE)
            .into(imgPhoto)
    }


    private fun getStandardVideoSize(width: Int, height: Int): Array<Int> {
        var standardHeight = (height * displayWidth) / width
        if (standardHeight > width && standardHeight > displayHeight / 1.3) {
            standardHeight = (displayHeight.toFloat() / 1.3).toInt()
        } else {

        }
        return arrayOf(displayWidth, height)
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

                if (item.videoVersions[0].bundle["isPlay"] == true) {
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
                    item.videoVersions[0].bundle["isPlay"] = true
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
                    setColorFilter(context.color(R.color.icon))
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