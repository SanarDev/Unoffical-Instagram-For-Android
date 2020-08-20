package com.idirect.app.ui.startmessage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.ActivityStartMessageBinding
import com.idirect.app.databinding.LayoutExplorerUserBinding
import com.idirect.app.datasource.model.Recipients
import com.idirect.app.datasource.model.event.ConnectionStateEvent
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.extensions.waitForTransition
import com.idirect.app.ui.main.ShareViewModel
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.Resource
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class StartMessageFragment : BaseFragment<ActivityStartMessageBinding, StartMessageViewModel>() {

    private lateinit var shareViewModel: ShareViewModel
    private var seqId: Int = 0
    private lateinit var mAdapter: ThreadsAdapter

    companion object {
        fun open(context: Context, seqId: Int) {
            context.startActivity(Intent(context, StartMessageFragment::class.java).apply {
                putExtra("seq_id",seqId)
            })
        }

        const val TAG = "TEST"
        const val NAME_TAG = "explore"
    }

    override fun layoutRes(): Int {
        return R.layout.activity_start_message
    }

    override fun getViewModelClass(): Class<StartMessageViewModel> {
        return StartMessageViewModel::class.java
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
        mAdapter = ThreadsAdapter(emptyArray<Recipients>().toMutableList())
        binding.recyclerviewThreads.adapter = mAdapter
        binding.recyclerviewThreads.adapter = mAdapter
//        waitForTransition(binding.recyclerviewThreads)
    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onConnectionStateEvent(connectionStateEvent: ConnectionStateEvent){
        when (connectionStateEvent.connection){
            ConnectionStateEvent.State.NETWORK_CONNECTION_RESET ->{
                viewModel.getRecipients(binding.edtSearch.text.toString())
            }
            else ->{

            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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

            ViewCompat.setTransitionName(dataBinding.imgProfileImage,"profile_${user.pk}")
            ViewCompat.setTransitionName(dataBinding.txtUsername,"username_${user.pk}")
            ViewCompat.setTransitionName(dataBinding.txtFullname,"fullname_${user.pk}")
            dataBinding.txtFullname.text = user.fullName
            dataBinding.txtUsername.text = user.username
            Glide.with(requireContext()).load(user.profilePicUrl).into(dataBinding.imgProfileImage)
            dataBinding.root.setOnClickListener {
                val userData = UserBundle().apply {
                    this.userId = user.pk.toString()
                    this.profilePic = user.profilePicUrl
                    this.username = user.username
                    this.fullname = user.fullName
                }
                val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
                val extras = FragmentNavigatorExtras(
                    dataBinding.imgProfileImage to dataBinding.imgProfileImage.transitionName,
                    dataBinding.txtUsername to dataBinding.txtUsername.transitionName,
                    dataBinding.txtFullname to dataBinding.txtFullname.transitionName
                )
                findNavController().navigate(action,extras)
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