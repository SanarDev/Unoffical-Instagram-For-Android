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
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
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
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.customview.customtextview.HyperTextView
import com.idirect.app.customview.postsrecyclerview.PostsAdapter
import com.idirect.app.customview.postsrecyclerview.PostsAdapter2
import com.idirect.app.customview.postsrecyclerview.PostsRecyclerListener
import com.idirect.app.customview.toast.CustomToast
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
import com.idirect.app.ui.forward.ForwardBundle
import com.idirect.app.ui.main.MainActivity
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

    companion object{
        const val NAME_TAG = "posts"
    }
    @Inject
    lateinit var mGlideRequestManager: RequestManager

    @Inject
    lateinit var mPlayManager: PlayManager

    private var isMoreAvailable: Boolean = false
    private var isLoading: Boolean = false
    private lateinit var mAdapter: PostsAdapter2
    private var scrollToItemId: String? = null

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

        mAdapter = PostsAdapter2(requireContext(),
            this@PostsFragment,
            mPlayManager,
            viewLifecycleOwner,
            emptyArray<UserPost>().toMutableList())

        binding.recyclerviewPosts.adapter = mAdapter
        binding.recyclerviewPosts.mPostsRecyclerState = object:PostsRecyclerListener{
            override fun requestForLoadMore() {
                if(!isLoading && isMoreAvailable){
                    isLoading = true
                    viewModel.loadMorePosts()
                }
            }

            override fun likeComment(v: View, id: Long) {
                viewModel.likeComment(id)
            }

            override fun unlikeComment(v: View, id: Long) {
                viewModel.unlikeComment(id)
            }

            override fun unlikePost(v: View, mediaId: String) {
                viewModel.unlikePost(mediaId)
            }

            override fun likePost(v: View, mediaId: String) {
                viewModel.likePost(mediaId)
            }

            override fun shareMedia(v: View, mediaId: String, mediaType: Int) {
                val forwardBundle =  ForwardBundle(mediaId,mediaType,false)
                (requireActivity() as MainActivity).showShareWindow(forwardBundle)
            }

            override fun showComments(v: View, post: UserPost) {
                val action = NavigationMainGraphDirections.actionGlobalCommentFragment(post)
                findNavController().navigate(action)
            }

            override fun userProfile(v: View, userId: Long, username: String) {
                val userData = UserBundle().apply {
                    this.userId = userId.toString()
                    this.username = username
                }
                val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
                findNavController().navigate(action)
            }
        }

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
                        mAdapter.items = it.data!!.userPosts.toMutableList()
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
            mAdapter.setLoading(isLoading)
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
        if (data == "SeeAllLikers") {

        } else if (data.startsWith("@")) {
            val userData = UserBundle().apply {
                username = data.replace("@", "")
            }
            val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
            findNavController().navigate(action)
        } else if (data.startsWith("#")) {
            CustomToast.show(requireContext(),getString(R.string.hashtag_not_support), Toast.LENGTH_SHORT)
        } else {
            try {
                val num = java.lang.Long.parseLong(data)
                val userData = UserBundle().apply {
                    userId = num.toString()
                }
                val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
                findNavController().navigate(action)
            } catch (e: Exception) {

            }
        }
    }

    override fun getNameTag(): String {
        return NAME_TAG
    }
}