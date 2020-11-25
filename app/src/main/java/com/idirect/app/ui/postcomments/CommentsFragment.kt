package com.idirect.app.ui.postcomments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.core.BaseFragment
import com.idirect.app.ui.customview.customtextview.HyperTextView
import com.idirect.app.ui.customview.loadingadapter.LoadingAdapter
import com.idirect.app.databinding.FragmentCommentBinding
import com.idirect.app.databinding.LayoutCommentBinding
import com.idirect.app.databinding.LayoutCommentReplyBinding
import com.idirect.app.extensions.color
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.Resource
import com.idirect.app.utils.TimeUtils
import com.sanardev.instagramapijava.model.media.Comment
import com.vanniktech.emoji.EmojiPopup
import java.lang.Long

class CommentsFragment : BaseFragment<FragmentCommentBinding, CommentsViewModel>() {

    companion object {
        const val NAME_TAG = "comment"
    }

    override fun getNameTag(): String {
        return NAME_TAG
    }

    private var _emojiPopup: EmojiPopup?=null
    private val emojiPopup: EmojiPopup get() = _emojiPopup!!
    private var _mGlide:RequestManager?=null
    private val mGlide:RequestManager get() = _mGlide!!

    private lateinit var mAdapter: CommentAdapter
    private var isLoading = false
    private var isMoreAvailable = false

    private val onHyperTextClick = object : HyperTextView.OnHyperTextClick {
        override fun onClick(v: View, data: String) {
            if (data.startsWith("@")) {
                val userData = UserBundle().apply {
                    username = data.replace("@", "")
                }
                val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
                findNavController().navigate(action)
            } else if (data.startsWith("#")) {

            } else {
                try {
                    val num = Long.parseLong(data)
                    val userData = UserBundle().apply {
                        userId = num
                    }
                    val action =
                        NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
                    findNavController().navigate(action)
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun getViewModelClass(): Class<CommentsViewModel> {
        return CommentsViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_comment
    }

    override fun onDestroyView() {
        _emojiPopup?.releaseMemory()
        _emojiPopup = null
        _mGlide = null
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _mGlide = Glide.with(this@CommentsFragment)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mediaId = requireArguments().getString("media_id")!!
        viewModel.init(mediaId)
        (requireActivity() as MainActivity).isHideNavigationBottom(true)
        mAdapter = CommentAdapter()
        binding.recyclerviewComments.adapter = mAdapter

        _emojiPopup =
            EmojiPopup.Builder.fromRootView(binding.root)
                .setOnEmojiPopupDismissListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_emoji)
                }.setOnEmojiPopupShownListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_keyboard_outline)
                }.build(binding.edtComment);

        binding.edtComment.setOnClickListener {
            emojiPopup.dismiss()
        }
        binding.btnEmoji.setOnClickListener {
            if (emojiPopup.isShowing) {
                emojiPopup.dismiss()
            } else {
                emojiPopup.toggle()
            }
        }

//        mGlide.load(postData.user.profilePicUrl).into(binding.imgOwnerProfile)
//        binding.txtComment.setText(postData.user.username, postData.user.pk,postData.user.isVerified, postData.caption.text)
//        binding.txtComment.mHyperTextClick = onHyperTextClick
//        binding.txtPostTime.text =
//            TimeUtils.convertTimestampToDate(requireContext(), postData.takenAt)
//        binding.edtComment.hint =
//            String.format(getString(R.string.comment_hint), viewModel.getUser().username)

        viewModel.comments.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    if (mAdapter.items.isEmpty()) {
                        binding.progressbar.visibility = View.VISIBLE
                        binding.recyclerviewComments.visibility = View.GONE
                    }
                }
                Resource.Status.SUCCESS -> {
                    isLoading = false
                    isMoreAvailable = it.data!!.hasMoreHeadloadComments
                    binding.progressbar.visibility = View.GONE
                    binding.recyclerviewComments.visibility = View.VISIBLE
                    mAdapter.items = it.data!!.comments.toMutableList()
                    mAdapter.notifyDataSetChanged()
                }
                Resource.Status.ERROR -> {
                    isLoading = false
                    binding.progressbar.visibility = View.GONE
                    binding.recyclerviewComments.visibility = View.VISIBLE
                }
            }
            mAdapter.setLoading(isLoading,binding.recyclerviewComments)
        })
        val mLayoutManager = binding.recyclerviewComments.layoutManager as LinearLayoutManager
        binding.recyclerviewComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastItemVisible = mLayoutManager.findLastCompletelyVisibleItemPosition()
                val totalItemCount = mLayoutManager.itemCount
                if (lastItemVisible > totalItemCount - 2 && !isLoading && isMoreAvailable) {
                    isLoading = true
                    viewModel.loadMoreComments()
                }
            }
        })
    }

    inner class CommentAdapter() : LoadingAdapter() {

        override fun objForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            item as Comment
            val dataBinding = holder.binding as LayoutCommentBinding
            dataBinding.txtComment.setText(item.user.username, item.user.pk,item.user.isVerified, item.text)
            dataBinding.txtComment.mHyperTextClick = onHyperTextClick
            dataBinding.txtComment.setLinkTextColor(color(R.color.hyperText))
            dataBinding.txtTime.text =
                TimeUtils.convertTimestampToDate(requireContext(), item.createdAt)
            mGlide.load(item.user.profilePicUrl).into(dataBinding.imgProfile)

            dataBinding.layoutCommentReply.removeAllViews()
            if (item.previewChildComments != null && item.previewChildComments.isNotEmpty()) {
                for (replyComment in item.previewChildComments) {
                    val replyCommentBinding: LayoutCommentReplyBinding = DataBindingUtil.inflate(
                        layoutInflater,
                        R.layout.layout_comment_reply,
                        null,
                        false
                    )
                    mGlide.load(replyComment.user.profilePicUrl)
                        .into(replyCommentBinding.imgProfile)
                    replyCommentBinding.txtComment.setText(
                        replyComment.user.username,
                        replyComment.user.pk,
                        replyComment.user.isVerified,
                        replyComment.text
                    )
                    replyCommentBinding.txtComment.setLinkTextColor(color(R.color.hyperText))
                    replyCommentBinding.txtComment.mHyperTextClick = onHyperTextClick
                    replyCommentBinding.txtTime.text =
                        TimeUtils.convertTimestampToDate(requireContext(), replyComment.createdAt)
                    if (replyComment.hasLikedComment) {
                        replyCommentBinding.btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
                        replyCommentBinding.btnLike.setColorFilter(resources.color(R.color.red))
                    } else {
                        replyCommentBinding.btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
                        replyCommentBinding.btnLike.setColorFilter(resources.color(R.color.text_light))
                    }
                    replyCommentBinding.btnLike.setOnClickListener {
                        if (replyComment.hasLikedComment) {
                            viewModel.unlikeComment(replyComment.pk)
                        } else {
                            viewModel.likeComment(replyComment.pk)
                        }
                        replyComment.hasLikedComment = !replyComment.hasLikedComment
                        notifyItemChanged(position)
                    }
                    dataBinding.layoutCommentReply.addView(replyCommentBinding.root)
                }
            }
            if (item.hasLikedComment) {
                dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_filled_24)
                dataBinding.btnLike.setColorFilter(resources.color(R.color.red))
            } else {
                dataBinding.btnLike.setImageResource(R.drawable.instagram_heart_outline_24)
                dataBinding.btnLike.setColorFilter(resources.color(R.color.text_light))
            }
            dataBinding.btnLike.setOnClickListener {
                if (item.hasLikedComment) {
                    viewModel.unlikeComment(item.pk)
                } else {
                    viewModel.likeComment(item.pk)
                }
                item.hasLikedComment = !item.hasLikedComment
                notifyItemChanged(position)
            }
            return item
        }

        override fun layoutIdForPosition(position: Int): Int {
            return R.layout.layout_comment
        }

    }
}