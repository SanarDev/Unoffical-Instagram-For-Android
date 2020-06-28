package com.sanardev.instagrammqtt.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.core.BaseAdapter
import com.sanardev.instagrammqtt.databinding.ActivityMainBinding
import com.sanardev.instagrammqtt.databinding.LayoutDirectBinding
import com.sanardev.instagrammqtt.datasource.model.Thread
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.service.fbns.FbnsIntent
import com.sanardev.instagrammqtt.service.realtime.RealTimeIntent
import com.sanardev.instagrammqtt.ui.direct.DirectActivity
import com.sanardev.instagrammqtt.ui.login.LoginActivity
import com.sanardev.instagrammqtt.utils.Resource
import com.sanardev.instagrammqtt.utils.dialog.DialogHelper
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import java.util.*


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }


    val serverUri = "ssl://mqtt-mini.facebook.com:443"
    var clientId = UUID.randomUUID().toString().substring(0,20)

    private lateinit var user: InstagramLoggedUser
    var seqID: Int = 0
    lateinit var adapter: DirectsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        EmojiManager.install(IosEmojiProvider())
        super.onCreate(savedInstanceState)
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
                if (it.apiError?.code == InstagramConstants.ErrorCode.INTERNET_CONNECTION.code) {
                    DialogHelper.createDialog(
                        this,
                        layoutInflater,
                        title = getString(R.string.error_internet_connection),
                        message = getString(R.string.check_your_network),
                        positiveText = getString(R.string.try_again),
                        positiveFun = {
                            viewModel.getDirects()
                        }
                    )
                    return@Observer
                }
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
            startService(Intent(RealTimeIntent.ACTION_CONNECT_SESSION).setPackage("com.sanardev.instagrammqtt")
                .putExtra("seq_id",it.data!!.seqId.toLong())
                .putExtra("snap_shot_at",it.data!!.snapshotAtMs))
            adapter.items = threads
            adapter.notifyDataSetChanged()
        })
        user = viewModel.getUser()
        binding.txtToolbarTitle.text = user.username
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(Exception::class)
    fun connect(protogle: String) {
        startService(Intent(FbnsIntent.ACTION_CONNECT_SESSION).setPackage("com.sanardev.instagrammqtt"));
    }

    inner class DirectsAdapter(var items: List<Thread>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            val dataBinding = holder.binding as LayoutDirectBinding
            if (item.messages != null) {
                val lastItem = item.messages[0]
                when (lastItem.itemType) {
                    InstagramConstants.MessageType.TEXT.type -> {
                        dataBinding.profileDec.text = lastItem.text
                    }
                    InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                        dataBinding.profileDec.text = getString(R.string.send_a_sticker)
                    }
                    InstagramConstants.MessageType.REEL_SHARE.type -> {
                        if (lastItem.reelShare.type == InstagramConstants.ReelType.REPLY.type) {
                            if (lastItem.userId == user.pk) {
                                dataBinding.profileDec.text =
                                    getString(R.string.reply_to_their_story)
                            } else {
                                dataBinding.profileDec.text =
                                    getString(R.string.reply_to_your_story)
                            }
                        }

                        if (lastItem.reelShare.type == InstagramConstants.ReelType.MENTION.type) {
                            if (lastItem.userId == user.pk) {
                                dataBinding.profileDec.text = String.format(
                                    getString(R.string.mentioned_person_in_your_story), item.users[0].username
                                )
                            }else{
                                dataBinding.profileDec.text = getString(R.string.mentioned_you_in_their_story)
                            }
                        }
                    }
                    InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                        dataBinding.profileDec.text = getString(R.string.share_a_media)
                    }
                    InstagramConstants.MessageType.LIKE.type -> {
                        dataBinding.profileDec.text = lastItem.like
                    }
                    InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                        if (lastItem.userId == user.pk) {
                            dataBinding.profileDec.text =
                                getString(R.string.you_send_a_voice_message)
                        } else {
                            dataBinding.profileDec.text = getString(R.string.send_a_voice_message)
                        }
                    }
                    InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                        dataBinding.profileDec.text = lastItem.videoCallEvent.description
                    }
                }
            }
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