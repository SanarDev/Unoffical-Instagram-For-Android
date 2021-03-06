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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentInboxBinding
import com.idirect.app.databinding.LayoutDirectBinding
import com.idirect.app.datasource.model.event.*
import com.idirect.app.extensions.gone
import com.idirect.app.extensions.setTextViewDrawableColor
import com.idirect.app.extensions.visible
import com.idirect.app.extentions.color
import com.idirect.app.ui.direct.DirectBundle
import com.idirect.app.ui.login.LoginActivity
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.ui.main.ShareViewModel
import com.idirect.app.utils.Resource
import com.idirect.app.utils.TimeUtils
import com.idirect.app.utils.dialog.DialogHelper
import com.idirect.app.utils.dialog.DialogListener
import com.sanardev.instagramapijava.model.direct.IGThread
import com.sanardev.instagramapijava.model.login.IGLoggedUser
import com.sanardev.instagramapijava.response.IGDirectsResponse
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider


class FragmentInbox : BaseFragment<FragmentInboxBinding, InboxViewModel>() {

    companion object{
        const val NAME_TAG = "inbox"
    }
    override fun getNameTag(): String {
        return NAME_TAG
    }
    override fun layoutRes(): Int {
        return R.layout.fragment_inbox
    }

    override fun getViewModelClass(): Class<InboxViewModel> {
        return InboxViewModel::class.java
    }

    private lateinit var shareViewModel: ShareViewModel
    private var isLoadingMoreDirects: Boolean = false
    private var isMoreDirectExist: Boolean = true
    private val mHandler = Handler()
    private lateinit var user: IGLoggedUser

    var _adapter: DirectsAdapter?=null
    val adapter: DirectsAdapter get() = _adapter!!
    private var _mGlide:RequestManager?=null
    private val mGlide:RequestManager get() = _mGlide!!


    override fun onDestroyView() {
        _adapter = null
        _mGlide = null
        super.onDestroyView()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EmojiManager.install(IosEmojiProvider())
        _mGlide = Glide.with(this@FragmentInbox)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _adapter = DirectsAdapter(emptyList<Any>().toMutableList())
        binding.recyclerviewDirects.adapter = adapter
        shareViewModel = ViewModelProvider(requireActivity()).get(ShareViewModel::class.java)
        (requireActivity() as MainActivity).isHideNavigationBottom(false)

        shareViewModel.mutableLiveData.observe(viewLifecycleOwner, InstagramDirectObserver())

        shareViewModel.threadNewMessageLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                return@Observer
            }
            for (index in adapter.items.indices) {
                val thread = adapter.items[index]
                if (thread is IGThread && thread.threadId == it.first) {
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
                if (thread is IGThread && thread.threadId == it.threadId) {
                    adapter.items[index] = shareViewModel.getThreadById(it.threadId)
                    adapter.notifyItemChanged(index)
                    break
                }
            }
        })
        shareViewModel.threadsPresence.observe(viewLifecycleOwner, Observer {
            adapter.items = shareViewModel.instagramDirect!!.inbox.igThreads.toMutableList()
            adapter.notifyDataSetChanged()
        })

        shareViewModel.threadChange.observe(viewLifecycleOwner, Observer {
            for (index in adapter.items.indices) {
                val thread = adapter.items[index]
                if (thread is IGThread && thread.threadId == it) {
                    adapter.items[index] = shareViewModel.getThreadById(it)
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
                    val layoutManager = binding.recyclerviewDirects.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
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

    private inner class InstagramDirectObserver : Observer<Resource<IGDirectsResponse>> {
        override fun onChanged(it: Resource<IGDirectsResponse>) {

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
                    if (it.data!!.errorType == InstagramConstants.Error.LOGIN_REQUIRED.msg) {
                        DialogHelper.createDialog(
                            context!!,
                            layoutInflater,
                            title = it.data!!.errorTitle!!,
                            message = it.data!!.errorBody!!,
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
                    val threads = it.data!!.inbox.igThreads
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
            item as IGThread
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
                if (item.bundle != null && item.bundle["typing"] == true) {
                    dataBinding.profileDec.text = getString(R.string.typing)
                    dataBinding.profileDec.setTextColor(Color.WHITE)
                    dataBinding.profileDec.setTypeface(null, Typeface.BOLD)
                    mHandler.postDelayed({
                        item.bundle["typing"] = false
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
                    dataBinding.profileDec.setTypeface(null, Typeface.BOLD)
                } else {
                    val prefix = if (item.lastPermanentItem.userId == user.pk) {
                        "You: "
                    } else {
                        if (item.group) {
                            shareViewModel.getUsernameByUserId(
                                item.threadId,
                                item.lastPermanentItem.userId
                            ) + ": "
                        } else {
                            ""
                        }
                    }
                    when (item.lastPermanentItem.itemType) {
                        InstagramConstants.MessageType.ACTION_LOG.type -> {
                            dataBinding.profileDec.text = item.lastPermanentItem.actionLog.description
                        }
                        InstagramConstants.MessageType.TEXT.type -> {
                            dataBinding.profileDec.text = prefix + item.lastPermanentItem.text
                        }
                        InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                            dataBinding.profileDec.text =
                                prefix + getString(R.string.send_a_sticker)
                        }
                        InstagramConstants.MessageType.REEL_SHARE.type -> {
                            if (item.lastPermanentItem.reelShare.type == InstagramConstants.ReelType.REPLY.type) {
                                if (item.lastPermanentItem.userId == user.pk) {
                                    dataBinding.profileDec.text =
                                        getString(R.string.reply_to_their_story)
                                } else {
                                    dataBinding.profileDec.text =
                                        getString(R.string.reply_to_your_story)
                                }
                            }

                            if (item.lastPermanentItem.reelShare.type == InstagramConstants.ReelType.MENTION.type) {
                                if (item.lastPermanentItem.userId == user.pk) {
                                    dataBinding.profileDec.text = String.format(
                                        getString(R.string.mentioned_person_in_your_story),
                                        item.users[0].username
                                    )
                                } else {
                                    dataBinding.profileDec.text =
                                        getString(R.string.mentioned_you_in_their_story)
                                }
                            }
                            if (item.lastPermanentItem.reelShare.type == InstagramConstants.ReelType.REACTION.type) {
                                if (item.lastPermanentItem.userId == user.pk) {
                                    dataBinding.profileDec.text = String.format(
                                        getString(R.string.you_reacted_to_user_story),
                                        item.users[0].username
                                    )
                                } else {
                                    dataBinding.profileDec.text = String.format(
                                        getString(R.string.reacted_to_your_story_with_reaction),
                                        item.lastPermanentItem.reelShare.text
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
                            dataBinding.profileDec.text = prefix + item.lastPermanentItem.like
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
                                prefix + item.lastPermanentItem.videoCallEvent.description
                        }
                        InstagramConstants.MessageType.LINK.type -> {
                            dataBinding.profileDec.text = prefix +
                                    getString(R.string.share_a_link)
                        }
                        InstagramConstants.MessageType.FELIX_SHARE.type -> {
                            dataBinding.profileDec.text = prefix + String.format(
                                getString(R.string.send_user_igtv_video),
                                item.lastPermanentItem.felixShare.video.user.username
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


                if (item.bundle != null && item.bundle["typing"] == true) {
                    dataBinding.lastMessageTime.visibility = View.GONE
                } else {
                    if (unreadMessage >= 1) {
                        dataBinding.lastMessageTime.setTextViewDrawableColor(Color.WHITE)
                        dataBinding.lastMessageTime.setTextColor(Color.WHITE)
                        dataBinding.lastMessageTime.setTypeface(null, Typeface.BOLD)
                        dataBinding.profileDec.setTextColor(Color.WHITE)
                        dataBinding.profileDec.setTypeface(null, Typeface.BOLD)
                    } else {
                        dataBinding.lastMessageTime.setTextViewDrawableColor(Color.GRAY)
                        dataBinding.lastMessageTime.setTextColor(Color.GRAY)
                        dataBinding.lastMessageTime.setTypeface(null, Typeface.NORMAL)
                        dataBinding.profileDec.setTextColor(Color.GRAY)
                        dataBinding.profileDec.setTypeface(null, Typeface.NORMAL)
                    }
                    dataBinding.lastMessageTime.visibility = View.VISIBLE
                    dataBinding.lastMessageTime.text =
                        TimeUtils.convertTimestampToDate(context!!, item.lastPermanentItem.timestamp)
                }
            }
            if (item.group && item.threadTitle == null) {
                if (item.users.size >= 2) {
                    dataBinding.profileName.text = String.format(
                        getString(R.string.group_name),
                        item.users[0].fullName,
                        item.users.size - 1
                    )
                } else {
                    dataBinding.profileName.text = item.users[0].fullName
                }
            } else {
                dataBinding.profileName.text = item.threadTitle
            }
            if (item.group) {
                visible(dataBinding.layoutProfileImageGroup)
                gone(dataBinding.layoutProfileImageUser)
                if (item.users.size >= 2) {
                    mGlide.load(item.users[1].profilePicUrl)
                        .into(dataBinding.profileImageG1)
                    mGlide.load(item.users[0].profilePicUrl)
                        .into(dataBinding.profileImageG2)
                } else {
                    mGlide.load(user.profilePicUrl)
                        .into(dataBinding.profileImageG1)
                    mGlide.load(item.users[0].profilePicUrl)
                        .into(dataBinding.profileImageG2)
                }
            } else {
                gone(dataBinding.layoutProfileImageGroup)
                visible(dataBinding.layoutProfileImageUser)
                mGlide.load(item.users[0].profilePicUrl)
                    .into(dataBinding.profileImage)
            }
            dataBinding.profileMoreOption.setOnClickListener {
                showPopupOptions(item.threadId, dataBinding.profileMoreOption)
            }
            if (item.lastActivityAt.toString().length == 16) {
                item.lastActivityAt /= 1000
            }
            if (item.bundle != null && item.bundle["active"] == true) {
                dataBinding.profileLastActivityAt.text = getString(R.string.online)
                dataBinding.profileLastActivityAt.setTextColor(context!!.color(R.color.online_color))
                dataBinding.imgIsOnline.visibility = View.VISIBLE
            } else {
                dataBinding.profileLastActivityAt.setTextColor(Color.GRAY)
                dataBinding.profileLastActivityAt.setTypeface(null, Typeface.NORMAL)
                dataBinding.profileLastActivityAt.text = String.format(
                    getString(R.string.active_at),
                    TimeUtils.convertTimestampToDate(context!!, item.lastActivityAt)
                )
                dataBinding.imgIsOnline.visibility = View.INVISIBLE
            }
            dataBinding.root.setOnClickListener {
                val data = DirectBundle().apply {
                    this.threadId = item.threadId
                    this.threadTitle = item.threadTitle
                    this.isGroup = item.group
                    this.isActive = item.bundle["active"] as Boolean
                    this.lastActivityAt = item.lastActivityAt
                }
                val action = NavigationMainGraphDirections.actionGlobalDirectFragment(data)
                findNavController().navigate(action)
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