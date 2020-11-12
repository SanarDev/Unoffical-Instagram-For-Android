package com.idirect.app.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.customview.customtextview.HyperTextView
import com.idirect.app.customview.postsrecyclerview.PostsAdapter2
import com.idirect.app.customview.postsrecyclerview.PostsAdapter3
import com.idirect.app.customview.postsrecyclerview.PostsRecyclerListener
import com.idirect.app.customview.story.StoryWidget
import com.idirect.app.customview.toast.CustomToast
import com.idirect.app.databinding.FragmentHomeBinding
import com.idirect.app.databinding.LayoutStoryBinding
import com.idirect.app.datasource.model.*
import com.idirect.app.manager.PlayManager
import com.idirect.app.ui.forward.ForwardBundle
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.ui.postcomments.CommentsFragmentDirections
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.model.timeline.MediaOrAd
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import java.lang.Long
import javax.inject.Inject

class FragmentHome : BaseFragment<FragmentHomeBinding, HomeViewModel>(),
    HyperTextView.OnHyperTextClick {


    companion object {
        const val NAME_TAG = "home"
    }


    @Inject
    lateinit var mPlayManager: PlayManager

    private var currentMediaPosition: Int = PlayManager.NONE

    private var _mStoryAdapter: StoriesAdapter? = null
    private val mStoryAdapter: StoriesAdapter get() = _mStoryAdapter!!
    private var _mAdapter: PostsAdapter2? = null
    private val mAdapter: PostsAdapter2 get() = _mAdapter!!
    private var _mGlide: RequestManager? = null
    private val mGlide: RequestManager get() = _mGlide!!

    private lateinit var dataSource: DataSource.Factory

    private var isLoading = false
    private var lastStoryClicked: kotlin.Long = 0


    override fun getViewModelClass(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_home
    }

    override fun onDestroyView() {
        _mAdapter = null
        _mStoryAdapter = null
        _mGlide = null
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EmojiManager.install(IosEmojiProvider())
        dataSource = DefaultHttpDataSourceFactory(Util.getUserAgent(requireContext(), "Instagram"))
        _mGlide = Glide.with(this@FragmentHome)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPlayManager.setRepeat(true)
        _mAdapter = PostsAdapter2(
            requireContext(),
            /* hyperListener */ this@FragmentHome,
            mPlayManager,
            mGlide,
            viewLifecycleOwner
        )
        (requireActivity() as MainActivity).isHideNavigationBottom(false)

        binding.recyclerviewPosts.adapter = mAdapter
        binding.recyclerviewPosts.mPostsRecyclerState = object : PostsRecyclerListener {
            override fun requestForLoadMore() {
                Log.i(
                    InstagramConstants.DEBUG_TAG,
                    "request for load more with isLoading $isLoading"
                )
                if (!isLoading) {
                    isLoading = true
                    viewModel.loadMorePosts()
                }
            }

            override fun likeComment(v: View, id: kotlin.Long) {
                viewModel.likeComment(id)
            }

            override fun unlikeComment(v: View, id: kotlin.Long) {
                viewModel.unlikeComment(id)
            }

            override fun unlikePost(v: View, mediaId: String) {
                viewModel.unlikePost(mediaId)
            }

            override fun likePost(v: View, mediaId: String) {
                viewModel.likePost(mediaId)
            }

            override fun shareMedia(v: View, mediaId: String, mediaType: Int) {
                val forwardBundle = ForwardBundle(mediaId, mediaType, false)
                (requireActivity() as MainActivity).showShareWindow(forwardBundle)
            }

            override fun showComments(v: View, post: MediaOrAd) {
                val action = NavigationMainGraphDirections.actionGlobalCommentFragment(post.id)
                findNavController().navigate(action)
            }

            override fun userProfile(v: View, userId: kotlin.Long, username: String) {
                val action =
                    NavigationMainGraphDirections.actionGlobalUserProfileFragment(UserBundle().apply {
                        this.username = username
                        this.userId = userId
                    })
                findNavController().navigate(action)
            }

            override fun onLocationClick(v: View, location: Location) {
                CustomToast.show(
                    requireContext(),
                    getString(R.string.location_not_support),
                    Toast.LENGTH_SHORT
                )
            }
        }
        viewModel.postsLiveData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                isLoading = false
                mAdapter.items = (it.data!!.feedItems.toMutableList())
            }
            mAdapter.setLoading(isLoading)
        })

        _mStoryAdapter = StoriesAdapter(null)
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
                    NavigationMainGraphDirections.actionGlobalStoryFragment(
                        lastStoryClicked.toString(),
                        false
                    )
                findNavController().navigate(action)
                viewModel.storyMediaLiveData.value = null
            }
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

    override fun onDestroy() {
        super.onDestroy()
        mPlayManager.releasePlay()
    }

    inner class StoriesAdapter(var items: MutableList<com.sanardev.instagramapijava.model.story.Tray>?) :
        RecyclerView.Adapter<StoriesAdapter.StoriesViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
            val parentLayout = LinearLayout(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
            }
            return StoriesViewHolder(parentLayout)
        }

        override fun getItemCount(): Int {
            return if (items == null) 0 else items!!.size
        }

        override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
            val item = items!![position]
            holder.storyWidget.setProfilePic(mGlide, item.user.profilePicUrl)
            holder.storyWidget.setUsername(item.user.username)
            holder.storyWidget.setStatus(item.latestReelMedia, item.hasBestiesMedia, item.seen)
            if(lastStoryClicked == item.user.pk){
                holder.storyWidget.setLoading(true)
            }else{
                holder.storyWidget.setLoading(false)
            }
            holder.storyWidget.setOnClickListener {
                viewModel.getStoryMedia(item.user.pk)
                lastStoryClicked = item.user.pk
                notifyDataSetChanged()
            }
        }

        inner class StoriesViewHolder(v: ViewGroup) : RecyclerView.ViewHolder(v) {
            val storyWidget: StoryWidget

            init {
                storyWidget = StoryWidget(v.context)
                storyWidget.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                v.addView(storyWidget)
            }
        }
    }
//    inner class StoriesAdapter(var items: MutableList<com.sanardev.instagramapijava.model.story.Tray>?) : BaseAdapter() {
//        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
//            val item = items!![position]
//            val dataBinding = holder.binding as LayoutStoryBinding
//            dataBinding.txtUsername.text = item.user.username
//            mGlide.load(item.user.profilePicUrl).into(dataBinding.imgProfile)
//            if(item.seen == 0.toLong() || item.latestReelMedia > item.seen){
//                if(item.hasBestiesMedia){
//                    dataBinding.imgProfile.setBackgroundResource(R.drawable.bg_close_friend_story)
//                }else{
//                    dataBinding.imgProfile.setBackgroundResource(R.drawable.bg_new_story)
//                }
//            }else{
//                dataBinding.imgProfile.setBackgroundResource(R.drawable.bg_story)
//            }
//            dataBinding.root.setOnClickListener {
//                viewModel.getStoryMedia(item.user.pk)
//                lastStoryClicked = item.user.pk
//            }
//            return item
//        }
//
//        override fun getLayoutIdForPosition(position: Int): Int {
//            return R.layout.layout_story
//        }
//
//        override fun getItemCount(): Int {
//            return if (items == null) 0 else items!!.size
//        }
//    }

    override fun onClick(v: View, data: String) {
        if (data == "SeeAllLikers") {

        } else if (data.startsWith("@")) {
            val userData = UserBundle().apply {
                username = data.replace("@", "")
            }
            val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
            findNavController().navigate(action)
        } else if (data.startsWith("#")) {
            CustomToast.show(
                requireContext(),
                getString(R.string.hashtag_not_support),
                Toast.LENGTH_SHORT
            )
        } else {
            try {
                val num = Long.parseLong(data)
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