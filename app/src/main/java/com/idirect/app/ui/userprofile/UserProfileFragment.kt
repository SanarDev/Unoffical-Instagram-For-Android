package com.idirect.app.ui.userprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentUserProfileBinding
import com.idirect.app.databinding.LayoutUserPostBinding
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.utils.Resource
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider

class UserProfileFragment : BaseFragment<FragmentUserProfileBinding, UserProfileViewModel>() {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = requireArguments()
        val userId = bundle.getString("user_id")!!
        val profileImageUrl = bundle.getString("profile_url")!!
        val username = bundle.getString("username")!!
        val fullname = bundle.getString("fullname")!!

        binding.txtUsername.text = username
        binding.txtFullname.text = fullname
        Glide.with(requireContext()).load(profileImageUrl).into(binding.imgProfile)

        ViewCompat.setTransitionName(binding.imgProfile, "profile_${userId}")
        ViewCompat.setTransitionName(binding.txtUsername, "username_${userId}")
        ViewCompat.setTransitionName(binding.txtFullname, "fullname_${userId}")

        viewModel.init(userId.toLong())

        val mAdapter = PostsAdapter(emptyArray<UserPost>().toMutableList())
        binding.recyclerviewPosts.adapter = mAdapter
        mLayoutManager = binding.recyclerviewPosts.layoutManager as GridLayoutManager

        viewModel.userLiveData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                val user = it.data!!.user
                binding.txtUsername.text = user.username
                binding.txtBio.text = user.biography
                binding.txtFullname.text = user.fullName
                binding.txtFollowersCount.text = user.followerCount.toString()
                binding.txtFollowingCount.text = user.followingCount.toString()
                binding.txtPostCount.text = user.mediaCount.toString()
                Glide.with(requireContext()).load(user.hdProfilePicVersions[0].url)
                    .into(binding.imgProfile)
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

        binding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY >= v.getChildAt(v.childCount - 1)
                        .measuredHeight - v.measuredHeight &&
                    scrollY > oldScrollY
                ) {
                    if (!isLoading && isMoreAvailable) {
                        val totalItemCount = mLayoutManager.itemCount
                        if (mLayoutManager != null && mLayoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
                            viewModel.loadMorePosts()
                            isLoading = true
                        }
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
                    Glide.with(context!!).load(item.imageVersions2.candidates[1].url).centerCrop()
                        .placeholder(R.drawable.post_load_place_holder).into(dataBinding.imgPost)
                }
                InstagramConstants.MediaType.VIDEO.type -> {
                    dataBinding.imgPostType.setImageDrawable(requireContext().getDrawable(R.drawable.ic_play))
                    Glide.with(context!!).load(item.imageVersions2.candidates[1].url).centerCrop()
                        .placeholder(R.drawable.post_load_place_holder).into(dataBinding.imgPost)
                }
                InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                    dataBinding.imgPostType.setImageDrawable(requireContext().getDrawable(R.drawable.ic_collection))
                    Glide.with(context!!)
                        .load(item.carouselMedias[0].imageVersions2.candidates[1].url).centerCrop()
                        .placeholder(R.drawable.post_load_place_holder).into(dataBinding.imgPost)
                }
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