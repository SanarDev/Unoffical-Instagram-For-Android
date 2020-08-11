package com.idirect.app.ui.home

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
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
import com.idirect.app.core.BaseFragment
import com.idirect.app.customview.customtextview.HyperTextView
import com.idirect.app.databinding.FragmentHomeBinding
import com.idirect.app.databinding.LayoutCarouselImageBinding
import com.idirect.app.databinding.LayoutCarouselVideoBinding
import com.idirect.app.databinding.LayoutUserDetailPostBinding
import com.idirect.app.datasource.model.CarouselMedia
import com.idirect.app.datasource.model.FeedItem
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.extensions.color
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.extentions.dpToPx
import com.idirect.app.manager.PlayManager
import com.idirect.app.ui.posts.PostsFragmentDirections
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.tylersuehr.chips.CircleImageView
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import javax.inject.Inject

class FragmentHome : BaseFragment<FragmentHomeBinding, HomeViewModel>(),
    HyperTextView.OnHyperTextClick {


    @Inject
    lateinit var mGlideRequestManager: RequestManager

    @Inject
    lateinit var mPlayManager: PlayManager

    private var currentMediaPosition: Int = PlayManager.NONE
    private lateinit var mAdapter: FragmentHome.PostsAdapter
    private lateinit var dataSource: DataSource.Factory
    private lateinit var mLayoutManager: LinearLayoutManager

    val displayWidth = DisplayUtils.getScreenWidth()
    val displayHeight = DisplayUtils.getScreenHeight()

    override fun getViewModelClass(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_home
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EmojiManager.install(IosEmojiProvider())
        dataSource = DefaultHttpDataSourceFactory(Util.getUserAgent(requireContext(), "Instagram"))
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = PostsAdapter(emptyArray<FeedItem>().toMutableList())
        binding.recyclerviewPosts.adapter = mAdapter
        mLayoutManager = binding.recyclerviewPosts.layoutManager as LinearLayoutManager
        viewModel.postsLiveData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                mAdapter.items = it.data!!.feedItems
                mAdapter.notifyDataSetChanged()
            }
        })


        binding.recyclerviewPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val positionFirstItem = mLayoutManager.findFirstCompletelyVisibleItemPosition()
                if (positionFirstItem != -1 &&
                    currentMediaPosition != PlayManager.NONE &&
                    positionFirstItem != currentMediaPosition
                ) {
                    currentMediaPosition = PlayManager.NONE
                    mPlayManager.stopPlay()
                    return
                }
//                if (!isLoading && isMoreAvailable) {
//                    val totalItemCount = mLayoutManager.itemCount
//                    if (mLayoutManager != null && mLayoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
//                        isLoading = true
//                        viewModel.loadMorePosts()
//                    }
//                }
            }
        })
    }


    inner class PostsAdapter(var items: MutableList<FeedItem>) : BaseAdapter() {

        val adapters = HashMap<Int, FragmentHome.CollectionMediaAdapter>()
        override fun onViewRecycled(holder: BaseViewHolder) {
            super.onViewRecycled(holder)

            adapters.remove(holder.adapterPosition)
        }

        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position].mediaOrAd
            val dataBinding = holder.binding as LayoutUserDetailPostBinding
            dataBinding.layoutLikersProfile.removeAllViews()
            val showLikersClickListener = View.OnClickListener {

            }
            val showCommentClickListener = View.OnClickListener {

            }

            dataBinding.txtLocationName.visibility = View.GONE
            visible(dataBinding.txtLikesCount)
            gone(dataBinding.layoutLikes)
            dataBinding.txtLikesCount.text =
                String.format(getString(R.string.liker_count), item.likeCount)
            dataBinding.txtLikesCount.setOnClickListener(showLikersClickListener)
            dataBinding.txtUsername.text = item.user.username
            if (item.caption != null) {
                dataBinding.txtCaption.setText(
                    item.caption.user.username,
                    item.caption.user.pk,
                    item.caption.text
                )
                dataBinding.txtCaption.mHyperTextClick = this@FragmentHome
            }
            mGlideRequestManager.load(item.user.profilePicUrl).into(dataBinding.imgProfile)
            dataBinding.layoutMedia.removeAllViews()
            when (item.mediaType) {
                InstagramConstants.MediaType.IMAGE.type -> {
                    val photoView = AppCompatImageView(requireContext())
                    photoView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    photoView.adjustViewBounds = true
                    mGlideRequestManager
                        .load(item.imageVersions2.candidates[0].url)
                        .placeholder(R.drawable.post_load_place_holder)
                        .into(photoView)
                    dataBinding.layoutMedia.addView(photoView)
                }
                InstagramConstants.MediaType.VIDEO.type -> {
                    val video = item.videoVersions[0]!!
                    val previewImage = item.imageVersions2.candidates[0]!!
                    val videoLayout: View =
                        layoutInflater.inflate(R.layout.layout_post_video, null, false)
                    val videoView: PlayerView = videoLayout.findViewById(R.id.video_view)
                    val layoutParent: FrameLayout = videoLayout.findViewById(R.id.layout_parent)
                    val photoView: AppCompatImageView =
                        videoLayout.findViewById(R.id.photo_view)
                    val imgPlay: AppCompatImageView = videoLayout.findViewById(R.id.img_play)
                    val size = viewModel.getStandardVideoSize(video.width, video.height)
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

                        mGlideRequestManager.load(previewImage.url).centerCrop().into(photoView)
                    }
                    dataBinding.layoutMedia.setOnClickListener {
                        video.isPlay = true
                        mAdapter.notifyItemChanged(position)
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
                        layoutInflater.inflate(R.layout.layout_post_carousel_media, null, false)
                    val pagePosition =
                        itemView.findViewById<AppCompatTextView>(R.id.txt_page_position)
                    val recyclerview = itemView.findViewById<RecyclerView>(R.id.view_pager)
                    val carouselMedias = item.carouselMedias
                    pagePosition.text =
                        String.format(getString(R.string.page_position), 1, carouselMedias.size)
                    val size = viewModel.getStandardVideoSize(carouselMedias[0].originalWidth,carouselMedias[0].originalHeight)
                    recyclerview.layoutParams.apply {
                        width = size[0]
                        height = size[1]
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
                                    getString(R.string.page_position),
                                    visibleItem + 1,
                                    carouselMedias.size
                                )
                                if (carouselMedias[visibleItem].mediaType == InstagramConstants.MediaType.VIDEO.type &&
                                        mPlayManager.currentPlayerId != carouselMedias[visibleItem].id) {
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
            if (item.isHasLiked) {
                dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
                dataBinding.btnLike.setColorFilter(color(R.color.red))
            } else {
                dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
                dataBinding.btnLike.setColorFilter(color(R.color.white))
            }
            dataBinding.btnComment.setOnClickListener(showCommentClickListener)
            dataBinding.btnLike.setOnClickListener {
                if (item.isHasLiked) {
                    dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
                    dataBinding.btnLike.setColorFilter(color(R.color.white))
                    item.isHasLiked = false
                    viewModel.unlikePost(item.id)
                } else {
                    dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
                    dataBinding.btnLike.setColorFilter(color(R.color.red))
                    item.isHasLiked = true
                    viewModel.likePost(item.id)
                }
            }
            dataBinding.layoutComment.visibility = View.GONE
            dataBinding.btnViewComments.visibility = View.GONE
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.layout_user_detail_post
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }


    inner class CollectionMediaAdapter constructor(
        var positionOfAdapter: Int,
        var items: List<CarouselMedia>
    ) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            if (item.mediaType == InstagramConstants.MediaType.IMAGE.type) {
                val dataBinding = holder.binding as LayoutCarouselImageBinding
                mGlideRequestManager.load(item.imageVersions2.candidates[0].url)
                    .into(dataBinding.imgPhoto)
            } else {
                val dataBinding = holder.binding as LayoutCarouselVideoBinding
                dataBinding.videoView.visibility = View.GONE
                val video = item.videoVersions[0]
                val image = item.imageVersions2.candidates[0]
                mGlideRequestManager
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

                mGlideRequestManager.load(item.imageVersions2.candidates[0].url)
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

    override fun onStop() {
        super.onStop()
        mPlayManager.stopPlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayManager.releasePlay()
    }

    override fun onClick(v: View, data: String) {
        if (data.startsWith("@")) {

        }
    }

}