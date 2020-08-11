package com.idirect.app.ui.inbox

import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentInboxBinding
import com.idirect.app.databinding.LayoutDirectBinding
import com.idirect.app.datasource.model.Thread
import com.idirect.app.datasource.model.event.*
import com.idirect.app.datasource.model.response.InstagramDirects
import com.idirect.app.datasource.model.response.InstagramLoggedUser
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.setTextViewDrawableColor
import com.idirect.app.extensions.visible
import com.idirect.app.extentions.color
import com.idirect.app.ui.login.LoginActivity
import com.idirect.app.ui.main.ShareViewModel
import com.idirect.app.ui.startmessage.StartMessageFragment
import com.idirect.app.utils.Resource
import com.idirect.app.utils.TimeUtils
import com.idirect.app.utils.dialog.DialogHelper
import com.idirect.app.utils.dialog.DialogListener
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import javax.inject.Inject


class FragmentInbox : BaseFragment<FragmentInboxBinding, InboxViewModel>() {
    override fun layoutRes(): Int {
        return R.layout.fragment_inbox
    }

    override fun getViewModelClass(): Class<InboxViewModel> {
        return InboxViewModel::class.java
    }

    @Inject
    lateinit var mGlideRequestManager:RequestManager

    private lateinit var shareViewModel: ShareViewModel
    private lateinit var layoutManager: LinearLayoutManager
    private var isLoadingMoreDirects: Boolean = false
    private var isMoreDirectExist: Boolean = true
    private val mHandler = Handler()
    private lateinit var user: InstagramLoggedUser
    lateinit var adapter: DirectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EmojiManager.install(IosEmojiProvider())
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DirectsAdapter(emptyList<Any>().toMutableList())
        binding.recyclerviewDirects.adapter = adapter
        layoutManager = (binding.recyclerviewDirects.layoutManager as LinearLayoutManager)
        shareViewModel = ViewModelProvider(requireActivity()).get(ShareViewModel::class.java)

        shareViewModel.mutableLiveData.observe(viewLifecycleOwner, InstagramDirectObserver())

        shareViewModel.threadNewMessageLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                return@Observer
            }
            for (index in adapter.items.indices) {
                val thread = adapter.items[index]
                if (thread is Thread && thread.threadId == it.first) {
                    thread.messages.add(0, it.second)
                    adapter.notifyItemMoved(index, 0)
                    break
                }
            }
        })
        shareViewModel.threadMessageRemoved.observe(viewLifecycleOwner, Observer {
            if(it == null){
                return@Observer
            }
            for (index in adapter.items.indices) {
                val thread = adapter.items[index]
                if (thread is Thread && thread.threadId == it.threadId) {
                    adapter.items[index] = shareViewModel.getThreadById(it.threadId)!!
                    adapter.notifyItemChanged(index)
                    break
                }
            }
        })
        shareViewModel.threadsPresence.observe(viewLifecycleOwner, Observer {
            adapter.items = shareViewModel.instagramDirect!!.inbox.threads.toMutableList()
            adapter.notifyDataSetChanged()
        })

        shareViewModel.threadChange.observe(viewLifecycleOwner, Observer {
            for (index in adapter.items.indices) {
                val thread = adapter.items[index]
                if (thread is Thread && thread.threadId == it) {
                    adapter.items[index] = shareViewModel.getThreadById(it)!!
                    adapter.notifyItemChanged(index)
                    break
                }
            }
        })

        shareViewModel.connectionState.observe(viewLifecycleOwner, Observer {
            when(it.connection){
                ConnectionStateEvent.State.CONNECTED ->{
                    binding.txtToolbarTitle.text = getString(R.string.connected)
                }
                ConnectionStateEvent.State.CONNECTING ->{
                    binding.txtToolbarTitle.text = getString(R.string.connecting)
                }
                ConnectionStateEvent.State.NETWORK_DISCONNECTED ->{
                    binding.txtToolbarTitle.text = getString(R.string.waiting_for_network)
                }
                else ->{
                    binding.txtToolbarTitle.text = getString(R.string.connecting)
                }
            }
        })

        user = shareViewModel.getUser()


        binding.recyclerviewDirects.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoadingMoreDirects && isMoreDirectExist) {
                    val totalItemCount = layoutManager.itemCount
                    if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
                        if (totalItemCount - 2 < 0) {
                            return
                        }
                        shareViewModel.loadMoreItem()
                        isLoadingMoreDirects = true
                        adapter.setLoading(isLoadingMoreDirects)
                    }
                }
            }
        })

    }

//    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        super.onPostCreate(savedInstanceState, persistentState)
//        toggle.syncState()
//    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        toggle.onConfigurationChanged(newConfig)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (toggle.onOptionsItemSelected(item)) {
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private inner class InstagramDirectObserver : Observer<Resource<InstagramDirects>> {
        override fun onChanged(it: Resource<InstagramDirects>) {

            when (it.status) {
                Resource.Status.LOADING -> {
                    if (!isLoadingMoreDirects && adapter.items.isEmpty()) {
                        visible(binding.progressbar)
                        gone(
                            binding.recyclerviewDirects,
                            binding.txtNoDirect,
                            binding.includeLayoutNetwork.root
                        )
                    }
                }
                Resource.Status.ERROR -> {
                    gone(binding.progressbar, binding.includeLayoutNetwork.root)
                    if (it.apiError?.code == InstagramConstants.ErrorCode.INTERNET_CONNECTION.code) {
                        if (adapter.items.isEmpty()) {
                            visible(binding.includeLayoutNetwork.root)
                        }
                        return
                    }
                    if (it.data!!.message == InstagramConstants.Error.LOGIN_REQUIRED.msg) {
                        DialogHelper.createDialog(
                            context!!,
                            layoutInflater,
                            title = it.data!!.errorTitle!!,
                            message = it.data!!.errorMessage!!,
                            positiveText = getString(R.string.login),
                            positiveListener = object : DialogListener.Positive {
                                override fun onPositiveClick() {
                                    shareViewModel.resetUserData()
                                    LoginActivity.open(context!!)
                                    activity!!.finish()
                                }
                            }
                        )
                    }
                }
                Resource.Status.SUCCESS -> {
                    gone(binding.progressbar, binding.includeLayoutNetwork.root)
                    visible(binding.recyclerviewDirects)
                    isLoadingMoreDirects = false
                    if (it.data!!.inbox.oldestCursor == null) {
                        isMoreDirectExist = false
                    }
                    adapter.setLoading(isLoadingMoreDirects)
                    val threads = it.data!!.inbox.threads
                    if (threads.isEmpty()) {
                        binding.txtNoDirect.visibility = View.VISIBLE
                    } else {
                        binding.txtNoDirect.visibility = View.GONE
                    }
                    adapter.items = threads.toMutableList()
                    adapter.notifyDataSetChanged()
                }
            }
        }

    }


    inner class DirectsAdapter(var items: MutableList<Any>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]

            if (item is LoadingEvent) {
                return item
            }
            item as Thread
            val dataBinding = holder.binding as LayoutDirectBinding
            if (item.messages != null) {
                var unreadMessage = 0
                if (item.lastSeenAt != null) {
                    item.lastSeenAt[user.pk.toString()].also {
                        if (it != null) {
                            for (message in item.messages) {
                                if (message.timestamp > it.timeStamp) {
                                    if (message.userId != user.pk) {
                                        unreadMessage++
                                    }
                                } else {
                                    break
                                }
                            }
                        }
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
                    val prefix = if (lastItem.userId == user.pk) {
                        "You: "
                    } else {
                        if (item.isGroup) {
                            shareViewModel.getUsernameByUserId(
                                item.threadId,
                                lastItem.userId
                            ) + ": "
                        } else {
                            ""
                        }
                    }
                    when (lastItem.itemType) {
                        InstagramConstants.MessageType.ACTION_LOG.type -> {
                            dataBinding.profileDec.text = lastItem.actionLog.description
                        }
                        InstagramConstants.MessageType.TEXT.type -> {
                            dataBinding.profileDec.text = prefix + lastItem.text
                        }
                        InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                            dataBinding.profileDec.text =
                                prefix + getString(R.string.send_a_sticker)
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
                            if (lastItem.reelShare.type == InstagramConstants.ReelType.REACTION.type) {
                                if (lastItem.userId == user.pk) {
                                    dataBinding.profileDec.text = String.format(
                                        getString(R.string.you_reacted_to_user_story),
                                        item.users[0].username
                                    )
                                } else {
                                    dataBinding.profileDec.text = String.format(
                                        getString(R.string.reacted_to_your_story_with_reaction),
                                        lastItem.reelShare.text
                                    )
                                }
                            }
                        }
                        InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                            dataBinding.profileDec.text = prefix + getString(R.string.share_a_media)
                        }
                        InstagramConstants.MessageType.MEDIA.type -> {
                            dataBinding.profileDec.text = prefix + getString(R.string.send_a_media)
                        }
                        InstagramConstants.MessageType.LIKE.type -> {
                            dataBinding.profileDec.text = prefix + lastItem.like
                        }
                        InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                            dataBinding.profileDec.text = prefix + getString(R.string.send_a_photo)
                        }
                        InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                            dataBinding.profileDec.text = prefix +
                                    getString(R.string.send_a_voice_message)
                        }
                        InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                            dataBinding.profileDec.text =
                                prefix + lastItem.videoCallEvent.description
                        }
                        InstagramConstants.MessageType.LINK.type -> {
                            dataBinding.profileDec.text = prefix +
                                    getString(R.string.share_a_link)
                        }
                        InstagramConstants.MessageType.FELIX_SHARE.type -> {
                            dataBinding.profileDec.text = prefix + String.format(
                                getString(R.string.send_user_igtv_video),
                                lastItem.felixShare.video.user.username
                            )
                        }
                        InstagramConstants.MessageType.STORY_SHARE.type -> {
                            dataBinding.profileDec.text = prefix + getString(R.string.share_story)
                        }
                        InstagramConstants.MessageType.PROFILE.type -> {
                            dataBinding.profileDec.text =
                                prefix + getString(R.string.send_a_profile)
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
                        TimeUtils.convertTimestampToDate(context!!, lastItem.timestamp)
                }
            }
            if (item.isGroup && item.threadTitle == null) {
                if (item.users.size >= 2) {
                    dataBinding.profileName.text = String.format(
                        getString(R.string.group_name),
                        item.users[0].username,
                        item.users.size - 1
                    )
                } else {
                    dataBinding.profileName.text = item.users[0].username
                }
            } else {
                dataBinding.profileName.text = item.threadTitle
            }
            if (item.isGroup) {
                visible(dataBinding.layoutProfileImageGroup)
                gone(dataBinding.layoutProfileImageUser)
                if (item.users.size >= 2) {
                    mGlideRequestManager.load(item.users[1].profilePicUrl)
                        .into(dataBinding.profileImageG1)
                    mGlideRequestManager.load(item.users[0].profilePicUrl)
                        .into(dataBinding.profileImageG2)
                } else {
                    mGlideRequestManager.load(user.profilePicUrl)
                        .into(dataBinding.profileImageG1)
                    mGlideRequestManager.load(item.users[0].profilePicUrl)
                        .into(dataBinding.profileImageG2)
                }
            } else {
                gone(dataBinding.layoutProfileImageGroup)
                visible(dataBinding.layoutProfileImageUser)
                mGlideRequestManager.load(item.users[0].profilePicUrl)
                    .into(dataBinding.profileImage)
            }
            dataBinding.profileMoreOption.setOnClickListener {
                showPopupOptions(item.threadId, dataBinding.profileMoreOption)
            }
            if (item.lastActivityAt.toString().length == 16) {
                item.lastActivityAt /= 1000
            }
            if (item.active) {
                dataBinding.profileLastActivityAt.text = getString(R.string.online)
                dataBinding.profileLastActivityAt.setTextColor(context!!.color(R.color.online_color))
                dataBinding.imgIsOnline.visibility = View.VISIBLE
            } else {
                dataBinding.profileLastActivityAt.setTextColor(Color.GRAY)
                dataBinding.profileLastActivityAt.setTypeface(null, Typeface.NORMAL);
                dataBinding.profileLastActivityAt.text = String.format(
                    getString(R.string.active_at),
                    TimeUtils.convertTimestampToDate(context!!, item.lastActivityAt)
                )
                dataBinding.imgIsOnline.visibility = View.INVISIBLE
            }
            dataBinding.root.setOnClickListener {
                shareViewModel.currentThread = shareViewModel.getThreadById(item.threadId)
                shareViewModel.currentThreadId = shareViewModel.currentThread!!.threadId
                Navigation.findNavController(it)
                    .navigate(R.id.action_fragmentInbox_to_fragmentDirect)
                if (binding.edtSearch.text.toString().isNotEmpty()) {
                    binding.edtSearch.setText("")
                }
            }
//            dataBinding.root.setOnLongClickListener{
//                showPopupOptions(item.threadId,dataBinding.root)
//                return@setOnLongClickListener true
//            }
            return item
        }

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                items.add(LoadingEvent())
                notifyItemInserted(items.size - 1)
                binding.recyclerviewDirects.scrollToPosition(items.size - 1)
            } else {
                for (i in items.indices) {
                    if (items[i] is LoadingEvent) {
                        items.removeAt(i)
                        notifyItemRemoved(i)
                    }
                }
            }
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            if (items[position] is LoadingEvent) {
                return R.layout.layout_loading
            }
            return R.layout.layout_direct
        }

        override fun getItemCount(): Int {
            return items.size
        }

        private fun showPopupOptions(threadId: String, view: View) {
            val popupWindow = PopupWindow(context!!)
            popupWindow.isOutsideTouchable = true
            popupWindow.isFocusable = true
            val layoutDirectOptionBinding: ViewDataBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.layout_direct_option, null, false)
            popupWindow.contentView = layoutDirectOptionBinding.root
            val location = locateView(view)
            popupWindow.showAtLocation(
                view,
                Gravity.CENTER,
                location!!.right,
                location.bottom
            )
        }

        fun locateView(v: View?): Rect? {
            val loc_int = IntArray(2)
            if (v == null) return null
            try {
                v.getLocationOnScreen(loc_int)
            } catch (npe: NullPointerException) {
                //Happens when the view doesn't exist on screen anymore.
                return null
            }
            val location = Rect()
            location.left = loc_int[0]
            location.top = loc_int[1]
            location.right = location.left + v.width
            location.bottom = location.top + v.height
            return location
        }
    }

}