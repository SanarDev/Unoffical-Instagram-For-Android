package com.sanardev.instagrammqtt.ui.startmessage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.core.BaseAdapter
import com.sanardev.instagrammqtt.databinding.ActivityStartMessageBinding
import com.sanardev.instagrammqtt.databinding.LayoutExplorerUserBinding
import com.sanardev.instagrammqtt.datasource.model.Recipients
import com.sanardev.instagrammqtt.datasource.model.Thread
import com.sanardev.instagrammqtt.ui.direct.DirectActivity
import com.sanardev.instagrammqtt.utils.Resource
import com.squareup.picasso.Picasso

class StartMessageActivity : BaseActivity<ActivityStartMessageBinding, StartMessageViewModel>() {

    private lateinit var adapter: ThreadsAdapter

    companion object {
        fun open(context: Context, bundle: Bundle? = null) {
            context.startActivity(Intent(context, StartMessageActivity::class.java).apply {
                if (bundle != null) {
                    putExtras(bundle)
                }
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

        viewModel.liveData.observe(this, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                adapter.items = it.data!!.recipients
                adapter.notifyDataSetChanged()
            }
        })
        adapter = ThreadsAdapter(emptyArray<Recipients>().toMutableList())
        binding.recyclerviewThreads.adapter = adapter
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