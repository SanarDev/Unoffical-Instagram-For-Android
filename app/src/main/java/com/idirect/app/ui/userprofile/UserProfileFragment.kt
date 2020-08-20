package com.idirect.app.ui.userprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.customview.toast.CustomToast
import com.idirect.app.databinding.FragmentUserProfileBinding
import com.idirect.app.databinding.LayoutUserPostBinding
import com.idirect.app.datasource.model.User
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.ui.direct.DirectBundle
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import kotlinx.android.synthetic.main.fragment_direct.*
import javax.inject.Inject

class UserProfileFragment : BaseFragment<FragmentUserProfileBinding, UserProfileViewModel>() {

    companion object{
        const val NAME_TAG = "user_profile"
    }

    private var _user: User?=null
    private val user: User get() = _user!!

    @Inject
    lateinit var mGlideRequestManager: RequestManager

    private var userId: String?=null
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
        (activity as MainActivity).isHideNavigationBottom(false)
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

        viewModel.userLiveData.observe(viewLifecycleOwner, Observer {
            if(it.status == Resource.Status.LOADING){
                binding.headerProgress.visibility = View.VISIBLE
                binding.layoutHeader.visibility = View.INVISIBLE
            }
            if (it.status == Resource.Status.SUCCESS) {
                binding.headerProgress.visibility = View.INVISIBLE
                binding.layoutHeader.visibility = View.VISIBLE

                _user = it.data!!.user
                userId = user.pk.toString()
                binding.txtUsername.text = user.username
                binding.txtBio.text = user.biography
                binding.txtFullname.text = user.fullName
                binding.txtFollowersCount.text = viewModel.getStringNumber(user.followerCount)
                binding.txtFollowingCount.text = viewModel.getStringNumber(user.followingCount)
                binding.txtPostCount.text = viewModel.getStringNumber(user.mediaCount)
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
        binding.btnMessage.setOnClickListener {
            if(_user == null){
                CustomToast.show(requireContext(), "Error user null", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            val data = DirectBundle().apply {
                this.userId = user.pk
                this.profileImage = user.profilePicUrl
                this.threadTitle = user.username
                this.isGroup = false
                this.username = user.username
            }
            val action = NavigationMainGraphDirections.actionGlobalDirectFragment(data)
            findNavController().navigate(action)
        }

        binding.recyclerviewPosts.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading && isMoreAvailable) {
                    val mLayoutManager = recyclerView.layoutManager as GridLayoutManager
                    val totalItemCount = mLayoutManager.itemCount
                    if (mLayoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
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
                val action = NavigationMainGraphDirections.actionGlobalPostsFragment(
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

    override fun getNameTag(): String {
        return NAME_TAG
    }
}