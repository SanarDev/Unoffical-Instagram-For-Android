package com.idirect.app.ui.singlepost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.idirect.app.R
import com.idirect.app.core.BaseFragment
import com.idirect.app.ui.customview.customtextview.HyperTextView
import com.idirect.app.ui.customview.postsrecyclerview.PostsAdapter2
import com.idirect.app.ui.customview.postsrecyclerview.PostsRecyclerListener
import com.idirect.app.databinding.FragmentSinglePostBinding
import com.idirect.app.datasource.model.Location
import com.idirect.app.manager.PlayManager
import com.sanardev.instagramapijava.model.timeline.MediaOrAd
import javax.inject.Inject

class FragmentSinglePost : BaseFragment<FragmentSinglePostBinding,SinglePostViewModel>(){

    private lateinit var mGlide: RequestManager

    @Inject
    lateinit var mPlayManager:PlayManager

    override fun getViewModelClass(): Class<SinglePostViewModel> {
        return SinglePostViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_single_post
    }

    override fun getNameTag(): String {
        return "fragment_single_post"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mGlide = Glide.with(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mediaId = requireArguments().getString("mediaId")!!
        val mAdapter = PostsAdapter2(requireContext(),object:HyperTextView.OnHyperTextClick{
            override fun onClick(v: View, url: String) {

            }
        },mPlayManager,mGlide,viewLifecycleOwner)
        mAdapter.mListener = object : PostsRecyclerListener{
            override fun requestForLoadMore() {

            }

            override fun likeComment(v: View, id: Long) {

            }

            override fun unlikeComment(v: View, id: Long) {

            }

            override fun unlikePost(v: View, mediaId: String) {

            }

            override fun likePost(v: View, mediaId: String) {

            }

            override fun shareMedia(v: View, mediaId: String, mediaType: Int) {

            }

            override fun showComments(v: View, post: MediaOrAd) {

            }

            override fun userProfile(v: View, userId: Long, username: String) {

            }

            override fun onLocationClick(v: View, location: Location) {

            }
        }
        mAdapter.captionLenghtLimit = HyperTextView.FLAG_NO_LIMIT
        binding.recyclerviewPosts.adapter = mAdapter
        viewModel.getMediaPost(mediaId)
        viewModel.mediaPost.observe(viewLifecycleOwner, Observer {
            mAdapter.items = it.toMutableList()
            mAdapter.notifyDataSetChanged()
        })
    }

}