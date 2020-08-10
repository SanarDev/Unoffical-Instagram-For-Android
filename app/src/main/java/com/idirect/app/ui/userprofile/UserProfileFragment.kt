package com.idirect.app.ui.userprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentUserProfileBinding
import com.idirect.app.databinding.LayoutUserPostBinding
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import javax.inject.Inject

class UserProfileFragment : BaseFragment<FragmentUserProfileBinding, UserProfileViewModel>() {

    @Inject
    lateinit var mGlideRequestManager: RequestManager

    private var userId: String?=null
    private lateinit var mLayoutManager: GridLayoutManager
    private var isMoreAvailable: Boolean = false
    private var isLoading: Boolean = false

    override fun getViewModelClass(): Class<UserProfileViewModel> {
        return UserProfileViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_user_profile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EmojiManager.install(IosEmojiProvider())
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    val size = DisplayUtils.getScreenWidth()
    val picStandardSize = size / 3
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = requireArguments()
        val userBundle = bundle.getParcelable<UserBundle>("user_data")!!
        userId = userBundle.userId
        val profileImageUrl = userBundle.profilePic
        val username = userBundle.username
        val fullname = userBundle.fullname

        binding.txtUsername.text = username
        binding.txtFullname.text = fullname
        mGlideRequestManager.load(profileImageUrl).into(binding.imgProfile)

        ViewCompat.setTransitionName(binding.imgProfile, "profile_${userId}")
        ViewCompat.setTransitionName(binding.txtUsername, "username_${userId}")
        ViewCompat.setTransitionName(binding.txtFullname, "fullname_${userId}")

        viewModel.init(username,userId)

        val mAdapter = PostsAdapter(emptyArray<UserPost>().toMutableList())
        binding.recyclerviewPosts.adapter = mAdapter
        mLayoutManager = binding.recyclerviewPosts.layoutManager as GridLayoutManager

        viewModel.userLiveData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                val user = it.data!!.user
                userId = user.pk.toString()
                binding.txtUsername.text = user.username
                binding.txtBio.text = user.biography
                binding.txtFullname.text = user.fullName
                binding.txtFollowersCount.text = user.followerCount.toString()
                binding.txtFollowingCount.text = user.followingCount.toString()
                binding.txtPostCount.text = user.mediaCount.toString()
                if(user.hdProfilePicVersions != null && user.hdProfilePicVersions.isNotEmpty()){
                    mGlideRequestManager.load(user.hdProfilePicVersions[0].url)
                        .into(binding.imgProfile)
                }else{
                    mGlideRequestManager.load(user.profilePicUrl)
                        .into(binding.imgProfile)
                }
            }
        })
        viewModel.resultUsePosts.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    if (mAdapter.items.isEmpty()) {
                        visible(binding.postsProgress)
                        gone(binding.recyclerviewPosts)
                    }
                }

                Resource.Status.ERROR ->{
                    if(it.apiError!!.message == InstagramConstants.Error.NOT_AUTHORIZED_VIEW_USER.msg){
                        gone(binding.postsProgress)
                        visible(binding.layoutPrivatePage)
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
                }
            }
        })

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.recyclerviewPosts.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading && isMoreAvailable) {
                    val totalItemCount = mLayoutManager.itemCount
                    if (mLayoutManager != null && mLayoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
                        viewModel.loadMorePosts()
                        isLoading = true
                    }
                }
            }
        })
    }

    inner class PostsAdapter(var items: MutableList<UserPost>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            val dataBinding = holder.binding as LayoutUserPostBinding
            when (item.mediaType) {
                InstagramConstants.MediaType.IMAGE.type -> {
                    dataBinding.imgPostType.setImageDrawable(null)
                    mGlideRequestManager
                        .load(item.imageVersions2.candidates[1].url)
                        .override(picStandardSize,picStandardSize)
                        .centerCrop()
                        .placeholder(R.drawable.post_load_place_holder)
                        .into(dataBinding.imgPost)
                }
                InstagramConstants.MediaType.VIDEO.type -> {
                    dataBinding.imgPostType.setImageDrawable(requireContext().getDrawable(R.drawable.ic_play))
                    mGlideRequestManager
                        .load(item.imageVersions2.candidates[1].url)
                        .override(picStandardSize,picStandardSize)
                        .centerCrop()
                        .placeholder(R.drawable.post_load_place_holder)
                        .into(dataBinding.imgPost)
                }
                InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                    dataBinding.imgPostType.setImageDrawable(requireContext().getDrawable(R.drawable.ic_collection))
                    mGlideRequestManager
                        .load(item.carouselMedias[0].imageVersions2.candidates[1].url)
                        .override(picStandardSize,picStandardSize)
                        .centerCrop()
                        .placeholder(R.drawable.post_load_place_holder)
                        .into(dataBinding.imgPost)
                }
            }
            dataBinding.root.setOnClickListener {
                val action = UserProfileFragmentDirections.actionUserProfileFragmentToPostsFragment(
                    userId = userId!!,
                    scrollToItemId = item.id
                )
                it.findNavController().navigate(action)
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.layout_user_post
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }
}