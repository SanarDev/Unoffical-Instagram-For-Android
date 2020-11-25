package com.idirect.app.ui.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.core.BaseFragment
import com.idirect.app.ui.customview.customtextview.HyperTextView
import com.idirect.app.ui.customview.postsrecyclerview.PostsAdapter2
import com.idirect.app.ui.customview.postsrecyclerview.PostsRecyclerListener
import com.idirect.app.ui.customview.toast.CustomToast
import com.idirect.app.databinding.FragmentPostsBinding
import com.idirect.app.datasource.model.Location
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.manager.PlayManager
import com.idirect.app.ui.forward.ForwardBundle
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.model.timeline.MediaOrAd
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import javax.inject.Inject

class PostsFragment : BaseFragment<FragmentPostsBinding, PostsViewModel>(),
    HyperTextView.OnHyperTextClick {

    companion object{
        const val NAME_TAG = "posts"
    }

    @Inject
    lateinit var mPlayManager: PlayManager

    private var isMoreAvailable: Boolean = false
    private var isLoading: Boolean = false
    private var _mAdapter: PostsAdapter2?=null
    private val mAdapter: PostsAdapter2 get() = _mAdapter!!
    private var scrollToItemId: String? = null

    private var _mGlide:RequestManager?=null
    private val mGlide:RequestManager get() = _mGlide!!

    override fun getViewModelClass(): Class<PostsViewModel> {
        return PostsViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_posts
    }

    override fun onDestroyView() {
        _mAdapter = null
        _mGlide = null
        super.onDestroyView()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EmojiManager.install(IosEmojiProvider())
        _mGlide = Glide.with(this@PostsFragment)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = requireArguments()
        val userId = bundle.getString("user_id")!!
        scrollToItemId = bundle.getString("scroll_to_item_id")
        bundle.remove("scroll_to_item_id")
        viewModel.init(userId)

        _mAdapter = PostsAdapter2(requireContext(),
            this@PostsFragment,
            mPlayManager,
            mGlide,
            viewLifecycleOwner)

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

            override fun showComments(v: View, post: MediaOrAd) {
                val action = NavigationMainGraphDirections.actionGlobalCommentFragment(post.id)
                findNavController().navigate(action)
            }

            override fun userProfile(v: View, userId: Long, username: String) {
                val userData = UserBundle().apply {
                    this.userId = userId
                    this.username = username
                }
                val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
                findNavController().navigate(action)
            }

            override fun onLocationClick(v: View, location: Location) {
                CustomToast.show(requireContext(),getString(R.string.location_not_support),Toast.LENGTH_SHORT)
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
                        mAdapter.items = it.data!!.posts.toMutableList()
                        mAdapter.notifyDataSetChanged()
                    }
                    if (scrollToItemId != null) {
                        for (index in it.data!!.posts.indices) {
                            val item = it.data!!.posts[index]
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


    override fun onPause() {
        super.onPause()
        mPlayManager.pausePlay()
    }

    override fun onResume() {
        super.onResume()
        mPlayManager.resumePlay()
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
                    userId = num
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