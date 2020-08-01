package com.idirect.app.ui.startmessage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.idirect.app.R
import com.idirect.app.core.BaseActivity
import com.idirect.app.core.BaseAdapter
import com.idirect.app.databinding.ActivityStartMessageBinding
import com.idirect.app.databinding.LayoutExplorerUserBinding
import com.idirect.app.datasource.model.Recipients
import com.idirect.app.datasource.model.event.ConnectionStateEvent
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.visible
import com.idirect.app.ui.direct.DirectActivity
import com.idirect.app.ui.direct.DirectBundle
import com.idirect.app.utils.Resource
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class StartMessageActivity : BaseActivity<ActivityStartMessageBinding, StartMessageViewModel>() {

    private var seqId: Int = 0
    private lateinit var adapter: ThreadsAdapter

    companion object {
        fun open(context: Context, seqId: Int) {
            context.startActivity(Intent(context, StartMessageActivity::class.java).apply {
                putExtra("seq_id",seqId)
            })
        }

        const val TAG = "TEST"
    }

    override fun layoutRes(): Int {
        return R.layout.activity_start_message
    }

    override fun getViewModelClass(): Class<StartMessageViewModel> {
        return StartMessageViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        seqId = intent.extras!!.getInt("seq_id")
        viewModel.liveData.observe(this, Observer {
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
                    adapter.items = it.data!!.recipients
                    adapter.notifyDataSetChanged()
                }
            }
        })
        adapter = ThreadsAdapter(emptyArray<Recipients>().toMutableList())
        binding.recyclerviewThreads.adapter = adapter
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
            dataBinding.txtFullname.text = user.fullName
            dataBinding.txtUsername.text = user.username
            Glide.with(applicationContext).load(user.profilePicUrl).into(dataBinding.imgProfileImage)
            dataBinding.root.setOnClickListener {
                if(item.thread != null){
                    DirectActivity.open(this@StartMessageActivity, DirectBundle().apply {
                        val thread = item.thread!!
                        this.threadId = thread.threadId
                        this.lastActivityAt = thread.lastActivityAt
                        this.isGroup = thread.isGroup
                        this.profileImage = thread.users[0].profilePicUrl
                        this.seqId = seqId
                        this.username = thread.users[0].username
                        this.threadTitle = username
                    })
                }else{
                    DirectActivity.open(this@StartMessageActivity, DirectBundle().apply {
                        val user = item.user!!
                        this.profileImage = user.profilePicUrl
                        this.seqId = seqId
                        this.username = user.username
                        this.name = user.fullName
                        this.userId = user.pk!!
                        this.threadTitle = username
                        this.name = user.fullName
                    })
                }
                finish()
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
}