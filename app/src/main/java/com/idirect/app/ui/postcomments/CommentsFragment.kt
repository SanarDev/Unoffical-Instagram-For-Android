package com.idirect.app.ui.postcomments

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.fasterxml.jackson.core.io.NumberInput
import com.idirect.app.R
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.customview.customtextview.HyperTextView
import com.idirect.app.databinding.FragmentCommentBinding
import com.idirect.app.databinding.LayoutCommentBinding
import com.idirect.app.databinding.LayoutCommentReplyBinding
import com.idirect.app.datasource.model.Comment
import com.idirect.app.datasource.model.UserPost
import com.idirect.app.extentions.toast
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.Resource
import com.idirect.app.utils.TimeUtils
import java.lang.Long
import javax.inject.Inject

class CommentsFragment : BaseFragment<FragmentCommentBinding,CommentsViewModel>() {


    @Inject
    lateinit var mGlideRequestManager: RequestManager
    private lateinit var mAdapter: CommentAdapter
    private val onHyperTextClick = object: HyperTextView.OnHyperTextClick{
        override fun onClick(v: View, data: String) {
            if(data.startsWith("@")){
                val userData = UserBundle().apply {
                    username = data.replace("@","")
                }
                val action = CommentsFragmentDirections.actionCommentsFragmentToUserProfileFragment(userData)
                v.findNavController().navigate(action)
            }else if (data.startsWith("#")){

            }else {
                try{
                    val num = Long.parseLong(data)
                    val userData = UserBundle().apply {
                        userId = num.toString()
                    }
                    val action = CommentsFragmentDirections.actionCommentsFragmentToUserProfileFragment(userData)
                    v.findNavController().navigate(action)
                }catch (e:Exception){

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = requireArguments()
        val postData = bundle.getParcelable<UserPost>("data")!!
        viewModel.init(postData.id)

        mAdapter = CommentAdapter(emptyArray<Comment>().toMutableList())
        binding.recyclerviewComments.adapter = mAdapter

        mGlideRequestManager.load(postData.user.profilePicUrl).into(binding.imgOwnerProfile)
        binding.txtComment.setText(postData.user.username,postData.user.pk, postData.caption.text)
        binding.txtComment.mHyperTextClick = onHyperTextClick
        binding.txtPostTime.text = TimeUtils.convertTimestampToDate(requireContext(), postData.takenAt)
        binding.edtComment.hint = String.format(getString(R.string.comment_hint),viewModel.getUser().username)

        viewModel.comments.observe(viewLifecycleOwner, Observer {
            when(it.status){
                Resource.Status.LOADING -> {
                    binding.progressbar.visibility = View.VISIBLE
                    binding.recyclerviewComments.visibility = View.GONE
                }
                Resource.Status.SUCCESS ->{
                    binding.progressbar.visibility = View.GONE
                    binding.recyclerviewComments.visibility = View.VISIBLE
                    mAdapter.items = it.data!!.comments
                    mAdapter.notifyDataSetChanged()
                }
                Resource.Status.ERROR ->{
                    binding.progressbar.visibility = View.GONE
                    binding.recyclerviewComments.visibility = View.VISIBLE
                }
            }
        })
    }

    inner class CommentAdapter(var items:MutableList<Comment>):BaseAdapter(){
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            val dataBinding = holder.binding as LayoutCommentBinding
            dataBinding.txtComment.setText(item.user.username,item.user.pk,item.text)
            dataBinding.txtComment.mHyperTextClick = onHyperTextClick
            dataBinding.txtTime.text = TimeUtils.convertTimestampToDate(requireContext(),item.createdAt)
            mGlideRequestManager.load(item.user.profilePicUrl).into(dataBinding.imgProfile)

            dataBinding.layoutCommentReply.removeAllViews()
            if(item.previewChildComments != null && item.previewChildComments.isNotEmpty()){
                for(replyComment in item.previewChildComments){
                    val replyCommentBinding:LayoutCommentReplyBinding = DataBindingUtil.inflate(layoutInflater,R.layout.layout_comment_reply,null,false)
                    mGlideRequestManager.load(replyComment.user.profilePicUrl).into(replyCommentBinding.imgProfile)
                    replyCommentBinding.txtComment.setText(replyComment.user.username,replyComment.user.pk,replyComment.text)
                    replyCommentBinding.txtComment.mHyperTextClick = onHyperTextClick
                    replyCommentBinding.txtTime.text= TimeUtils.convertTimestampToDate(requireContext(),replyComment.createdAt)
                    dataBinding.layoutCommentReply.addView(replyCommentBinding.root)
                }
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.layout_comment
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }
}