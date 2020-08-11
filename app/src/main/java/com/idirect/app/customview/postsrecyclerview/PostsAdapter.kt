package com.idirect.app.customview.postsrecyclerview

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.idirect.app.databinding.*
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
import java.lang.Exception

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
        val item = items[position]
        val showLikersClickListener = View.OnClickListener {

        }
        val showCommentClickListener = View.OnClickListener {
            val action = PostsFragmentDirections.actionPostsFragmentToCommentsFragment(item)
            it.findNavController().navigate(action)
        }

        val txtLocationName:AppCompatTextView
        val layoutLikersProfile:LinearLayout
        val layoutLikes:LinearLayout
        val layoutComment:LinearLayout
        val txtLikesCount:AppCompatTextView
        val btnViewComments:AppCompatTextView
        val btnTopLikerUsername:AppCompatTextView
        val btnOthers:AppCompatTextView
        val txtUsername:AppCompatTextView
        val txtCaption:AppCompatTextView
        val btnComment:AppCompatImageView
        val btnLike:AppCompatImageView
        val imgProfile:CircleImageView


        when(item.mediaType){
            InstagramConstants.MediaType.IMAGE.type ->{
                val dataBinding = (holder.binding as LayoutPostImageBinding)
                txtLocationName = dataBinding.txtLocationName
                layoutLikersProfile = dataBinding.layoutLikersProfile
                layoutLikes = dataBinding.layoutLikes
                txtLikesCount = dataBinding.txtLikesCount
                btnTopLikerUsername = dataBinding.btnTopLikerUsername
                btnOthers = dataBinding.btnOthers
                txtUsername = dataBinding.txtUsername
                txtCaption= dataBinding.txtCaption
                imgProfile= dataBinding.imgProfile
                btnComment = dataBinding.btnComment
                layoutComment = dataBinding.layoutComment
                btnViewComments = dataBinding.btnViewComments
                btnLike = dataBinding.btnLike
            }
            InstagramConstants.MediaType.VIDEO.type ->{
                val dataBinding = (holder.binding as LayoutPostVideoBinding)
                txtLocationName = dataBinding.txtLocationName
                layoutLikersProfile = dataBinding.layoutLikersProfile
                layoutLikes = dataBinding.layoutLikes
                txtLikesCount = dataBinding.txtLikesCount
                btnTopLikerUsername = dataBinding.btnTopLikerUsername
                btnOthers = dataBinding.btnOthers
                txtUsername = dataBinding.txtUsername
                txtCaption= dataBinding.txtCaption
                imgProfile= dataBinding.imgProfile
                btnComment = dataBinding.btnComment
                layoutComment = dataBinding.layoutComment
                btnViewComments = dataBinding.btnViewComments
                btnLike = dataBinding.btnLike
            }
            InstagramConstants.MediaType.CAROUSEL_MEDIA.type ->{
                val dataBinding = (holder.binding as LayoutPostCarouselMediaBinding)
                txtLocationName = dataBinding.txtLocationName
                layoutLikersProfile = dataBinding.layoutLikersProfile
                layoutLikes = dataBinding.layoutLikes
                txtLikesCount = dataBinding.txtLikesCount
                btnTopLikerUsername = dataBinding.btnTopLikerUsername
                btnOthers = dataBinding.btnOthers
                txtUsername = dataBinding.txtUsername
                txtCaption= dataBinding.txtCaption
                imgProfile= dataBinding.imgProfile
                btnComment = dataBinding.btnComment
                layoutComment = dataBinding.layoutComment
                btnViewComments = dataBinding.btnViewComments
                btnLike = dataBinding.btnLike
            }
            else ->{
                val dataBinding = (holder.binding as LayoutPostImageBinding)
                txtLocationName = dataBinding.txtLocationName
                layoutLikersProfile = dataBinding.layoutLikersProfile
                layoutLikes = dataBinding.layoutLikes
                txtLikesCount = dataBinding.txtLikesCount
                btnTopLikerUsername = dataBinding.btnTopLikerUsername
                btnOthers = dataBinding.btnOthers
                txtUsername = dataBinding.txtUsername
                txtCaption= dataBinding.txtCaption
                imgProfile= dataBinding.imgProfile
                btnComment = dataBinding.btnComment
                layoutComment = dataBinding.layoutComment
                btnViewComments = dataBinding.btnViewComments
                btnLike = dataBinding.btnLike
            }
        }

        /* Location Start*/
        if (item.location == null) {
            txtLocationName.visibility = View.GONE
        } else {
            txtLocationName.text = item.location.shortName
            txtLocationName.visibility = View.VISIBLE
        }
        /* Location End*/


        /* Profile top likers Start*/
        layoutLikersProfile.removeAllViews()
        if (item.facepileTopLikers != null && item.facepileTopLikers.isNotEmpty()) {
            visible(layoutLikes)
            gone(txtLikesCount)
            btnTopLikerUsername.text = item.facepileTopLikers[0].username
            btnTopLikerUsername.setOnClickListener {
                item.facepileTopLikers?.let {
                    item.facepileTopLikers[0].pk
                }
            }
            btnOthers.text =
                String.format(context.getString(R.string.others_liker_count), item.likeCount)
            btnOthers.setOnClickListener(showLikersClickListener)
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
            visible(txtLikesCount)
            gone(layoutLikes)
            txtLikesCount.text =
                String.format(context.getString(R.string.liker_count), item.likeCount)
            txtLikesCount.setOnClickListener(showLikersClickListener)
        }
        /* Profile top likers End*/


        txtUsername.text = item.user.username
        if (item.caption != null) {
            txtCaption.setText(
                item.caption.user.username,
                item.caption.user.pk,
                item.caption.text
            )
            txtCaption.mHyperTextClick = mHyperTextClick
        }
        mGlide.load(item.user.profilePicUrl).into(imgProfile)
        when (item.mediaType) {
            InstagramConstants.MediaType.IMAGE.type -> {
                val dataBinding = (holder.binding as LayoutPostImageBinding)
                mGlide
                    .load(item.imageVersions2.candidates[0].url)
                    .placeholder(R.drawable.post_load_place_holder)
                    .into(dataBinding.photoView)
            }
            InstagramConstants.MediaType.VIDEO.type -> {
                val dataBinding = (holder.binding as LayoutPostVideoBinding)
                val video = item.videoVersions[0]!!
                val previewImage = item.imageVersions2.candidates[0]!!
                val size = getStandardVideoSize(video.width, video.height)
                dataBinding.layoutMedia.layoutParams =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size[1])
                if (video.isPlay) {
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSource)
                            .createMediaSource(Uri.parse(video.url))
                    dataBinding.videoView.player = mPlayManager.player
                    dataBinding.photoView.visibility = View.GONE
                    dataBinding.imgPlay.visibility = View.GONE
                    mPlayManager.startPlay(mediaSource, item.id)
                    dataBinding.videoView.showController()
                    currentMediaPosition = position
                } else {
                    if (mPlayManager.currentPlayerId == item.id) {
                        mPlayManager.stopPlay()
                    }
                    dataBinding.videoView.player = null
                    dataBinding.photoView.visibility = View.VISIBLE
                    dataBinding.imgPlay.visibility = View.VISIBLE

                    mGlide.load(previewImage.url).centerCrop().into(dataBinding.photoView)
                }
                dataBinding.layoutMedia.setOnClickListener {
                    video.isPlay = true
                    notifyItemChanged(position)
                }
                dataBinding.videoView.setOnClickListener {
                    if (mPlayManager.currentPlayerId == video.id) {
                        if (mPlayManager.isSoundEnable()) {
                            mPlayManager.disableSound()
                        } else {
                            mPlayManager.enableSound()
                        }
                    }
                }
            }
            InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                val dataBinding = (holder.binding as LayoutPostCarouselMediaBinding)
                val carouselMedias = item.carouselMedias
                dataBinding.txtPagePosition.text =
                    String.format(context.getString(R.string.page_position), 1, carouselMedias.size)
                var maxWidth = 0
                var maxHeight = 0
                for(media in carouselMedias){
                    if(media.originalWidth > maxWidth){
                        maxWidth = media.originalWidth
                    }
                    if(media.originalHeight > maxHeight){
                        maxHeight = media.originalHeight
                    }
                }
                val size = getStandardVideoSize(maxWidth,maxHeight)
                dataBinding.recyclerviewMedias.layoutParams.apply {
                    width = displayWidth
                    height = size[1]
                }
                try{
                    val snapHelper = PagerSnapHelper()
                    snapHelper.attachToRecyclerView(dataBinding.recyclerviewMedias)
                }catch (e:Exception){

                }
                adapters.put(position, CollectionMediaAdapter(position, carouselMedias))
                dataBinding.recyclerviewMedias.adapter = adapters[position]
//                dataBinding.recyclerviewMedias.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                    override fun onScrollStateChanged(
//                        recyclerView: RecyclerView,
//                        newState: Int
//                    ) {
//                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                            val visibleItem =
//                                (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//                            dataBinding.txtPagePosition.text = String.format(
//                                context.getString(R.string.page_position),
//                                visibleItem + 1,
//                                carouselMedias.size
//                            )
//                            if (carouselMedias[visibleItem].mediaType == InstagramConstants.MediaType.VIDEO.type &&
//                                    mPlayManager.currentPlayerId != carouselMedias[visibleItem].id) {
//                                carouselMedias[visibleItem].videoVersions[0].isPlay = true
//                                recyclerView.adapter!!.notifyDataSetChanged()
//                            }
//                        }
//                    }
//                })
            }
        }
        if (!item.isCommentThreadingEnabled) {
            btnComment.visibility = View.GONE
        } else {
            btnComment.visibility = View.VISIBLE
        }
        layoutComment.removeAllViews()
        if (item.previewComments != null && item.previewComments.isNotEmpty()) {
            visible(btnViewComments)
            btnViewComments.text =
                String.format(context.getString(R.string.view_all_comments), item.commentCount)
            btnViewComments.setOnClickListener(showCommentClickListener)
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
        btnComment.setOnClickListener(showCommentClickListener)
        btnLike.setOnClickListener {
            if (item.isHasLiked) {
                btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
                btnLike.setColorFilter(context.resources.color(R.color.white))
                item.isHasLiked = false
//                viewModel.unlikePost(item.id)
            } else {
                btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
                btnLike.setColorFilter(context.resources.color(R.color.red))
                item.isHasLiked = true
//                viewModel.likePost(item.id)
            }
        }
        return item
    }

    fun getStandardVideoSize(width: Int, height: Int): Array<Int> {
        var standardHeight = (height * displayWidth) / width
        if (standardHeight > width && standardHeight > displayHeight / 3) {
            standardHeight = (displayHeight.toFloat() / 2f).toInt()
            Log.i(InstagramConstants.DEBUG_TAG,"line 358 height: $standardHeight")
        }else{

        }
        return arrayOf(displayWidth, standardHeight)
    }

    override fun getLayoutIdForPosition(position: Int): Int {
        val item = items[position]
        return when(item.mediaType){
            InstagramConstants.MediaType.IMAGE.type -> {
                R.layout.layout_post_image
            }
            InstagramConstants.MediaType.VIDEO.type -> {
                R.layout.layout_post_video
            }
            InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                R.layout.layout_post_carousel_media
            }
            else ->{
                0
            }
        }
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