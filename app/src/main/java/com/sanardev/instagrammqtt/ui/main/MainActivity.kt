package com.sanardev.instagrammqtt.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hovans.android.global.GlobalAppHolder
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.core.BaseAdapter
import com.sanardev.instagrammqtt.databinding.ActivityMainBinding
import com.sanardev.instagrammqtt.databinding.LayoutDirectBinding
import com.sanardev.instagrammqtt.datasource.model.Message
import com.sanardev.instagrammqtt.datasource.model.Thread
import com.sanardev.instagrammqtt.mqtt.service.NettyIntent
import com.sanardev.instagrammqtt.ui.direct.DirectActivity
import com.sanardev.instagrammqtt.ui.login.LoginActivity
import com.sanardev.instagrammqtt.utils.Resource
import com.sanardev.instagrammqtt.utils.dialog.DialogHelper
import com.squareup.picasso.Picasso


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    var seqID: Int = 0
    lateinit var adapter: DirectsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalAppHolder.get().init(application)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connect("")
        }


        adapter = DirectsAdapter(emptyList())
        binding.recyclerviewDirects.adapter = adapter

        viewModel.liveData.observe(this, Observer {
            if (it.status == Resource.Status.LOADING) {
                binding.progressbar.visibility = View.VISIBLE
                return@Observer
            }
            binding.progressbar.visibility = View.GONE
            if (it.status == Resource.Status.ERROR) {
                if (it.data == null) {
                    DialogHelper.createDialog(
                        this,
                        layoutInflater,
                        title = getString(R.string.error),
                        message = getString(R.string.unknownError),
                        positiveText = getString(R.string.try_again),
                        positiveFun = {
                            viewModel.getDirects()
                        }
                    )
                    return@Observer
                }
                if (it.data!!.message == InstagramConstants.Error.LOGIN_REQUIRED.msg) {
                    DialogHelper.createDialog(
                        this,
                        layoutInflater,
                        title = it.data!!.errorTitle!!,
                        message = it.data!!.errorMessage!!,
                        positiveText = getString(R.string.login),
                        positiveFun = {
                            viewModel.resetUserData()
                            LoginActivity.open(this)
                            finish()
                        }
                    )
                }
                return@Observer
            }
            seqID = it.data!!.seqId
            val threads = it.data!!.inbox.threads
            adapter.items = threads
            adapter.notifyDataSetChanged()
        })

    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(Exception::class)
    fun connect(protogle: String) {
        val client = MqttClient.builder()
            .useMqttVersion3()
            .serverHost("mqtt-mini.facebook.com")
            .serverPort(443)
            .sslWithDefaultConfig()
            .buildAsync()
            .connect()
            .whenComplete { connAck, throwable-> if (throwable != null) {
                Log.i("TEST","TEST")
            } else {
                Log.i("TEST","TEST")
            } }
//        startService(Intent(NettyIntent.ACTION_CONNECT_SESSION).setPackage("com.sanardev.instagrammqtt"));
    }

    inner class DirectsAdapter(var items: List<Thread>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            val dataBinding = holder.binding as LayoutDirectBinding
//            if (item.messages != null) {
//                val lastItem = item.messages[item.messages.size - 1]
//                if (lastItem.itemType == "text") {
//                    dataBinding.profileDec.text = lastItem.text
//                }
//            }
            dataBinding.profileName.text = item.threadTitle
            dataBinding.profileLastActivityAt.text =
                viewModel.convertTimeStampToData(item.lastActivityAt)
            if (!item.isGroup) {
                Picasso.get().load(item.users[0].profilePicUrl).into(dataBinding.profileImage)
            }
            dataBinding.profileMoreOption.setOnClickListener {
                showPopupOptions(item.threadId, dataBinding.profileMoreOption)
            }
            dataBinding.root.setOnClickListener {
                DirectActivity.open(this@MainActivity, Bundle().apply {
                    putString("thread_id", item.threadId)
                    putString("profile_image", item.users[0].profilePicUrl)
                    putString("username", item.users[0].username)
                    putLong("last_activity_at", item.lastActivityAt)
                    putInt("seq_id", seqID)
                })
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.layout_direct
        }

        override fun getItemCount(): Int {
            return items.size
        }

        private fun showPopupOptions(threadId: String, bottomOfView: View) {
            val popupWindow = PopupWindow(this@MainActivity)
            popupWindow.isOutsideTouchable = true
            popupWindow.isFocusable = true
            val layoutDirectOptionBinding: ViewDataBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.layout_direct_option, null, false)
            popupWindow.contentView = layoutDirectOptionBinding.root
            popupWindow.showAsDropDown(bottomOfView)
        }

    }
}