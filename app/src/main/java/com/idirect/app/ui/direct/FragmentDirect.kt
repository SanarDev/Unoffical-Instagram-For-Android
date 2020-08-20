package com.idirect.app.ui.direct

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.devlomi.record_view.OnRecordListener
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseApplication
import com.idirect.app.core.BaseFragment
import com.idirect.app.customview.doubleclick.DoubleClick
import com.idirect.app.customview.doubleclick.DoubleClickListener
import com.idirect.app.databinding.*
import com.idirect.app.datasource.model.DirectDate
import com.idirect.app.datasource.model.Message
import com.idirect.app.datasource.model.Thread
import com.idirect.app.datasource.model.event.*
import com.idirect.app.datasource.model.response.InstagramLoggedUser
import com.idirect.app.extensions.*
import com.idirect.app.extentions.*
import com.idirect.app.manager.PlayManager
import com.idirect.app.realtime.commands.*
import com.idirect.app.realtime.service.RealTimeService
import com.idirect.app.ui.fullscreen.FullScreenFragment
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.ui.main.ShareViewModel
import com.idirect.app.ui.playvideo.PlayVideoActivity
import com.idirect.app.ui.selectimage.SelectImageDialog
import com.idirect.app.ui.selectimage.SelectImageListener
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.*
import com.tylersuehr.chips.CircleImageView
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiTextView
import com.vanniktech.emoji.ios.IosEmojiProvider
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject


class FragmentDirect : BaseFragment<FragmentDirectBinding, DirectViewModel>(), ActionListener,
    View.OnClickListener {

    @Inject
    lateinit var mGlideRequestManager: RequestManager

    @Inject
    lateinit var mPlayManager: PlayManager

    private lateinit var shareViewModel: ShareViewModel
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var httpDataSourceFactory: DataSource.Factory
    private lateinit var dataSourceFactory: DefaultDataSourceFactory
    private lateinit var mAdapter: ChatsAdapter
    private lateinit var emojiPopup: EmojiPopup
    private var isLoading = false
    private var olderMessageExist = true
    private var _thread: Thread?=null
    private val thread: Thread get() = _thread!!
    private lateinit var mAudioManager: AudioManager
    val onPreDrawListener = object: ViewTreeObserver.OnPreDrawListener{
        override fun onPreDraw(): Boolean {
            startPostponedEnterTransition()
            return true
        }
    }
//    private var lastSeenAt: Long = 0

    companion object {

        const val TAG = "TEST"
        const val PERMISSION_READ_EXTERNAL_STORAGE_CODE = 101
        const val PERMISSION_RECORD_AUDIO_CODE = 102

        const val NAME_TAG = "direct"
    }

    override fun getNameTag(): String {
        return NAME_TAG
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_direct
    }

    override fun getViewModelClass(): Class<DirectViewModel> {
        return DirectViewModel::class.java
    }

    override fun onDestroyView() {
        removeWaitForTransition(binding.recyclerviewChats,onPreDrawListener)
        emojiPopup.releaseMemory()
        shareViewModel.currentThread = null
        super.onDestroyView()
    }

    private val mHandler = Handler()
    private var endTypeAtMs: Long = 0
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
        mAudioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val directBundle = requireArguments().getParcelable<DirectBundle>("data")!!
        shareViewModel = ViewModelProvider(requireActivity()).get(ShareViewModel::class.java)
        (requireActivity() as MainActivity).isHideNavigationBottom(true)

        mAdapter = ChatsAdapter(shareViewModel.getUser())
        binding.recyclerviewChats.adapter = mAdapter
        waitForTransition(binding.recyclerviewChats,onPreDrawListener)
        layoutManager = (binding.recyclerviewChats.layoutManager as LinearLayoutManager)

        initThreadWithBundle(directBundle)

        emojiPopup =
            EmojiPopup.Builder.fromRootView(binding.layoutParent)
                .setOnEmojiPopupDismissListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_emoji)
                }.setOnEmojiPopupShownListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_keyboard_outline)
                }.build(binding.edtTextChat);

        binding.btnVoice.setRecordView(binding.recordView)
        binding.btnVoice.isSoundEffectsEnabled = false
        binding.recordView.isSoundEffectsEnabled = false
        binding.recordView.setCounterTimeColor(color(R.color.counter_voice_time_color));
        binding.recordView.setSmallMicColor(color(R.color.voice_mic_color));
        binding.recordView.setCustomSounds(0, 0, 0);
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.btnVoice.isListenForRecord = false
        } else {
            binding.btnVoice.isListenForRecord = true
        }

        shareViewModel.mutableLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    if(shareViewModel.currentThread != null){
                        val thread = shareViewModel.getThreadById(shareViewModel.currentThread!!.threadId!!)
                        gone(binding.includeLayoutNetwork.root, binding.progressbar)
                        olderMessageExist = thread.oldestCursor != null
                        if (thread.messages.size > mAdapter.items.size) {
                            mAdapter.setItems(viewModel.releaseMessages(thread.messages).toMutableList())
                        }
                        isLoading = false
                        mAdapter.setLoading(isLoading)
                    }
                }
            }
        })

        shareViewModel.threadNewMessageLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                return@Observer
            }
            if (it.first == shareViewModel.currentThread!!.threadId!!) {
                mAdapter.items.add(0, it.second)
                mAdapter.notifyItemInserted(0)
                binding.recyclerviewChats.scrollToPosition(0)
                shareViewModel.threadNewMessageLiveData.value = null
            }
        })

        shareViewModel.messageChange.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                return@Observer
            }
            if (it.first == shareViewModel.currentThread!!.threadId!!) {
                for (i in mAdapter.items.indices) {
                    val message = mAdapter.items[i]
                    if (message is Message) {
                        if (message.clientContext == it.second.clientContext) {
                            mAdapter.items[i] = it.second
                            mAdapter.notifyItemChanged(i)
                        }
                    }
                }
            }
            shareViewModel.messageChange.value = null
        })

        shareViewModel.connectionState.observe(viewLifecycleOwner, Observer {
            when (it.connection) {
                ConnectionStateEvent.State.CONNECTED -> {
                    checkUserStatus()
                }
                ConnectionStateEvent.State.CONNECTING -> {
                    binding.txtProfileDec.text = getString(R.string.connecting)
                }
                ConnectionStateEvent.State.NETWORK_DISCONNECTED -> {
                    binding.txtProfileDec.text = getString(R.string.waiting_for_network)
                }
                else -> {
                    binding.txtProfileDec.text = getString(R.string.connecting)
                }
            }
        })

        binding.recordView.setOnRecordListener(object : OnRecordListener {
            override fun onFinish(recordTime: Long) {
                visible(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto, binding.btnLike)
                gone(binding.recordView)
                shareViewModel.stopRecording()
            }

            override fun onLessThanSecond() {
                visible(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto, binding.btnLike)
                gone(binding.recordView)
                shareViewModel.cancelAudioRecording()
            }

            override fun onCancel() {
                requireContext().vibration(50)
                visible(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto, binding.btnLike)
                gone(binding.recordView)
                shareViewModel.cancelAudioRecording()
            }

            override fun onStart() {
                requireContext().vibration(100)
                visible(binding.recordView)
                gone(
                    binding.btnEmoji,
                    binding.edtTextChat,
                    binding.btnAddPhoto,
                    binding.btnLike
                )
                shareViewModel.startAudioRecording()

            }
        })

        binding.edtTextChat.setOnClickListener(this@FragmentDirect)
        binding.btnBack.setOnClickListener(this@FragmentDirect)
        binding.btnSend.setOnClickListener(this@FragmentDirect)
        binding.btnAddPhoto.setOnClickListener(this@FragmentDirect)
        binding.btnLike.setOnClickListener(this@FragmentDirect)
        binding.btnVoice.setOnClickListener(this@FragmentDirect)
        binding.toolbar.setOnClickListener(this@FragmentDirect)
        binding.btnEmoji.setOnClickListener(this@FragmentDirect)

        binding.edtTextChat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotBlank()) {
                    visible(binding.btnSend)
                    gone(binding.btnLike, binding.btnVoice, binding.btnAddPhoto)
                    RealTimeService.run(
                        requireContext(),
                        RealTime_SendTypingState(
                            thread.threadId,
                            true,
                            InstagramHashUtils.getClientContext()
                        )
                    )
                } else {
                    gone(binding.btnSend)
                    visible(binding.btnLike, binding.btnVoice, binding.btnAddPhoto)
                    RealTimeService.run(
                        requireContext(),
                        RealTime_SendTypingState(
                            thread.threadId,
                            false,
                            InstagramHashUtils.getClientContext()
                        )
                    )
                }
            }
        })


        binding.recyclerviewChats.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading && olderMessageExist) {
                    val totalItemCount = layoutManager.itemCount
                    if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
                        if (totalItemCount - 2 < 0) {
                            return
                        }
                        if (mAdapter.items[totalItemCount - 2] is Message) {
                            shareViewModel.loadMoreItem(
                                (mAdapter.items[totalItemCount - 2] as Message).itemId,
                                thread.threadId
                            )
                            isLoading = true
                            mAdapter.setLoading(isLoading)
                        }
                    }
                }
            }
        })
        initPlayer()

    }

    private fun setLoading(isLoading: Boolean){
        if(isLoading){
            binding.progressbar.visibility = View.VISIBLE
        }else{
            binding.progressbar.visibility = View.GONE
        }
    }
    private fun initThreadData(thread: Thread?=null,directBundle: DirectBundle?=null){
        if(thread == null && directBundle == null){
            return
        }
        val threadTitle:String
        val profilePicUrl:String
        val profilePicUrl2:String
        val isGroup:Boolean

        if(thread != null){
            threadTitle = thread.threadTitle
            profilePicUrl = thread.users[0].profilePicUrl
            profilePicUrl2 = if(thread.isGroup){
                thread.users[1].profilePicUrl
            }else{
                ""
            }
            isGroup = thread.isGroup
        }else{
            threadTitle = directBundle!!.threadTitle
            profilePicUrl = directBundle.profileImage
            if(directBundle.isGroup){
                profilePicUrl2 = directBundle.profileImage2
            }else{
                profilePicUrl2 = ""
            }
            isGroup = directBundle.isGroup
        }

        binding.txtProfileName.text = threadTitle
        checkUserStatus()

        if (!isGroup) {
            gone(binding.layoutProfileImageGroup)
            visible(binding.imgProfileImage)
            mGlideRequestManager.load(profilePicUrl)
                .into(binding.imgProfileImage)
        } else {
            visible(binding.layoutProfileImageGroup)
            gone(binding.imgProfileImage)
            mGlideRequestManager.load(profilePicUrl)
                .into(binding.profileImageG1)
            mGlideRequestManager.load(profilePicUrl2)
                .into(binding.profileImageG2)
        }
    }
    private fun initThreadWithBundle(directBundle: DirectBundle) {
        if(directBundle.threadId != null){
            shareViewModel.currentThread = shareViewModel.getThreadById(directBundle.threadId)
            initThreadData(thread = shareViewModel.currentThread!!)
            _thread = shareViewModel.currentThread!!
            val messages = shareViewModel.currentThread!!.messages
            mAdapter.setItems(viewModel.releaseMessages(messages).toMutableList())
        }else{
            initThreadData(directBundle = directBundle)
            shareViewModel.getThreadByUserId(directBundle).observe(viewLifecycleOwner, Observer {
                if(it.status == Resource.Status.LOADING){
                    setLoading(true)
                }
                if(it.status == Resource.Status.SUCCESS){
                    setLoading(false)
                    shareViewModel.currentThread = it.data
                    initThreadData(thread = shareViewModel.currentThread!!)
                    _thread = shareViewModel.currentThread!!
                    val messages = shareViewModel.currentThread!!.messages
                    mAdapter.setItems(viewModel.releaseMessages(messages).toMutableList())
                }
            })
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            binding.toolbar.id -> {
                if (!thread.isGroup) {
                    val user = thread.users[0]
                    val userId = user.pk.toString()
                    ViewCompat.setTransitionName(binding.imgProfileImage, "profile_$userId")
                    ViewCompat.setTransitionName(binding.txtProfileName, "username_$userId")
                    ViewCompat.setTransitionName(binding.txtProfileDec, "fullname_$userId")

                    val userData = UserBundle().apply {
                        this.userId = userId
                        this.profilePic = user.profilePicUrl
                        this.username = user.username
                        this.fullname = user.fullName
                    }
                    val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(userData)
                    val extras = FragmentNavigatorExtras(
                        binding.imgProfileImage to binding.imgProfileImage.transitionName,
                        binding.txtProfileName to binding.txtProfileName.transitionName,
                        binding.txtProfileDec to binding.txtProfileDec.transitionName
                    )
                    v.findNavController().navigate(action, extras)
                }
            }
            binding.edtTextChat.id -> {
                emojiPopup.dismiss()
            }
            binding.btnBack.id -> {
                requireActivity().onBackPressed()
            }
            binding.btnAddPhoto.id -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        PERMISSION_READ_EXTERNAL_STORAGE_CODE
                    )
                    return
                }
                SelectImageDialog(object : SelectImageListener {
                    override fun onImageSelected(imagesPath: List<String>) {
                        shareViewModel.uploadMedias(items = imagesPath)
                    }
                }).show(requireActivity().supportFragmentManager, "Dialog")
            }
            binding.btnSend.id -> {
                val text = binding.edtTextChat.text.toString()
                if (text.isBlank()) {
                    return
                }
                binding.edtTextChat.setText("")
                shareViewModel.sendTextMessage(shareViewModel.currentThread!!.threadId!!, text)
            }
            binding.btnLike.id -> {
                shareViewModel.sendLike(shareViewModel.currentThread!!.threadId)
            }
            binding.btnVoice.id -> {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    PERMISSION_RECORD_AUDIO_CODE
                )
            }
            binding.btnEmoji.id -> {
                if (emojiPopup.isShowing) {
                    emojiPopup.dismiss()
                } else {
                    emojiPopup.toggle()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_READ_EXTERNAL_STORAGE_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.btnAddPhoto.callOnClick()
            }
        } else if (requestCode == PERMISSION_RECORD_AUDIO_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.btnVoice.isListenForRecord = true
            }
        }
    }

    private fun checkUserStatus() {
        if(_thread == null){
            return
        }
        if (!thread.isGroup && thread.active) {
            binding.txtProfileDec.text = getString(R.string.online)
            binding.txtProfileDec.setTextColor(color(R.color.online_color))
        } else {
            if (thread.lastActivityAt == 0.toLong()) {
                binding.txtProfileDec.text = thread.threadTitle
            } else {
                binding.txtProfileDec.text = String.format(
                    getString(R.string.active_at),
                    TimeUtils.convertTimestampToDate(requireContext(), thread.lastActivityAt)
                )
                binding.txtProfileDec.setTextColor(color(R.color.text_light))
            }
        }
    }
//
//    override fun onHideKeyboard() {
//        RealTimeService.run(
//            requireContext(),
//            RealTime_SendTypingState(
//                thread.threadId,
//                false,
//                InstagramHashUtils.getClientContext()
//            )
//        )
//    }

    private fun initPlayer() {
        httpDataSourceFactory =
            DefaultHttpDataSourceFactory(Util.getUserAgent(requireContext(), "Instagram"))
        dataSourceFactory =
            DefaultDataSourceFactory(
                requireContext(),
                Util.getUserAgent(requireContext(), "Instagram")
            )
    }

    inner class ChatsAdapter(var user: InstagramLoggedUser) :
        BaseAdapter() {

        var items: MutableList<Any> = ArrayList<Any>().toMutableList()
        private set

        fun setItems(items: MutableList<Any>){
            this.items = items
            notifyDataSetChanged()
        }

        override fun onViewRecycled(holder: BaseViewHolder) {
            if (holder.binding is LayoutVoiceMediaBinding) {
                val dataBinding = holder.binding as LayoutVoiceMediaBinding
                if (dataBinding.seekbarPlay.tag == mPlayManager.currentPlayerId) {
                    mPlayManager.seekbarPlay = null
                }
            }
        }

        @SuppressLint("SimpleDateFormat")
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            if (item is DirectDate) {
                val dataBinding = holder.binding as LayoutDirectDateBinding
                dataBinding.txtDate.text = item.timeString
                return item
            }
            if (item is LoadingEvent) {
                return item
            }
            item as Message
            if (item.timestamp.toString().length == 16) {
                item.timestamp = item.timestamp / 1000
            }

            var includeTime: LayoutTimeBinding? = null
            var includeReaction: LayoutReactionsLikeBinding? = null
            var layoutParent: LinearLayout? = null
            var layoutMessage: LinearLayout? = null
            var imgThreadProfileImage: CircleImageView? = null
            var txtSendername: EmojiTextView? = null
            var layoutItem: LinearLayout? = null


            when (item.itemType) {
                InstagramConstants.MessageType.TEXT.type -> {
                    val dataBinding = (holder.binding as LayoutMessageBinding)
                    includeTime = dataBinding.includeTime
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    layoutParent = dataBinding.layoutParent
                    layoutMessage = dataBinding.layoutMessage
                    includeReaction = dataBinding.includeReaction
                    txtSendername = dataBinding.txtSendername
                }
                InstagramConstants.MessageType.LINK.type -> {
                    val dataBinding = (holder.binding as LayoutLinkBinding)
                    includeTime = dataBinding.includeTime
                    layoutParent = dataBinding.layoutParent
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    layoutMessage = dataBinding.layoutMessage
                    includeReaction = dataBinding.includeReaction
                    txtSendername = dataBinding.txtSendername
                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    when (item.reelShare.type) {
                        InstagramConstants.ReelType.REPLY.type -> {
                            val dataBinding = (holder.binding as LayoutReelShareReplyBinding)
                            includeTime = dataBinding.includeTime
                            layoutParent = dataBinding.layoutParent
                            imgThreadProfileImage = dataBinding.imgThreadProfileImage
                            layoutMessage = dataBinding.layoutMessage
                            includeReaction = dataBinding.includeReaction
                        }
                        InstagramConstants.ReelType.MENTION.type -> {
                            val dataBinding = (holder.binding as LayoutReelShareBinding)
                            includeTime = dataBinding.includeTime
                            layoutParent = dataBinding.layoutParent
                            layoutMessage = dataBinding.layoutMessage
                            imgThreadProfileImage = dataBinding.imgThreadProfileImage
                            includeReaction = dataBinding.includeReaction
                        }
                        else -> { // item.reelShare.type == InstagramConstants.ReelType.REACTION.type
                            val dataBinding = (holder.binding as LayoutReactionStoryBinding)
                            includeTime = dataBinding.includeTime
                            imgThreadProfileImage = dataBinding.imgThreadProfileImage
                            includeReaction = dataBinding.includeReaction
                            layoutParent = dataBinding.layoutParent
                            layoutMessage = dataBinding.layoutMessage
                        }
                    }
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        val dataBinding = (holder.binding as LayoutReelShareBinding)
                        includeTime = dataBinding.includeTime
                        layoutParent = dataBinding.layoutParent
                        layoutMessage = dataBinding.layoutMessage
                        imgThreadProfileImage = dataBinding.imgThreadProfileImage
                        includeReaction = dataBinding.includeReaction
                    } else {
                        val dataBinding = (holder.binding as LayoutStoryShareNotLinkedBinding)
                        includeTime = dataBinding.includeTime
                        layoutParent = dataBinding.layoutParent
                        imgThreadProfileImage =dataBinding.imgThreadProfileImage
                        layoutMessage = dataBinding.layoutMessage
                        includeReaction = dataBinding.includeReaction
                    }
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    val dataBinding = (holder.binding as LayoutVoiceMediaBinding)
                    includeTime = dataBinding.includeTime
                    layoutParent = dataBinding.layoutParent
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    includeReaction = dataBinding.includeReaction
                    layoutMessage = dataBinding.layoutMessage
                    txtSendername = dataBinding.txtSendername
                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                }
                InstagramConstants.MessageType.MEDIA.type -> {
                    val dataBinding = (holder.binding as LayoutMediaBinding)
                    includeTime = dataBinding.includeTime
                    layoutParent = dataBinding.layoutParent
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    layoutMessage = dataBinding.layoutMessage
                    includeReaction = dataBinding.includeReaction
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    val dataBinding = (holder.binding as LayoutRavenMediaBinding)
                    includeTime = dataBinding.includeTime
                    layoutParent = dataBinding.layoutParent
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    layoutMessage = dataBinding.layoutMessage
                    includeReaction = dataBinding.includeReaction
                    txtSendername = dataBinding.txtSendername
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    val dataBinding = (holder.binding as LayoutLikeBinding)
                    includeTime = dataBinding.includeTime
                    layoutParent = dataBinding.layoutParent
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    includeReaction = dataBinding.includeReaction
                    layoutMessage = dataBinding.layoutMessage
//                            holder.binding as LayoutLikeBinding
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    val dataBinding = (holder.binding as LayoutMediaShareBinding)
                    includeTime = dataBinding.includeTime
                    layoutParent = dataBinding.layoutParent
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    layoutMessage = dataBinding.layoutMessage
                    includeReaction = dataBinding.includeReaction
                    txtSendername = dataBinding.txtSendername
                }
                InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                    val dataBinding = (holder.binding as LayoutAnimatedMediaBinding)
                    layoutParent = dataBinding.layoutParent
                    includeReaction = dataBinding.includeReaction
//                    imgThreadProfileImage =  (holder.binding as LayoutAnimatedMediaBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
                    val dataBinding = (holder.binding as LayoutFelixShareBinding)
                    includeTime = dataBinding.includeTime
                    layoutParent = dataBinding.layoutParent
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    layoutMessage = dataBinding.layoutMessage
                    includeReaction = dataBinding.includeReaction
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {

                }
                InstagramConstants.MessageType.PLACE_HOLDER.type -> {
                    val dataBinding = (holder.binding as LayoutPlaceholderBinding)
                    includeTime = dataBinding.includeTime
                    layoutParent = dataBinding.layoutParent
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    layoutMessage = dataBinding.layoutMessage
                    txtSendername = dataBinding.txtSendername
                }
                else -> {
                    val dataBinding = (holder.binding as LayoutMessageBinding)
                    includeTime = dataBinding.includeTime
                    layoutParent = dataBinding.layoutParent
                    imgThreadProfileImage = dataBinding.imgThreadProfileImage
                    layoutMessage = dataBinding.layoutMessage
                    includeReaction = dataBinding.includeReaction
                    txtSendername = dataBinding.txtSendername
                }
            }

            if (includeReaction != null) {
                if (item.reactions == null) {
                    includeReaction.layoutReactionsParent.visibility = View.GONE
                } else {
                    if (item.userId == user.pk) {
                        includeReaction.layoutReactionsParent.gravity = Gravity.RIGHT
                    } else {
                        includeReaction.layoutReactionsParent.gravity = Gravity.RIGHT
//                        (includeReaction.layoutReactionsParent.layoutParams as LinearLayout.LayoutParams).apply {
//                            leftMargin = resources.dpToPx(50f)
//                        }
                    }
                    val likes = item.reactions.likes
                    includeReaction.layoutReactionsProfiles.removeAllViews()
                    for (i in likes.indices) {
                        if (i == 2) {
                            break
                        }
                        var profileUrl: String? = null
                        if (likes[i].senderId == mAdapter.user.pk) {
                            profileUrl = mAdapter.user.profilePicUrl
                        } else {
                            for (user in thread.users) {
                                if (likes[i].senderId == user.pk) {
                                    profileUrl = user.profilePicUrl
                                }
                            }
                        }
                        if (profileUrl != null) {
                            val image = CircleImageView(requireContext())
                            image.layoutParams = android.widget.LinearLayout.LayoutParams(
                                resources.dpToPx(25f),
                                resources.dpToPx(25f)
                            )
                            image.setBackgroundResource(R.drawable.bg_stroke_circluar)
                            mGlideRequestManager.load(profileUrl).into(image)
                            image.setPadding(
                                includeReaction.imgHeart.paddingLeft,
                                includeReaction.imgHeart.paddingTop,
                                includeReaction.imgHeart.paddingRight,
                                includeReaction.imgHeart.paddingBottom
                            )
                            includeReaction.layoutReactionsProfiles.addView(image)
                        }
                    }
                    includeReaction.layoutReactionsParent.visibility = View.VISIBLE
                }
            }

            var lastSeenAt: Long = 0
            if (thread.lastSeenAt != null) {
                for (ls in thread.lastSeenAt.entries) {
                    if (ls.key.toLong() != item.userId) {
                        // for example last seen in group is laster seen
                        viewModel.convertToStandardTimeStamp(ls.value.timeStamp).also {
                            if (it > lastSeenAt) {
                                lastSeenAt = it
                            }
                        }
                    }
                }
            }
            if (item.timestamp > lastSeenAt && shareViewModel.isSeenMessageEnable) {
//                shareViewModel.markAsSeen(threadId, item.itemId) moshkel ine ke callback barash ok nakardm barate update shodan main activty
                RealTimeService.run(
                    requireContext(),
                    RealTime_MarkAsSeen(
                        thread.threadId,
                        item.itemId
                    )
                )
            }

            if (includeTime != null) {
                includeTime.txtTime.text =
                    shareViewModel.getTimeFromTimeStamps(item.timestamp)

                if (!item.isDelivered) {
                    includeTime.imgMessageStatus.setImageResource(R.drawable.ic_time)
                    includeTime.imgMessageStatus.setColorFilter(color(R.color.text_light))
                } else {

                    if (item.timestamp <= lastSeenAt) {
                        includeTime.imgMessageStatus.setImageResource(R.drawable.ic_check_multiple)
                        includeTime.imgMessageStatus.setColorFilter(color(R.color.checked_message_color))
                    } else {
                        includeTime.imgMessageStatus.setImageResource(R.drawable.ic_check)
                        includeTime.imgMessageStatus.setColorFilter(color(R.color.text_light))
                    }
                }
                if (item.userId == user.pk) {
                    includeTime.imgMessageStatus.visibility = View.VISIBLE
                } else {
                    includeTime.imgMessageStatus.visibility = View.GONE
                }

            }
            if (layoutParent != null) {
                if (item.userId == user.pk) {
                    layoutParent.gravity = Gravity.RIGHT
                    if (item.itemType == InstagramConstants.MessageType.REEL_SHARE.type ||
                        item.itemType == InstagramConstants.MessageType.MEDIA_SHARE.type ||
                        item.itemType == InstagramConstants.MessageType.FELIX_SHARE.type ||
                        item.itemType == InstagramConstants.MessageType.STORY_SHARE.type
                    ) {
                        layoutParent.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    }
                } else {
                    layoutParent.gravity = Gravity.LEFT
                    layoutParent.layoutDirection = View.LAYOUT_DIRECTION_LTR
                }
            }
            if (imgThreadProfileImage != null) {
                if (item.userId == user.pk) {
                    imgThreadProfileImage.visibility = View.GONE
                } else {
                    imgThreadProfileImage.visibility = View.VISIBLE
                    mGlideRequestManager.load(shareViewModel.getUserProfilePic(item.userId))
                        .into(imgThreadProfileImage)
                }
            }
            if (layoutMessage != null) {
                layoutMessage.setOnClickListener(DoubleClick(object : DoubleClickListener {
                    override fun onDoubleClick(view: View?) {
//                        RealTimeService.run(this@DirectActivity,RealTime_SendReaction(item.itemId,"like",item.clientContext,threadId,"created"))
                        shareViewModel.sendReaction(
                            item.itemId,
                            thread.threadId,
                            item.clientContext
                        )
                    }

                    override fun onSingleClick(view: View?) {
                    }
                }))
                layoutMessage.setOnLongClickListener {
                    showPopupOptions(item, it)
                    return@setOnLongClickListener true
                }
                if (item.userId == user.pk) {
                    layoutMessage.background =
                        requireContext().getDrawable(R.drawable.bg_message)
                } else {
                    layoutMessage.background =
                        requireContext().getDrawable(R.drawable.bg_message_2)
                }
            }
            if (txtSendername != null) {
                if (thread.isGroup && item.userId != thread.viewerId) {
                    visible(txtSendername)
                    txtSendername.text = shareViewModel.getUsername(item.userId)
                } else {
                    gone(txtSendername)
                }
            }


            when (item.itemType) {
                InstagramConstants.MessageType.TEXT.type -> {
                    val dataBinding = holder.binding as LayoutMessageBinding
                    val textWithoghtEmoji = item.text.replace(
                        Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+").toRegex(), ""
                    )
                    dataBinding.txtMessage.maxWidth = (DisplayUtils.getScreenWidth() * 0.7).toInt()
                    // check text is only emoji
                    if (textWithoghtEmoji.isBlank()) {
                        dataBinding.layoutMessage.background = null
                        dataBinding.txtMessage.setEmojiSizeRes(R.dimen.emoji_size_big)
                        dataBinding.txtMessage.text = item.text
                    } else {
                        dataBinding.txtMessage.setEmojiSizeRes(R.dimen.emoji_size_normal)
                        dataBinding.txtMessage.text = item.text
                    }
                }
                InstagramConstants.MessageType.LINK.type -> {
                    val dataBinding = holder.binding as LayoutLinkBinding
                    dataBinding.txtMessage.width = (DisplayUtils.getScreenWidth() * 0.6).toInt()
                    val link = item.link
                    dataBinding.txtMessage.setTextLinkHTML(requireContext(), link.text)
                    if (link.linkContext == null || link.linkContext.linkTitle.isNullOrBlank()) {
                        gone(dataBinding.layoutLinkDes)
                    } else {
                        visible(dataBinding.layoutLinkDes)
                        dataBinding.txtLinkSummary.text = link.linkContext.linkSummary
                        dataBinding.txtLinkTitle.text = link.linkContext.linkTitle
                    }
                    if (link.linkContext == null || link.linkContext.linkImageUrl.isNullOrBlank()) {
                        dataBinding.imgLinkImage.visibility = View.GONE
                    } else {
                        mGlideRequestManager.load(link.linkContext.linkImageUrl)
                            .placeholder(R.drawable.placeholder_loading)
                            .into(dataBinding.imgLinkImage)
                    }

                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    if (item.reelShare.type == InstagramConstants.ReelType.REPLY.type) {
                        val dataBinding = holder.binding as LayoutReelShareReplyBinding
                        dataBinding.layoutStory.layoutDirection =
                            if (item.userId == thread.viewerId) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
                        if (item.reelShare.media?.imageVersions2 != null) {
                            val image = item.reelShare.media!!.imageVersions2!!.candidates[1]
                            val size =
                                shareViewModel.getStandardWidthAndHeight(
                                    image.width,
                                    image.height,
                                    0.2f
                                )
                            mGlideRequestManager
                                .load(image.url)
                                .override(size[0], size[1])
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imgStory)

                        } else {
                            gone(dataBinding.layoutImgStory)
                        }
                        val textWithoghtEmoji = item.reelShare.text.replace(
                            Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+").toRegex(), ""
                        )
                        if (item.userId == user.pk) {
                            if (item.userId != item.reelShare.reelOwnerId) {
                                dataBinding.txtReelTypeInfo.text =
                                    getString(R.string.reply_to_their_story)
                            } else {
                                dataBinding.txtReelTypeInfo.text =
                                    getString(R.string.reply_to_their_story)
                            }
                            dataBinding.txtMessage.gravity = Gravity.RIGHT
                        } else {
                            if (item.userId != item.reelShare.reelOwnerId) {
                                dataBinding.txtReelTypeInfo.text =
                                    getString(R.string.reply_to_your_story)
                            } else {
                                dataBinding.txtReelTypeInfo.text =
                                    getString(R.string.you_viewed_their_story)
                            }
                            dataBinding.txtMessage.gravity = Gravity.LEFT
                        }
                        if (textWithoghtEmoji.isBlank()) {
                            dataBinding.layoutMessage.background = null
                            dataBinding.txtMessage.setEmojiSizeRes(R.dimen.emoji_size_big)
                            dataBinding.txtMessage.text = item.reelShare.text
                        } else {
                            dataBinding.txtMessage.setEmojiSizeRes(R.dimen.emoji_size_normal)
                            dataBinding.txtMessage.text = item.reelShare.text
                        }

                    }
                    if (item.reelShare.type == InstagramConstants.ReelType.MENTION.type) {
                        val dataBinding = holder.binding as LayoutReelShareBinding
                        gone(dataBinding.imgProfile, dataBinding.txtUsername)
                        layoutMessage?.background = null
                        if (item.userId == thread.viewerId) {
                            dataBinding.layoutParent.gravity = Gravity.RIGHT
                            dataBinding.layoutStory.layoutDirection = View.LAYOUT_DIRECTION_RTL
                            dataBinding.txtReelStatus.text = String.format(
                                getString(R.string.mentioned_person_in_your_story),
                                thread.users[0].username
                            )
                        } else {
                            dataBinding.txtReelStatus.text =
                                getString(R.string.mentioned_you_in_their_story)
                            dataBinding.layoutParent.gravity = Gravity.LEFT
                            dataBinding.layoutStory.layoutDirection = View.LAYOUT_DIRECTION_LTR
                        }
                        dataBinding.includeTime.txtTime.text =
                            shareViewModel.getTimeFromTimeStamps(
                                item.timestamp
                            )
                        if (item.reelShare.media.imageVersions2 != null) {
                            val image = item.reelShare.media!!.imageVersions2!!.candidates[1]
                            val size =
                                shareViewModel.getStandardWidthAndHeight(
                                    image.width,
                                    image.height,
                                    0.2f
                                )
                            val user = item.reelShare.media.user
                            mGlideRequestManager.load(image.url)
                                .override(size[0], size[1])
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imgStory)
                            mGlideRequestManager.load(user.profilePicUrl)
                                .into(dataBinding.imgProfile)
                            dataBinding.txtUsername.text = user.username
                        } else {
                            visible(dataBinding.txtNoDataAvailable)
                            gone(dataBinding.imgStory, dataBinding.imgProfile)
                        }
                    }
                    if (item.reelShare.type == InstagramConstants.ReelType.REACTION.type) {
                        val dataBinding = holder.binding as LayoutReactionStoryBinding
                        layoutMessage?.background = null
                        if (item.reelShare.media?.imageVersions2 != null) {
                            val image = item.reelShare.media!!.imageVersions2!!.candidates[1]
                            val size =
                                shareViewModel.getStandardWidthAndHeight(
                                    image.width,
                                    image.height,
                                    0.2f
                                )
                            mGlideRequestManager
                                .load(image.url)
                                .override(size[0], size[1])
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imgStory)
                        }

                        val textWithoghtEmoji = item.reelShare.text.replace(
                            Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+").toRegex(), ""
                        )
                        if (textWithoghtEmoji.isBlank()) {
                            dataBinding.txtMessage.setEmojiSizeRes(R.dimen.emoji_size_big)
                            dataBinding.txtMessage.text = item.reelShare.text
                        } else {
                            dataBinding.txtMessage.setEmojiSizeRes(R.dimen.emoji_size_normal)
                            dataBinding.txtMessage.text = item.reelShare.text
                        }

                        if (item.userId == user.pk) {
                            dataBinding.includeTime.imgMessageStatus.visibility = View.VISIBLE
                            dataBinding.txtReactedStatus.text =
                                getString(R.string.you_reacted_to_their_story)
                            dataBinding.layoutStory.layoutDirection = View.LAYOUT_DIRECTION_RTL
                            dataBinding.imgThreadProfileImage.visibility = View.GONE
                        } else {
                            dataBinding.includeTime.imgMessageStatus.visibility = View.GONE
                            dataBinding.txtReactedStatus.text =
                                getString(R.string.reacted_to_your_story)
                            dataBinding.layoutStory.layoutDirection = View.LAYOUT_DIRECTION_LTR

                            dataBinding.imgThreadProfileImage.visibility = View.VISIBLE
                            mGlideRequestManager
                                .load(shareViewModel.getUserProfilePic(item.userId))
                                .into(dataBinding.imgThreadProfileImage)
                        }

                    }

                    layoutMessage?.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            item.reelShare.media?.videoVersions?.also {
                                PlayVideoActivity.playUrl(requireContext(), it[0].url)
                                return
                            }
                            item.reelShare.media?.imageVersions2?.also {
//                                val action =
//                                    FragmentDirectDirections.actionFragmentDirectToFullScreenFragment(
//                                        FullScreenFragment.TYPE_URL,
//                                        it.candidates[0].url
//                                    )
//                                view!!.findNavController().navigate(action)
                                return
                            }
                        }
                    })
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        val dataBinding = holder.binding as LayoutReelShareBinding
                        layoutMessage?.background = null
                        val images = item.storyShare.media.imageVersions2.candidates
                        val user = item.storyShare.media.user
                        val size =
                            shareViewModel.getStandardWidthAndHeight(
                                images[0].width,
                                images[0].height,
                                0.4f
                            )
                        mGlideRequestManager.load(images[0].url)
                            .placeholder(R.drawable.placeholder_loading).into(dataBinding.imgStory)
                        mGlideRequestManager.load(user.profilePicUrl)
                            .into(dataBinding.imgProfile)
                        dataBinding.txtUsername.text = user.username
                        dataBinding.txtReelStatus.text = String.format(
                            getString(R.string.send_story_from), user.username
                        )
                        dataBinding.imgStory.layoutParams.apply {
                            width = size[0]
                            height = size[1]
                        }
                        layoutMessage?.setOnClickListener(DoubleClick(object : DoubleClickListener {
                            override fun onDoubleClick(view: View?) {
//                        RealTimeService.run(this@DirectActivity,RealTime_SendReaction(item.itemId,"like",item.clientContext,threadId,"created"))
                                shareViewModel.sendReaction(
                                    item.itemId,
                                    thread.threadId,
                                    item.clientContext
                                )
                            }

                            override fun onSingleClick(view: View?) {
                                if (item.storyShare.media.videoVersions != null) {
                                    PlayVideoActivity.playUrl(
                                        activity!!,
                                        item.storyShare.media.videoVersions[0].url
                                    )
                                } else {
//                                    val action =
//                                        FragmentDirectDirections.actionFragmentDirectToFullScreenFragment(
//                                            FullScreenFragment.TYPE_URL,
//                                            images[0].url
//                                        )
//                                    view!!.findNavController().navigate(action)
                                }
                            }
                        }))
                    } else {
                        val dataBinding = holder.binding as LayoutStoryShareNotLinkedBinding
                        dataBinding.txtMessage.maxWidth =
                            (DisplayUtils.getScreenWidth() * 0.6).toInt()
                        dataBinding.txtTitle.text = item.storyShare.title
                        dataBinding.txtMessage.text = item.storyShare.message
                    }
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    val dataBinding = holder.binding as LayoutVoiceMediaBinding
                    val mediaSource: MediaSource
                    val id: String = item.itemId
                    val uri: Uri
                    val duration: Int
                    if (item.voiceMediaData.isLocal) {
                        uri = Uri.fromFile(File(item.voiceMediaData.localFilePath))
                        mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(uri)
                        duration = item.voiceMediaData.localDuration
                    } else {
//                        id = item.voiceMediaData.voiceMedia.id
                        uri = Uri.parse(item.voiceMediaData.voiceMedia.audio.audioSrc)
                        mediaSource = ProgressiveMediaSource.Factory(httpDataSourceFactory)
                            .createMediaSource(uri)
                        duration = item.voiceMediaData.voiceMedia.audio.duration
                    }

                    (dataBinding.layoutMessage.layoutParams as LinearLayout.LayoutParams).apply {
                        width = shareViewModel.getStandardVoiceWitdh(resources, duration)
                    }

                    dataBinding.seekbarPlay.tag = id
                    if (mPlayManager.currentPlayerId == id) {
                        mPlayManager.seekbarPlay = dataBinding.seekbarPlay
                        mPlayManager.btnPlay = dataBinding.btnPlayPause
                        dataBinding.btnPlayPause.setImageResource(R.drawable.ic_pause_circle)
                    } else {
                        dataBinding.seekbarPlay.progress = 0
                        dataBinding.btnPlayPause.setImageResource(R.drawable.ic_play_circle)
                    }
                    dataBinding.btnPlayPause.setOnClickListener {
                        if (mPlayManager.currentPlayerId != id) {
                            mPlayManager.startPlay(mediaSource, id)
                            dataBinding.btnPlayPause.setImageResource(R.drawable.ic_pause_circle)
                            mPlayManager.seekbarPlay = dataBinding.seekbarPlay
                            mPlayManager.btnPlay = dataBinding.btnPlayPause
                            this@FragmentDirect.stopAllAudio()
                        } else {
                            dataBinding.btnPlayPause.setImageResource(R.drawable.ic_play_circle)
                            mPlayManager.stopPlay()
                        }
                    }
                    dataBinding.layoutMessage.minimumWidth =
                        (DisplayUtils.getScreenWidth() * 0.6).toInt()

                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                    val dataBinding = holder.binding as LayoutEventBinding
                    dataBinding.txtEventDes.text = item.videoCallEvent.description
                }
                InstagramConstants.MessageType.MEDIA.type -> {
                    val dataBinding = holder.binding as LayoutMediaBinding
                    ViewCompat.setTransitionName(
                        dataBinding.imgMedia,
                        "media_${item.clientContext}"
                    )
                    if (item.media.isLocal) {
                        val originalSize =
                            MediaUtils.getMediaWidthAndHeight(item.media.localFilePath)
                        val size = shareViewModel.getStandardWidthAndHeight(
                            originalSize[0],
                            originalSize[1],
                            0.5f
                        )
                        if (item.media.mediaType == 1) {
                            mGlideRequestManager.load(File(item.media.localFilePath))
                                .override(size[0], size[1])
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imgMedia)
                            gone(dataBinding.btnPlay)
                        } else {
                            val options = RequestOptions().frame(0)
                            mGlideRequestManager.asBitmap()
                                .load(File(item.media.localFilePath))
                                .override(size[0], size[1])
                                .centerCrop()
                                .apply(options)
                                .into(dataBinding.imgMedia)
                            visible(dataBinding.btnPlay)
                        }
                    } else {
                        val images = item.media.imageVersions2.candidates[0]
                        val size =
                            shareViewModel.getStandardWidthAndHeight(
                                images.width,
                                images.height,
                                0.5f
                            )
                        mGlideRequestManager.load(images.url)
                            .override(size[0], size[1])
                            .placeholder(R.drawable.placeholder_loading)
                            .into(dataBinding.imgMedia)
                        if (item.media.videoVersions != null) {
                            visible(dataBinding.btnPlay)
                        } else {
                            gone(dataBinding.btnPlay)
                        }

                        layoutMessage?.setOnClickListener(DoubleClick(object : DoubleClickListener {
                            override fun onDoubleClick(view: View?) {
//                        RealTimeService.run(this@DirectActivity,RealTime_SendReaction(item.itemId,"like",item.clientContext,threadId,"created"))
                                shareViewModel.sendReaction(
                                    item.itemId,
                                    thread.threadId,
                                    item.clientContext
                                )
                            }

                            override fun onSingleClick(view: View?) {
                                if (item.media.videoVersions != null) {
                                    PlayVideoActivity.playUrl(
                                        activity!!,
                                        item.media.videoVersions[0].url
                                    )
                                } else {
//                                    val action =
//                                        FragmentDirectDirections.actionFragmentDirectToFullScreenFragment(
//                                            FullScreenFragment.TYPE_URL,
//                                            images.url,
//                                            item.clientContext
//                                        )
//                                    val extras = FragmentNavigatorExtras(
//                                        dataBinding.imgMedia to dataBinding.imgMedia.transitionName
//                                    )
//                                    view!!.findNavController().navigate(action, extras)
                                }
                            }
                        }))
                    }
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    val dataBinding = holder.binding as LayoutRavenMediaBinding
                    val media = item.ravenMedia.media
                    if (media.imageVersions2 != null) {
                        dataBinding.txtMessage.text = getString(R.string.view_photo)
                        dataBinding.layoutMedia.setOnClickListener {
                            shareViewModel.markAsSeenRavenMedia(
                                shareViewModel.currentThread!!.threadId!!,
                                item.itemId,
                                item.clientContext
                            )
//                            val action =
//                                FragmentDirectDirections.actionFragmentDirectToFullScreenFragment(
//                                    FullScreenFragment.TYPE_URL,
//                                    media.imageVersions2.candidates[0].url
//                                )
//                            it!!.findNavController().navigate(action)
                        }
                    } else if (media.videoVersions != null) {
                        dataBinding.txtMessage.text = getString(R.string.view_video)
                        dataBinding.layoutMedia.setOnClickListener {
                            shareViewModel.markAsSeenRavenMedia(
                                shareViewModel.currentThread!!.threadId!!,
                                item.itemId,
                                item.clientContext
                            )
                            PlayVideoActivity.playUrl(
                                activity!!,
                                media.videoVersions[0].url
                            )
                        }
                    } else {
                        dataBinding.txtMessage.text = getString(R.string.media_expired)
                    }
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    val dataBinding = holder.binding as LayoutLikeBinding
                    layoutMessage?.background = null
                    dataBinding.txtMessage.text = item.like
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    val dataBinding = holder.binding as LayoutMediaShareBinding
                    val media = item.mediaShare
                    val user = media.user
                    val id = item.mediaShare.id
                    when (media.mediaType) {
                        InstagramConstants.MediaType.IMAGE.type -> {
                            val image = media.imageVersions2
                            gone(dataBinding.layoutVideoView, dataBinding.imgMultipleItem)
                            dataBinding.layoutImageView.visibility = View.VISIBLE
                            mGlideRequestManager.load(image.candidates[0].url)
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imageView)
                            dataBinding.layoutImageView.layoutParams.apply {
                                val sizeArray = shareViewModel.getStandardWidthAndHeight(
                                    resources.dpToPx(image.candidates[0].width.toFloat()),
                                    resources.dpToPx(image.candidates[0].height.toFloat())
                                )
                                width = sizeArray[0]
                                height = sizeArray[1]
                            }
                        }
                        InstagramConstants.MediaType.VIDEO.type -> {
                            gone(dataBinding.layoutImageView, dataBinding.imgMultipleItem)
                            dataBinding.layoutVideoView.visibility = View.VISIBLE
                            val videoSrc = item.mediaShare.videoVersions[0].url
                            val image = item.mediaShare.imageVersions2.candidates[1].url
                            mGlideRequestManager.load(image).into(dataBinding.imgPreviewVideo)
                        }
                        InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
                            val image = media.carouselMedia[0].imageVersions2
                            gone(dataBinding.layoutVideoView)
                            visible(dataBinding.imgMultipleItem)
                            dataBinding.layoutImageView.visibility = View.VISIBLE
                            mGlideRequestManager.load(image.candidates[0].url)
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imageView)
                            dataBinding.layoutImageView.layoutParams.apply {
                                val sizeArray = shareViewModel.getStandardWidthAndHeight(
                                    resources.dpToPx(image.candidates[0].width.toFloat()),
                                    resources.dpToPx(image.candidates[0].height.toFloat())
                                )
                                width = sizeArray[0]
                                height = sizeArray[1]
                            }
                        }
                    }

                    layoutMessage?.setOnClickListener(DoubleClick(object : DoubleClickListener {
                        override fun onDoubleClick(view: View?) {
//                        RealTimeService.run(this@DirectActivity,RealTime_SendReaction(item.itemId,"like",item.clientContext,threadId,"created"))
                            shareViewModel.sendReaction(
                                item.itemId,
                                thread.threadId,
                                item.clientContext
                            )
                        }

                        override fun onSingleClick(view: View?) {
                            when (media.mediaType) {
                                InstagramConstants.MediaType.IMAGE.type -> {
//                                    val action =
//                                        FragmentDirectDirections.actionFragmentDirectToFullScreenFragment(
//                                            FullScreenFragment.TYPE_URL,
//                                            media.imageVersions2.candidates[0].url
//                                        )
//                                    view!!.findNavController().navigate(action)
                                }
                                InstagramConstants.MediaType.VIDEO.type -> {
                                    PlayVideoActivity.playUrl(
                                        activity!!,
                                        item.mediaShare.videoVersions[0].url
                                    )
                                }
                                InstagramConstants.MediaType.CAROUSEL_MEDIA.type -> {
//                                    val action =
//                                        FragmentDirectDirections.actionFragmentDirectToFullScreenFragment(
//                                            FullScreenFragment.TYPE_POST,
//                                            media.id
//                                        )
//                                    view!!.findNavController().navigate(action)
                                }
                            }
                        }
                    }))

                    mGlideRequestManager.load(user.profilePicUrl)
                        .into(dataBinding.imgProfile)
                    dataBinding.txtUsername.text = user.username
                    media.caption.also {
                        if (it != null)
                            dataBinding.txtCaption.text = it.text
                    }
                }
                InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                    val dataBinding = holder.binding as LayoutAnimatedMediaBinding
                    val url = item.animatedMedia.images.fixedHeight.url
                    val width = item.animatedMedia.images.fixedHeight.width.toInt()
                    val height = item.animatedMedia.images.fixedHeight.height.toInt()
                    dataBinding.imgAnim.layoutParams.apply {
                        this.width = width
                        this.height = height
                    }
                    mGlideRequestManager.load(url).placeholder(R.drawable.load)
                        .into(dataBinding.imgAnim)
                }
                InstagramConstants.MessageType.PLACE_HOLDER.type -> {
                    val dataBinding = holder.binding as LayoutPlaceholderBinding
                    dataBinding.txtMessage.maxWidth = (DisplayUtils.getScreenWidth() * 0.65).toInt()
                    val title = item.placeHolder.title
                    val message = item.placeHolder.message
                    val isLinked = item.placeHolder.isLinked
                    dataBinding.txtTitle.text = title
                    dataBinding.txtMessage.text = message
                    dataBinding.layoutMessage.setOnClickListener {

                    }
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
                    val dataBinding = holder.binding as LayoutFelixShareBinding
                    val image = item.felixShare.video.imageVersions2
                    val user = item.felixShare.video.user
                    val videoUrl = item.felixShare.video.videoVersions[0].url
                    dataBinding.layoutImage.layoutParams.apply {
                        val sizeArray = shareViewModel.getStandardWidthAndHeight(
                            resources.dpToPx(image.candidates[0].width.toFloat()),
                            resources.dpToPx(image.candidates[0].height.toFloat()),
                            0.45f
                        )
                        width = sizeArray[0]
                        height = sizeArray[1]
                    }
                    dataBinding.txtThreadFelixShareUsername.text = user.username
                    mGlideRequestManager.load(image.candidates[0].url)
                        .into(dataBinding.imgMedia)
                    mGlideRequestManager.load(user.profilePicUrl)
                        .into(dataBinding.imgThreadFelixShareProfile)
                    dataBinding.layoutImage.setOnClickListener {
                        PlayVideoActivity.playUrl(activity!!, videoUrl)
                    }
                    dataBinding.btnShareLink.setOnClickListener {
                        activity!!.shareText(videoUrl)
                    }
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {
                    val dataBinding = holder.binding as LayoutEventBinding
                    if (item.actionLog.description.toLowerCase().contains("like")) {
                        gone(dataBinding.txtEventDes)
                    } else {
                        visible(dataBinding.txtEventDes)
                    }
                    dataBinding.txtEventDes.text = item.actionLog.description
                }
                else -> {
                    val dataBinding = holder.binding as LayoutMessageBinding
                    dataBinding.txtMessage.text = String.format(
                        getString(R.string.item_not_supported),
                        item.itemType.replace("_", " ")
                    )
                }
            }

            return item
        }

        private fun showPopupOptions(item: Message, view: View) {
            if (item.userId == thread.viewerId && item.isDelivered) {
                requireContext().vibration(50)
                val dialog = Dialog(view.context)
                val viewDataBinding: LayoutMessageOptionBinding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.layout_message_option,
                    null,
                    false
                )
                viewDataBinding.remove.setOnClickListener {
                    RealTimeService.run(
                        requireContext(),
                        RealTime_ClearItemCache(thread.threadId, item.itemId)
                    )
                    shareViewModel.unsendMessage(
                        shareViewModel.currentThread!!.threadId!!,
                        item.itemId,
                        item.clientContext
                    )
                    dialog.dismiss()
                }
                dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent);
                dialog.setContentView(viewDataBinding.root)
                dialog.setCancelable(true)
                dialog.show()
            }
        }
        /*
        val popupWindow = PopupWindow(this@DirectActivity)
            popupWindow.isOutsideTouchable = true
            popupWindow.isFocusable = true
            val layoutDirectOptionBinding: ViewDataBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.layout_message_option, null, false)
            popupWindow.contentView = layoutDirectOptionBinding.root
            popupWindow.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT)
            );
            val screenHeight = DisplayUtils.getScreenHeight()
            val rectLocaleView = view.locateViewInScreen()
            if((screenHeight - rectLocaleView!!.bottom) < screenHeight / 4){
                if(view.height < screenHeight / 3){
                    popupWindow.showAsDropDown(view, 100, -100 ,Gravity.TOP)
                }else{
                    popupWindow.showAsDropDown(view, view.width/2,-view.height,Gravity.TOP)
                }
            }else{
                popupWindow.showAsDropDown(view, view.width + 20,-40,Gravity.CENTER)
            }
         */

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                items.add(LoadingEvent())
                notifyItemInserted(items.size - 1)
                binding.recyclerviewChats.scrollToPosition(items.size - 1)
            } else {
                for (i in items.indices) {
                    if (items[i] is LoadingEvent) {
                        items.removeAt(i)
                        notifyItemRemoved(i)
                    }
                }
            }
        }

        private fun fullRotation(v: View) {
            val valueAnimator = ValueAnimator.ofFloat(360f, 0f)
            valueAnimator.duration = 500
            valueAnimator.addUpdateListener {
                v.rotation = it.animatedValue as Float
            }
            valueAnimator.start()
        }

        private fun hideAfterSeconds(v: View, duration: Long) {
            val valueAnimator = ValueAnimator.ofInt(1, 0)
            valueAnimator.duration = duration
            valueAnimator.addUpdateListener {
                if (it.animatedValue as Int == 0) {
                    v.visibility = View.GONE
                }
            }
            valueAnimator.start()
        }


        override fun getLayoutIdForPosition(position: Int): Int {
            val item = items[position]
            if (item is DirectDate) {
                return R.layout.layout_direct_date
            }
            if (item is LoadingEvent) {
                return R.layout.layout_loading
            }
            item as Message
            when (item.itemType) {
                InstagramConstants.MessageType.MEDIA.type -> {
                    return R.layout.layout_media
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    return R.layout.layout_raven_media
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    return R.layout.layout_media_share
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    return R.layout.layout_voice_media
                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                    return R.layout.layout_event
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
                    return R.layout.layout_felix_share
                }
                InstagramConstants.MessageType.LINK.type -> {
                    return R.layout.layout_link
                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    return when (item.reelShare.type) {
                        InstagramConstants.ReelType.USER_REEL.type -> {
                            R.layout.layout_reel_share_reply
                        }
                        InstagramConstants.ReelType.REPLY.type -> {
                            R.layout.layout_reel_share_reply
                        }
                        InstagramConstants.ReelType.MENTION.type -> {
                            R.layout.layout_reel_share
                        }
                        InstagramConstants.ReelType.REACTION.type -> {
                            R.layout.layout_reaction_story
                        }
                        else -> {

                            R.layout.layout_reel_share
                        }
                    }
                    /*(item.reelShare.reelType == InstagramConstants.ReelType.MENTION.type)*/
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        return R.layout.layout_reel_share
                    } else
                        return R.layout.layout_story_share_not_linked
                }
                InstagramConstants.MessageType.TEXT.type -> {
                    return R.layout.layout_message
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    return R.layout.layout_like
                }
                InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                    return R.layout.layout_animated_media
                }
                InstagramConstants.MessageType.PLACE_HOLDER.type -> {
                    return R.layout.layout_placeholder
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {
                    return R.layout.layout_event
                }
                else -> {
                    return R.layout.layout_message
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }

    private fun stopAllAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener {
                        //Handle Focus Change
                    }.build()
            )
        } else {
            mAudioManager.requestAudioFocus(
                { focusChange: Int -> },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }


    override fun onNewMessage(message: Message) {
        mAdapter.items.add(0, message)
        mAdapter.notifyItemInserted(0)
        binding.recyclerviewChats.scrollToPosition(0)
    }

    override fun onChangeMessage(message: Message) {
        for (i in mAdapter.items.indices) {
            val message = mAdapter.items[i]
            if (message is Message) {
                if (message.itemId == message.itemId) {
                    mAdapter.notifyItemChanged(i)
                }
            }
        }
    }

    override fun onChangeMessageWithClientContext(message: Message) {
        for (i in mAdapter.items.indices) {
            val message = mAdapter.items[i]
            if (message is Message) {
                if (message.clientContext == message.clientContext) {
                    mAdapter.notifyItemChanged(i)
                }
            }
        }
    }

    override fun realTimeCommand(realTimeCommand: RealTimeCommand) {
        RealTimeService.run(requireActivity(), realTimeCommand)
    }

    override fun removeMessage(itemId: String) {
        for (index in mAdapter.items.indices) {
            val item = mAdapter.items[index]
            if (item is Message && item.itemId == itemId) {
                mAdapter.items.remove(item)
                mAdapter.notifyItemRemoved(index)
                return
            }
        }
    }


}