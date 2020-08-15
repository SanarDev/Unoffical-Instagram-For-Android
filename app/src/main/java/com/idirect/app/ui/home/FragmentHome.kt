package com.idirect.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.R
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.customview.customtextview.HyperTextView
import com.idirect.app.customview.postsrecyclerview.PostsAdapter2
import com.idirect.app.customview.postsrecyclerview.PostsRecyclerListener
import com.idirect.app.databinding.FragmentHomeBinding
import com.idirect.app.databinding.LayoutStoryBinding
import com.idirect.app.datasource.model.Story
import com.idirect.app.datasource.model.Tray
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.manager.PlayManager
import com.idirect.app.ui.postcomments.CommentsFragmentDirections
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import java.lang.Long
import javax.inject.Inject

class FragmentHome : BaseFragment<FragmentHomeBinding, HomeViewModel>(),
    HyperTextView.OnHyperTextClick {


    companion object {
        const val NAME_TAG = "home"
    }

    private lateinit var mStoryAdapter: StoriesAdapter

    @Inject
    lateinit var mGlideRequestManager: RequestManager

    @Inject
    lateinit var mPlayManager: PlayManager

    private var currentMediaPosition: Int = PlayManager.NONE
    private lateinit var mAdapter: PostsAdapter2
    private lateinit var dataSource: DataSource.Factory
    private lateinit var mLayoutManager: LinearLayoutManager

    val displayWidth = DisplayUtils.getScreenWidth()
    val displayHeight = DisplayUtils.getScreenHeight()
    private var isLoading = false
    private var lastStoryClicked: kotlin.Long = 0
    private var v: View? = null

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

        mAdapter = PostsAdapter2(
            requireContext(),
            /* hyperListener */ this@FragmentHome,
            mPlayManager,
            viewLifecycleOwner,
            emptyArray<UserPost>().toMutableList()
        )

        binding.recyclerviewPosts.adapter = mAdapter
        binding.recyclerviewPosts.mPostsRecyclerState = object : PostsRecyclerListener {
            override fun requestForLoadMore() {
                if (!isLoading) {
                    isLoading = true
                    viewModel.loadMorePosts()
                }
            }

            override fun likeComment(id: kotlin.Long) {

            }

            override fun unlikeComment(id: kotlin.Long) {
            }

            override fun unlikePost(mediaId: String) {
            }

            override fun likePost(mediaId: String) {
            }
        }
        mLayoutManager = binding.recyclerviewPosts.layoutManager as LinearLayoutManager
        viewModel.postsLiveData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                isLoading = false
                mAdapter.items = it.data!!.posts
                mAdapter.notifyDataSetChanged()
            }
        })

        mStoryAdapter = StoriesAdapter(null)
        binding.recyclerviewStories.adapter = mStoryAdapter
        viewModel.storiesLiveData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                mStoryAdapter.items = it.data!!.tray
                mStoryAdapter.notifyDataSetChanged()
            }
        })

        viewModel.storyMediaLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null && it.status == Resource.Status.SUCCESS) {
                val action =
                    FragmentHomeDirections.actionFragmentHomeToFragmentStory(lastStoryClicked.toString())
                v?.findNavController()?.navigate(action)
                viewModel.storyMediaLiveData.value = null
            }
        })
        binding.navigationBottom.btnInbox.setOnClickListener {
            val action = FragmentHomeDirections.actionFragmentHomeToFragmentInbox()
            it.findNavController().navigate(action)
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

    inner class StoriesAdapter(var items: MutableList<Tray>?) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items!![position]
            val dataBinding = holder.binding as LayoutStoryBinding
            dataBinding.txtUsername.text = item.user.username
            mGlideRequestManager.load(item.user.profilePicUrl).into(dataBinding.imgProfile)
            dataBinding.root.setOnClickListener {
                v = it
                viewModel.getStoryMedia(item.user.pk)
                lastStoryClicked = item.user.pk
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.layout_story
        }

        override fun getItemCount(): Int {
            return if (items == null) 0 else items!!.size
        }
    }

    override fun onClick(v: View, data: String) {
        if (data == "SeeAllLikers") {

        } else if (data.startsWith("@")) {
            val userData = UserBundle().apply {
                username = data.replace("@", "")
            }
        } else if (data.startsWith("#")) {

        } else {
            try {
                val num = Long.parseLong(data)
                val userData = UserBundle().apply {
                    userId = num.toString()
                }
                val action =
                    CommentsFragmentDirections.actionCommentsFragmentToUserProfileFragment(userData)
                v.findNavController().navigate(action)
            } catch (e: Exception) {

            }
        }
    }

    override fun getNameTag(): String {
        return NAME_TAG
    }

}