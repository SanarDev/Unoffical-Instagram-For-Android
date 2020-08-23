package com.idirect.app.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentSearchBinding
import com.idirect.app.databinding.FragmentStoryBinding
import com.idirect.app.databinding.LayoutExplorerUserBinding
import com.idirect.app.datasource.model.Recipients
import com.idirect.app.datasource.model.event.ConnectionStateEvent
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.extentions.hideKeyboard
import com.idirect.app.ui.main.ShareViewModel
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.Resource
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FragmentSearch : BaseFragment<FragmentSearchBinding, SearchViewModel>() {

    private lateinit var shareViewModel: ShareViewModel
    private var seqId: Int = 0
    private var _mAdapter: ThreadsAdapter?=null
    private val mAdapter: ThreadsAdapter get() = _mAdapter!!

    companion object {
        fun open(context: Context, seqId: Int) {
            context.startActivity(Intent(context, FragmentSearch::class.java).apply {
                putExtra("seq_id",seqId)
            })
        }

        const val TAG = "TEST"
        const val NAME_TAG = "explore"
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_search
    }

    override fun onDestroyView() {
        _mAdapter = null
        super.onDestroyView()
    }

    override fun getViewModelClass(): Class<SearchViewModel> {
        return SearchViewModel::class.java
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shareViewModel = ViewModelProvider(requireActivity()).get(ShareViewModel::class.java)
        seqId = shareViewModel.instagramDirect!!.seqId
        viewModel.liveData.observe(viewLifecycleOwner, Observer {
            when(it.status){
                Resource.Status.LOADING ->{
                    visible(binding.progressbar)
                    gone(binding.recyclerviewThreads,binding.includeLayoutNetwork.root,binding.txtPageNotFound)
                }
                Resource.Status.ERROR -> {
                    visible(binding.includeLayoutNetwork.root)
                    gone(binding.recyclerviewThreads,binding.progressbar)
                }
                Resource.Status.SUCCESS ->{
                    if(it.data!!.recipients.size == 0){
                        visible(binding.txtPageNotFound)
                    }
                    gone(binding.includeLayoutNetwork.root,binding.progressbar)
                    visible(binding.recyclerviewThreads)
                    mAdapter.items = it.data!!.recipients
                    mAdapter.notifyDataSetChanged()
                }
            }
        })
        _mAdapter = ThreadsAdapter(emptyArray<Recipients>().toMutableList())
        binding.recyclerviewThreads.adapter = mAdapter
        binding.recyclerviewThreads.adapter = mAdapter
//        waitForTransition(binding.recyclerviewThreads)

        binding.edtSearch.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s.toString())
            }
        })
    }

    inner class ThreadsAdapter(var items: MutableList<Recipients>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            val thread = item.thread
            val dataBinding = holder.binding as LayoutExplorerUserBinding
            val user = if(thread != null){
                thread.users[0]
            }else{
                item.user
            }

//            ViewCompat.setTransitionName(dataBinding.imgProfileImage,"profile_${user.pk}")
//            ViewCompat.setTransitionName(dataBinding.txtUsername,"username_${user.pk}")
//            ViewCompat.setTransitionName(dataBinding.txtFullname,"fullname_${user.pk}")
            dataBinding.txtFullname.text = user.fullName
            dataBinding.txtUsername.setText(user.username,user.pk,user.isVerified)
            Glide.with(requireContext()).load(user.profilePicUrl).into(dataBinding.imgProfileImage)
            dataBinding.root.setOnClickListener {
                val userData = UserBundle().apply {
                    this.userId = user.pk
                    this.profilePic = user.profilePicUrl
                    this.username = user.username
                    this.fullname = user.fullName
                    this.isVerified = user.isVerified
                }
                val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
//                val extras = FragmentNavigatorExtras(
//                    dataBinding.imgProfileImage to dataBinding.imgProfileImage.transitionName,
//                    dataBinding.txtUsername to dataBinding.txtUsername.transitionName,
//                    dataBinding.txtFullname to dataBinding.txtFullname.transitionName
//                )
                findNavController().navigate(action)
                requireActivity().hideKeyboard()
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.layout_explorer_user
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }

    override fun getNameTag(): String {
        return NAME_TAG
    }
}