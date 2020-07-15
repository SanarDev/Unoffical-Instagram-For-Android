package com.sanardev.instagrammqtt.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.google.gson.internal.LinkedTreeMap
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.core.BaseAdapter
import com.sanardev.instagrammqtt.databinding.ActivityMainBinding
import com.sanardev.instagrammqtt.databinding.LayoutDirectBinding
import com.sanardev.instagrammqtt.datasource.model.Thread
import com.sanardev.instagrammqtt.datasource.model.event.TypingEvent
import com.sanardev.instagrammqtt.datasource.model.event.MessageEvent
import com.sanardev.instagrammqtt.datasource.model.event.PresenceEvent
import com.sanardev.instagrammqtt.datasource.model.event.UpdateSeenEvent
import com.sanardev.instagrammqtt.datasource.model.realtime.RealTime_StartService
import com.sanardev.instagrammqtt.datasource.model.response.InstagramDirects
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.extensions.color
import com.sanardev.instagrammqtt.extensions.gone
import com.sanardev.instagrammqtt.extensions.setTextViewDrawableColor
import com.sanardev.instagrammqtt.extensions.visible
import com.sanardev.instagrammqtt.service.fbns.FbnsIntent
import com.sanardev.instagrammqtt.service.realtime.RealTimeIntent
import com.sanardev.instagrammqtt.service.realtime.RealTimeService
import com.sanardev.instagrammqtt.ui.direct.DirectActivity
import com.sanardev.instagrammqtt.ui.login.LoginActivity
import com.sanardev.instagrammqtt.ui.startmessage.StartMessageActivity
import com.sanardev.instagrammqtt.utils.Resource
import com.sanardev.instagrammqtt.utils.dialog.DialogHelper
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    private val mHandler = Handler()
    private lateinit var user: InstagramLoggedUser
    var seqID: Int = 0
    lateinit var adapter: DirectsAdapter

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        EmojiManager.install(IosEmojiProvider())
        super.onCreate(savedInstanceState)

        adapter = DirectsAdapter(emptyList())
        binding.recyclerviewDirects.adapter = adapter

        val instagramDirectObserver = InstagramDirectObserver()
        viewModel.mutableLiveData.observe(this, instagramDirectObserver)
        viewModel.threadNewMessageLiveData.observe(this, Observer {
            for(index in adapter.items.indices){
                val thread = adapter.items[index]
                if(thread.threadId == it.first){
                    thread.messages.add(0,it.second)
                    adapter.notifyItemMoved(index,0)
                }
            }
        })

        user = viewModel.getUser()
        binding.txtToolbarTitle.text = getString(R.string.app_name)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val directCollectionPagerAdapter =
            DirectCollectionPagerAdapter(ArrayList<Fragment>().apply {
                add(Fragment())
                add(Fragment())
                add(Fragment())
            }, supportFragmentManager)
        binding.directViewPager.adapter = directCollectionPagerAdapter
        binding.collectionTabLayout.setupWithViewPager(binding.directViewPager)

        binding.collectionTabLayout.getTabAt(0)!!.apply {
            setIcon(R.drawable.ic_account_group)
        }
        binding.collectionTabLayout.getTabAt(1)!!.apply {
            setIcon(R.drawable.ic_online)
        }
        binding.fabStartMessage.setOnClickListener {
            StartMessageActivity.open(this@MainActivity)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class InstagramDirectObserver : Observer<Resource<InstagramDirects>> {
        override fun onChanged(it: Resource<InstagramDirects>) {

            if (it.status == Resource.Status.LOADING) {
                binding.progressbar.visibility = View.VISIBLE
                return
            }
            binding.progressbar.visibility = View.GONE
            if (it.status == Resource.Status.ERROR) {
                if (it.apiError?.code == InstagramConstants.ErrorCode.INTERNET_CONNECTION.code) {
                    DialogHelper.createDialog(
                        this@MainActivity,
                        layoutInflater,
                        title = getString(R.string.error_internet_connection),
                        message = getString(R.string.check_your_network),
                        positiveText = getString(R.string.try_again),
                        positiveFun = {
                            viewModel.getDirects()
                        }
                    )
                    return
                }
                if (it.data == null) {
                    DialogHelper.createDialog(
                        this@MainActivity,
                        layoutInflater,
                        title = getString(R.string.error),
                        message = getString(R.string.unknownError),
                        positiveText = getString(R.string.try_again),
                        positiveFun = {
                            viewModel.getDirects()
                        }
                    )
                    return
                }
                if (it.data!!.message == InstagramConstants.Error.LOGIN_REQUIRED.msg) {
                    DialogHelper.createDialog(
                        this@MainActivity,
                        layoutInflater,
                        title = it.data!!.errorTitle!!,
                        message = it.data!!.errorMessage!!,
                        positiveText = getString(R.string.login),
                        positiveFun = {
                            viewModel.resetUserData()
                            LoginActivity.open(this@MainActivity)
                            finish()
                        }
                    )
                }
                return
            }
            seqID = it.data!!.seqId
            val threads = it.data!!.inbox.threads
            RealTimeService.run(this@MainActivity,RealTime_StartService(it.data!!.seqId.toLong(),it.data!!.snapshotAtMs))
            adapter.items = threads
            adapter.notifyDataSetChanged()
        }

    }



    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) { /* Do something */
        viewModel.onMessageReceive(event)
        Log.i(InstagramConstants.DEBUG_TAG, "message event ${event.message.text}")
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateSeenEvent(event: UpdateSeenEvent) {
        viewModel.onUpdateSeenEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTypingEvent(event: TypingEvent) { /* Do something */
        viewModel.onTyping(event)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPresenceEvent(event: PresenceEvent) { /* Do something */
        viewModel.onPresenceEvent(event)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    inner class DirectsAdapter(var items: List<Thread>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            val dataBinding = holder.binding as LayoutDirectBinding
            if (item.messages != null) {
                val lastSeen =
                    item.lastSeenAt[user.pk.toString()]!!.timeStamp
                var unreadMessage = 0
                for (message in item.messages) {
                    if (message.timestamp > lastSeen) {
                        unreadMessage++
                    } else {
                        break
                    }
                }
                val lastItem = item.messages[0]
                if (item.typing) {
                    dataBinding.profileDec.text = getString(R.string.typing)
                    dataBinding.profileDec.setTextColor(Color.WHITE)
                    dataBinding.profileDec.setTypeface(null, Typeface.BOLD);
                    mHandler.postDelayed({
                        item.typing = false
                        notifyDataSetChanged()
                    }, 2000)
                } else if (unreadMessage > 1) {
                    if (unreadMessage > 9) {
                        dataBinding.profileDec.text =
                            String.format(
                                getString(R.string.more_than_nine_new_message),
                                unreadMessage
                            )
                    } else {
                        dataBinding.profileDec.text =
                            String.format(getString(R.string.new_message), unreadMessage)
                    }
                    dataBinding.profileDec.setTextColor(Color.WHITE)
                    dataBinding.profileDec.setTypeface(null, Typeface.BOLD);
                } else {
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
                                        getString(R.string.mentioned_person_in_your_story),
                                        item.users[0].username
                                    )
                                } else {
                                    dataBinding.profileDec.text =
                                        getString(R.string.mentioned_you_in_their_story)
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
                                dataBinding.profileDec.text =
                                    getString(R.string.send_a_voice_message)
                            }
                        }
                        InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                            dataBinding.profileDec.text = lastItem.videoCallEvent.description
                        }
                    }
                }


                if (item.typing) {
                    dataBinding.lastMessageTime.visibility = View.GONE
                } else {
                    if (unreadMessage >= 1) {
                        dataBinding.lastMessageTime.setTextViewDrawableColor(Color.WHITE)
                        dataBinding.lastMessageTime.setTextColor(Color.WHITE)
                        dataBinding.lastMessageTime.setTypeface(null, Typeface.BOLD);
                        dataBinding.profileDec.setTextColor(Color.WHITE)
                        dataBinding.profileDec.setTypeface(null, Typeface.BOLD);
                    } else {
                        dataBinding.lastMessageTime.setTextViewDrawableColor(Color.GRAY)
                        dataBinding.lastMessageTime.setTextColor(Color.GRAY)
                        dataBinding.lastMessageTime.setTypeface(null, Typeface.NORMAL);
                        dataBinding.profileDec.setTextColor(Color.GRAY)
                        dataBinding.profileDec.setTypeface(null, Typeface.NORMAL);
                    }
                    dataBinding.lastMessageTime.visibility = View.VISIBLE
                    dataBinding.lastMessageTime.text =
                        viewModel.convertTimeStampToData(lastItem.timestamp)
                }
            }
            if (item.isGroup) {
                dataBinding.profileName.text = String.format(
                    getString(R.string.group_name),
                    item.users[0].username,
                    item.users.size - 1
                )
            } else {
                dataBinding.profileName.text = item.threadTitle
            }
            if (item.isGroup) {
                visible(dataBinding.layoutProfileImageGroup)
                gone(dataBinding.layoutProfileImageUser)
                Picasso.get().load(item.users[1].profilePicUrl).into(dataBinding.profileImageG1)
                Picasso.get().load(item.users[0].profilePicUrl).into(dataBinding.profileImageG2)
            }else{
                gone(dataBinding.layoutProfileImageGroup)
                visible(dataBinding.layoutProfileImageUser)
                Picasso.get().load(item.users[0].profilePicUrl).into(dataBinding.profileImage)
            }
            dataBinding.profileMoreOption.setOnClickListener {
                showPopupOptions(item.threadId, dataBinding.profileMoreOption)
            }
            if (item.lastActivityAt.toString().length == 16) {
                item.lastActivityAt /= 1000
            }
            if (item.active) {
                dataBinding.profileLastActivityAt.text = getString(R.string.online)
                dataBinding.profileLastActivityAt.setTextColor(color(R.color.online_color))
                dataBinding.imgIsOnline.visibility = View.VISIBLE
            } else {
                dataBinding.profileLastActivityAt.setTextColor(Color.GRAY)
                dataBinding.profileLastActivityAt.setTypeface(null, Typeface.NORMAL);
                dataBinding.profileLastActivityAt.text = String.format(
                    getString(R.string.active_at),
                    viewModel.convertTimeStampToData(item.lastActivityAt)
                )
                dataBinding.imgIsOnline.visibility = View.INVISIBLE
            }
            dataBinding.root.setOnClickListener {
                DirectActivity.open(this@MainActivity, Bundle().apply {
                    putString("thread_id", item.threadId)
                    putString("profile_image", item.users[0].profilePicUrl)
                    putString("username", item.users[0].username)
                    putLong("last_activity_at", item.lastActivityAt)
                    putInt("seq_id", seqID)
                    if (!item.isGroup) {
                        if(item.lastSeenAt[item.users[0].pk.toString()] != null){
                            putLong(
                                "last_seen_at",
                                item.lastSeenAt[item.users[0].pk.toString()]!!.timeStamp
                            )
                        }else{
                            putLong("last_seen_at",0)
                        }
                    }
                })
            }
            Log.i(InstagramConstants.DEBUG_TAG,item.threadId)
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

    inner class DirectCollectionPagerAdapter(
        var items: List<Fragment>,
        fragmentManager: FragmentManager
    ) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        // Returns total number of pages
        override fun getCount(): Int {
            return items.size
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment {
            return items[position]
        }

        // Returns the page title for the top indicator
        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> {
                    "All"
                }
                1 -> {
                    "Online"
                }
                1 -> {
                    "Freinds $position"
                }
                1 -> {
                    "Love $position"
                }
                else -> {
                    "Custom $position"
                }
            }
        }


    }

}