package com.idirect.app.ui.posts

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.SimpleExoPlayer
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
import com.idirect.app.customview.postsrecyclerview.PostsAdapter
import com.idirect.app.databinding.FragmentPostsBinding
import com.idirect.app.databinding.LayoutCarouselImageBinding
import com.idirect.app.databinding.LayoutCarouselVideoBinding
import com.idirect.app.databinding.LayoutUserDetailPostBinding
import com.idirect.app.datasource.model.CarouselMedia
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.extensions.color
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.extentions.dpToPx
import com.idirect.app.extentions.toast
import com.idirect.app.manager.PlayManager
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.tylersuehr.chips.CircleImageView
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiTextView
import com.vanniktech.emoji.ios.IosEmojiProvider
import javax.inject.Inject

class PostsFragment : BaseFragment<FragmentPostsBinding, PostsViewModel>(),
    HyperTextView.OnHyperTextClick {

    @Inject
    lateinit var mGlideRequestManager: RequestManager

    @Inject
    lateinit var mPlayManager: PlayManager

    private lateinit var mLayoutManager: LinearLayoutManager
    private var isMoreAvailable: Boolean = false
    private var isLoading: Boolean = false
    private lateinit var mAdapter: PostsAdapter
    private var scrollToItemId: String? = null
    private var currentMediaPosition: Int = -1
    private var currentVideoLayout: View? = null
    val displayWidth = DisplayUtils.getScreenWidth()
    val displayHeight = DisplayUtils.getScreenHeight()

    override fun getViewModelClass(): Class<PostsViewModel> {
        return PostsViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_posts
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EmojiManager.install(IosEmojiProvider())
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = requireArguments()
        val userId = bundle.getString("user_id")!!
        scrollToItemId = bundle.getString("scroll_to_item_id")
        bundle.remove("scroll_to_item_id")
        viewModel.init(userId)

        mAdapter = PostsAdapter(requireContext(),
            this@PostsFragment,
            mPlayManager,
            viewLifecycleOwner,
            emptyArray<UserPost>().toMutableList())

        mLayoutManager = binding.recyclerviewPosts.layoutManager as LinearLayoutManager
        binding.recyclerviewPosts.adapter = mAdapter

        viewModel.resultUsePosts.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    if (mAdapter.items.isEmpty()) {
                        visible(binding.postsProgress)
                        gone(binding.recyclerviewPosts)
                    }
                }
                Resource.Status.SUCCESS -> {
                    gone(binding.postsProgress)
                    visible(binding.recyclerviewPosts)
                    isLoading = false
                    isMoreAvailable = it.data!!.isMoreAvailable
                    if (it.data!!.numResults > 0) {
                        mAdapter.items = it.data!!.userPosts
                        mAdapter.notifyDataSetChanged()
                    }
                    if (scrollToItemId != null) {
                        for (index in it.data!!.userPosts.indices) {
                            val item = it.data!!.userPosts[index]
                            if (item.id == scrollToItemId) {
                                binding.recyclerviewPosts.scrollToPosition(index)
                                scrollToItemId = null
                                break
                            }
                        }
                    }
                }
            }
        })
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
            val userData = UserBundle().apply {
                username = data.replace("@", "")
            }
            val action = PostsFragmentDirections.actionPostsFragmentToUserProfileFragment(userData)
            v.findNavController().navigate(action)
        }
    }
}