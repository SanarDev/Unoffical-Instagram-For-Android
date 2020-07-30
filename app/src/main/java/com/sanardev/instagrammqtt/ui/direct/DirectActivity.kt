package com.sanardev.instagrammqtt.ui.direct

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
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.devlomi.record_view.OnRecordListener
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.core.BaseActivity
import com.sanardev.instagrammqtt.core.BaseAdapter
import com.sanardev.instagrammqtt.core.BaseApplication
import com.sanardev.instagrammqtt.customview.doubleclick.DoubleClick
import com.sanardev.instagrammqtt.customview.doubleclick.DoubleClickListener
import com.sanardev.instagrammqtt.databinding.*
import com.sanardev.instagrammqtt.datasource.model.DirectDate
import com.sanardev.instagrammqtt.datasource.model.Message
import com.sanardev.instagrammqtt.datasource.model.event.*
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.extensions.*
import com.sanardev.instagrammqtt.extentions.*
import com.sanardev.instagrammqtt.realtime.commands.RealTimeCommand
import com.sanardev.instagrammqtt.realtime.commands.RealTime_MarkAsSeen
import com.sanardev.instagrammqtt.realtime.commands.RealTime_SendLike
import com.sanardev.instagrammqtt.realtime.commands.RealTime_SendTypingState
import com.sanardev.instagrammqtt.service.realtime.RealTimeService
import com.sanardev.instagrammqtt.ui.fullscreen.FullScreenActivity
import com.sanardev.instagrammqtt.ui.playvideo.PlayVideoActivity
import com.sanardev.instagrammqtt.ui.selectimage.SelectImageDialog
import com.sanardev.instagrammqtt.utils.*
import com.tylersuehr.chips.CircleImageView
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiTextView
import com.vanniktech.emoji.ios.IosEmojiProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.regex.Pattern


class DirectActivity : BaseActivity<ActivityDirectBinding, DirectViewModel>(), ActionListener {

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var httpDataSourceFactory: DataSource.Factory
    private lateinit var dataSourceFactory: DefaultDataSourceFactory
    private lateinit var mPlayarManager: PlayerManager
    private lateinit var adapter: ChatsAdapter
    private lateinit var emojiPopup: EmojiPopup
    private var currentPlayerId: String? = null
    private var isLoading = false
    private var olderMessageExist = true
    private lateinit var mAudioManager: AudioManager
//    private var lastSeenAt: Long = 0

    companion object {
        fun open(context: Context, directBundle: DirectBundle) {
            context.startActivity(Intent(context, DirectActivity::class.java).apply {
                putExtra("data", directBundle)
            })
        }

        const val TAG = "TEST"
        const val PERMISSION_READ_EXTERNAL_STORAGE_CODE = 101
        const val PERMISSION_RECORD_AUDIO_CODE = 102
    }

    override fun layoutRes(): Int {
        return R.layout.activity_direct
    }

    override fun getViewModelClass(): Class<DirectViewModel> {
        return DirectViewModel::class.java
    }

    private val mHandler = Handler()
    private var endTypeAtMs: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        EmojiManager.install(IosEmojiProvider())
        super.onCreate(savedInstanceState)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        attachKeyboardListeners()

        viewModel.init(intent.extras!!.getParcelable<DirectBundle>("data")!!)

        // profile
        binding.txtProfileName.text = viewModel.mThread.threadTitle
        checkUserStatus()
        if (!viewModel.mThread.isGroup) {
            gone(binding.layoutProfileImageGroup)
            visible(binding.imgProfileImage)
            Glide.with(applicationContext).load(viewModel.mThread.users[0].profilePicUrl)
                .into(binding.imgProfileImage)
        } else {
            visible(binding.layoutProfileImageGroup)
            gone(binding.imgProfileImage)
            Glide.with(applicationContext).load(viewModel.mThread.users[1].profilePicUrl)
                .into(binding.profileImageG1)
            Glide.with(applicationContext).load(viewModel.mThread.users[0].profilePicUrl)
                .into(binding.profileImageG2)
        }


        adapter = ChatsAdapter(ArrayList<Any>(), viewModel.getUserProfile())
        binding.recyclerviewChats.adapter = adapter
        layoutManager = (binding.recyclerviewChats.layoutManager as LinearLayoutManager)

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
                this@DirectActivity,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.btnVoice.isListenForRecord = false
        } else {
            binding.btnVoice.isListenForRecord = true
        }
        binding.btnVoice.setOnClickListener {
            ActivityCompat.requestPermissions(
                this@DirectActivity, arrayOf(android.Manifest.permission.RECORD_AUDIO),
                PERMISSION_RECORD_AUDIO_CODE
            )
        }
        binding.recordView.setOnRecordListener(object : OnRecordListener {
            override fun onFinish(recordTime: Long) {
                visible(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto, binding.btnLike)
                gone(binding.recordView)
                viewModel.stopRecording()
            }

            override fun onLessThanSecond() {
                visible(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto, binding.btnLike)
                gone(binding.recordView)
                viewModel.cancelAudioRecording()
            }

            override fun onCancel() {
                vibration(50)
                visible(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto, binding.btnLike)
                gone(binding.recordView)
                viewModel.cancelAudioRecording()
            }

            override fun onStart() {
                vibration(100)
                visible(binding.recordView)
                gone(
                    binding.btnEmoji,
                    binding.edtTextChat,
                    binding.btnAddPhoto,
                    binding.btnLike
                )
                viewModel.startAudioRecording()

            }
        })
        binding.edtTextChat.setOnClickListener {
            emojiPopup.dismiss()
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnLike.setOnClickListener {
            val clientContext = InstagramHashUtils.getClientContext()
            RealTimeService.run(
                this,
                RealTime_SendLike(
                    viewModel.mThread.threadId,
                    clientContext
                )
            )
            val message = MessageGenerator.like(adapter.user.pk!!, clientContext)
            EventBus.getDefault()
                .postSticky(
                    arrayListOf(
                        MessageItemEvent(
                            viewModel.mThread.threadId,
                            message
                        )
                    )
                )
        }

        binding.btnAddPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@DirectActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@DirectActivity,
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_READ_EXTERNAL_STORAGE_CODE
                )
                return@setOnClickListener
            }
            SelectImageDialog {
                viewModel.uploadMedias(it)
            }.show(supportFragmentManager, "Dialog")
        }
        binding.edtTextChat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotBlank()) {
                    RealTimeService.run(
                        this@DirectActivity,
                        RealTime_SendTypingState(
                            viewModel.mThread.threadId,
                            true,
                            InstagramHashUtils.getClientContext()
                        )
                    )
                } else {
                    RealTimeService.run(
                        this@DirectActivity,
                        RealTime_SendTypingState(
                            viewModel.mThread.threadId,
                            false,
                            InstagramHashUtils.getClientContext()
                        )
                    )
                }
            }
        })

        viewModel.mutableLiveData.observe(this, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    if (adapter.items.isNotEmpty()) {
                        return@Observer
                    }
                    visible(binding.progressbar)
                }
                Resource.Status.ERROR -> {
                    if (adapter.items.isEmpty()) {
                        gone(binding.progressbar)
                        visible(binding.includeLayoutNetwork.root)
                    }
                }
                Resource.Status.SUCCESS -> {
                    gone(binding.includeLayoutNetwork.root, binding.progressbar)
                    olderMessageExist = it.data!!.thread!!.oldestCursor != null
                    if (it.data!!.thread!!.releasesMessage.size > adapter.items.size) {
                        adapter.items = it.data!!.thread!!.releasesMessage
                        adapter.notifyDataSetChanged()
                    }
                    isLoading = false
                    adapter.setLoading(isLoading)
                }
            }
        })

        viewModel.mActionListener = this
        viewModel.fileLiveData.observe(this, Observer {


        })

        viewModel.sendMediaLiveData.observe(this, Observer {
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
                        if (adapter.items[totalItemCount - 2] is Message) {
                            viewModel.loadMoreItem(
                                (adapter.items[totalItemCount - 2] as Message).itemId,
                                viewModel.mThread.threadId,
                                viewModel.seqId
                            )
                            isLoading = true
                            adapter.setLoading(isLoading)
                        }
                    }
                }
            }
        })
        initPlayer()

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
        if (!viewModel.mThread.isGroup && viewModel.mThread.active) {
            binding.txtProfileDec.text = getString(R.string.online)
            binding.txtProfileDec.setTextColor(color(R.color.online_color))
        } else {
            if (viewModel.mThread.lastActivityAt == 0.toLong()) {
                binding.txtProfileDec.text = viewModel.mThread.threadTitle
            } else {
                binding.txtProfileDec.text = String.format(
                    getString(R.string.active_at),
                    TimeUtils.convertTimestampToDate(application, viewModel.mThread.lastActivityAt)
                )
                binding.txtProfileDec.setTextColor(color(R.color.text_light))
            }
        }
    }

    override fun onHideKeyboard() {
        RealTimeService.run(
            this@DirectActivity,
            RealTime_SendTypingState(
                viewModel.mThread.threadId,
                false,
                InstagramHashUtils.getClientContext()
            )
        )
    }

    private fun initPlayer() {
        httpDataSourceFactory =
            DefaultHttpDataSourceFactory(Util.getUserAgent(this, "Instagram"))
        dataSourceFactory =
            DefaultDataSourceFactory(this@DirectActivity, Util.getUserAgent(this, "Instagram"))
    }

    fun onEmojiClick(v: View) {
        if (emojiPopup.isShowing) {
            emojiPopup.dismiss()
        } else {
            emojiPopup.toggle()
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onTypingEvent(event: TypingEvent) { /* Do something */
        if (viewModel.mThread.threadId == event.threadId) {
            endTypeAtMs = System.currentTimeMillis() + 3 * 1000
            binding.txtProfileDec.text = getString(R.string.typing)
            binding.txtProfileDec.setTextColor(color(R.color.text_very_light))
            mHandler.postDelayed({
                if (endTypeAtMs < System.currentTimeMillis()) {
                    checkUserStatus()
                }
            }, 3000)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvents(events: MutableList<MessageItemEvent>) {
        for (event in events) {
            if (event.threadId == viewModel.mThread.threadId) {
                checkUserStatus()
                viewModel.onMessageReceive(event)
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageResponseEvent(event: MessageResponse) {
        viewModel.onMessageResponseEvent(event)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateSeenEvent(event: UpdateSeenEvent) {
        if (event.threadId == viewModel.mThread.threadId) {
            for (item in viewModel.mThread.lastSeenAt.entries) {
                item.value.timeStamp = viewModel.convertToStandardTimeStamp(event.seen.timeStamp)
            }
            adapter.notifyDataSetChanged()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPresenceEvent(event: PresenceEvent) { /* Do something */
        if (viewModel.mThread != null && event.userId.toLong() == viewModel.mThread!!.users[0].pk) {
            viewModel.mThread.active = event.isActive
            viewModel.mThread.lastActivityAt = event.lastActivityAtMs.toLong()
            checkUserStatus()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageRemoveEvent(event: MutableList<MessageRemoveEvent>) {
        for(item in event){
            if(viewModel.mThread.threadId == item.threadId){
                viewModel.deleteMessage(item.itemId)
            }
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onConnectionStateEvent(connectionStateEvent: ConnectionStateEvent) {
        when (connectionStateEvent.connection) {
            ConnectionStateEvent.State.CONNECTING -> {
                binding.txtProfileDec.text = getString(R.string.connecting)
            }
            ConnectionStateEvent.State.NETWORK_DISCONNECTED -> {
                binding.txtProfileDec.text = getString(R.string.waiting_for_network)
            }
            ConnectionStateEvent.State.CHANNEL_DISCONNECTED -> {
                binding.txtProfileDec.text = getString(R.string.connecting)
            }
            ConnectionStateEvent.State.NETWORK_CONNECTION_RESET -> {
                binding.txtProfileDec.text = getString(R.string.connecting)
            }
            else -> {

            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }


    inner class ChatsAdapter(var items: MutableList<Any>, var user: InstagramLoggedUser) :
        BaseAdapter() {
        override fun onViewRecycled(holder: BaseViewHolder) {
            if (holder.binding is LayoutVoiceMediaBinding) {
                val dataBinding = holder.binding as LayoutVoiceMediaBinding
                if (dataBinding.seekbarPlay.tag == BaseApplication.currentPlayerId) {
                    BaseApplication.seekbarPlay = null
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
                    includeTime = (holder.binding as LayoutMessageBinding).includeTime
                    imgThreadProfileImage =
                        (holder.binding as LayoutMessageBinding).imgThreadProfileImage
                    layoutParent = (holder.binding as LayoutMessageBinding).layoutParent
                    layoutMessage = (holder.binding as LayoutMessageBinding).layoutMessage
                    includeReaction = (holder.binding as LayoutMessageBinding).includeReaction
                    txtSendername = (holder.binding as LayoutMessageBinding).txtSendername
                }
                InstagramConstants.MessageType.LINK.type -> {
                    includeTime = (holder.binding as LayoutLinkBinding).includeTime
                    layoutParent = (holder.binding as LayoutLinkBinding).layoutParent
                    imgThreadProfileImage =
                        (holder.binding as LayoutLinkBinding).imgThreadProfileImage
                    layoutMessage = (holder.binding as LayoutLinkBinding).layoutMessage
                    includeReaction = (holder.binding as LayoutLinkBinding).includeReaction
                    txtSendername = (holder.binding as LayoutLinkBinding).txtSendername
                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    when (item.reelShare.type) {
                        InstagramConstants.ReelType.REPLY.type -> {
                            includeTime =
                                (holder.binding as LayoutReelShareReplyBinding).includeTime
                            layoutParent =
                                (holder.binding as LayoutReelShareReplyBinding).layoutParent
                            imgThreadProfileImage =
                                (holder.binding as LayoutReelShareReplyBinding).imgThreadProfileImage
                            layoutMessage =
                                (holder.binding as LayoutReelShareReplyBinding).layoutMessage
                            includeReaction =
                                (holder.binding as LayoutReelShareReplyBinding).includeReaction
                        }
                        InstagramConstants.ReelType.MENTION.type -> {
                            includeTime = (holder.binding as LayoutReelShareBinding).includeTime
                            layoutParent = (holder.binding as LayoutReelShareBinding).layoutParent
                            layoutMessage = (holder.binding as LayoutReelShareBinding).layoutMessage
                            imgThreadProfileImage =
                                (holder.binding as LayoutReelShareBinding).imgThreadProfileImage
                            includeReaction =
                                (holder.binding as LayoutReelShareBinding).includeReaction
                        }
                        else -> { // item.reelShare.type == InstagramConstants.ReelType.REACTION.type
                            includeTime = (holder.binding as LayoutReactionStoryBinding).includeTime
                            imgThreadProfileImage =
                                (holder.binding as LayoutReactionStoryBinding).imgThreadProfileImage
                            includeReaction =
                                (holder.binding as LayoutReactionStoryBinding).includeReaction
                            layoutParent =
                                (holder.binding as LayoutReactionStoryBinding).layoutParent
                            layoutMessage =
                                (holder.binding as LayoutReactionStoryBinding).layoutMessage
                        }
                    }
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        includeTime = (holder.binding as LayoutReelShareBinding).includeTime
                        layoutParent = (holder.binding as LayoutReelShareBinding).layoutParent
                        layoutMessage = (holder.binding as LayoutReelShareBinding).layoutMessage
                        imgThreadProfileImage =
                            (holder.binding as LayoutReelShareBinding).imgThreadProfileImage
                        includeReaction = (holder.binding as LayoutReelShareBinding).includeReaction
                    } else {
                        includeTime =
                            (holder.binding as LayoutStoryShareNotLinkedBinding).includeTime
                        layoutParent =
                            (holder.binding as LayoutStoryShareNotLinkedBinding).layoutParent
                        imgThreadProfileImage =
                            (holder.binding as LayoutStoryShareNotLinkedBinding).imgThreadProfileImage
                        layoutMessage =
                            (holder.binding as LayoutStoryShareNotLinkedBinding).layoutMessage
                        includeReaction =
                            (holder.binding as LayoutStoryShareNotLinkedBinding).includeReaction
                    }
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    includeTime = (holder.binding as LayoutVoiceMediaBinding).includeTime
                    layoutParent = (holder.binding as LayoutVoiceMediaBinding).layoutParent
                    imgThreadProfileImage =
                        (holder.binding as LayoutVoiceMediaBinding).imgThreadProfileImage
                    includeReaction =
                        (holder.binding as LayoutVoiceMediaBinding).includeReaction
                    layoutMessage = (holder.binding as LayoutVoiceMediaBinding).layoutMessage
                    txtSendername = (holder.binding as LayoutVoiceMediaBinding).txtSendername
                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                }
                InstagramConstants.MessageType.MEDIA.type -> {
                    includeTime = (holder.binding as LayoutMediaBinding).includeTime
                    layoutParent = (holder.binding as LayoutMediaBinding).layoutParent
                    imgThreadProfileImage =
                        (holder.binding as LayoutMediaBinding).imgThreadProfileImage
                    layoutMessage = (holder.binding as LayoutMediaBinding).layoutMessage
                    includeReaction = (holder.binding as LayoutMediaBinding).includeReaction
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    includeTime = (holder.binding as LayoutRavenMediaBinding).includeTime
                    layoutParent = (holder.binding as LayoutRavenMediaBinding).layoutParent
                    imgThreadProfileImage =
                        (holder.binding as LayoutRavenMediaBinding).imgThreadProfileImage
                    layoutMessage = (holder.binding as LayoutRavenMediaBinding).layoutMessage
                    includeReaction = (holder.binding as LayoutRavenMediaBinding).includeReaction
                    txtSendername = (holder.binding as LayoutRavenMediaBinding).txtSendername
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    includeTime = (holder.binding as LayoutLikeBinding).includeTime
                    layoutParent = (holder.binding as LayoutLikeBinding).layoutParent
                    imgThreadProfileImage =
                        (holder.binding as LayoutLikeBinding).imgThreadProfileImage
                    includeReaction = (holder.binding as LayoutLikeBinding).includeReaction
                    layoutMessage = (holder.binding as LayoutLikeBinding).layoutMessage
//                            holder.binding as LayoutLikeBinding
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    includeTime = (holder.binding as LayoutMediaShareBinding).includeTime
                    layoutParent = (holder.binding as LayoutMediaShareBinding).layoutParent
                    imgThreadProfileImage =
                        (holder.binding as LayoutMediaShareBinding).imgThreadProfileImage
                    layoutMessage = (holder.binding as LayoutMediaShareBinding).layoutMessage
                    includeReaction = (holder.binding as LayoutMediaShareBinding).includeReaction
                    txtSendername = (holder.binding as LayoutMediaShareBinding).txtSendername
                }
                InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                    layoutParent = (holder.binding as LayoutAnimatedMediaBinding).layoutParent
                    includeReaction = (holder.binding as LayoutAnimatedMediaBinding).includeReaction
//                    imgThreadProfileImage =  (holder.binding as LayoutAnimatedMediaBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
                    includeTime = (holder.binding as LayoutFelixShareBinding).includeTime
                    layoutParent = (holder.binding as LayoutFelixShareBinding).layoutParent
                    imgThreadProfileImage =
                        (holder.binding as LayoutFelixShareBinding).imgThreadProfileImage
                    layoutMessage = (holder.binding as LayoutFelixShareBinding).layoutMessage
                    includeReaction = (holder.binding as LayoutFelixShareBinding).includeReaction
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {

                }
                InstagramConstants.MessageType.PLACE_HOLDER.type -> {
                    includeTime = (holder.binding as LayoutPlaceholderBinding).includeTime
                    layoutParent = (holder.binding as LayoutPlaceholderBinding).layoutParent
                    imgThreadProfileImage =
                        (holder.binding as LayoutPlaceholderBinding).imgThreadProfileImage
                    layoutMessage = (holder.binding as LayoutPlaceholderBinding).layoutMessage
                    txtSendername = (holder.binding as LayoutPlaceholderBinding).txtSendername
                }
                else -> {
                    includeTime = (holder.binding as LayoutMessageBinding).includeTime
                    layoutParent = (holder.binding as LayoutMessageBinding).layoutParent
                    imgThreadProfileImage =
                        (holder.binding as LayoutMessageBinding).imgThreadProfileImage
                    layoutMessage = (holder.binding as LayoutMessageBinding).layoutMessage
                    includeReaction = (holder.binding as LayoutMessageBinding).includeReaction
                    txtSendername = (holder.binding as LayoutMessageBinding).txtSendername
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
                        if (likes[i].senderId == adapter.user.pk) {
                            profileUrl = adapter.user.profilePicUrl
                        } else {
                            for (user in viewModel.mThread.users) {
                                if (likes[i].senderId == user.pk) {
                                    profileUrl = user.profilePicUrl
                                }
                            }
                        }
                        if (profileUrl != null) {
                            val image = CircleImageView(this@DirectActivity)
                            image.layoutParams = android.widget.LinearLayout.LayoutParams(
                                resources.dpToPx(25f),
                                resources.dpToPx(25f)
                            )
                            image.setBackgroundResource(R.drawable.bg_stroke_circluar)
                            Glide.with(applicationContext).load(profileUrl).into(image)
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
            if (viewModel.mThread.lastSeenAt != null) {
                for (ls in viewModel.mThread.lastSeenAt.entries) {
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
            if (item.timestamp > lastSeenAt && viewModel.isSeenMessageEnable) {
//                viewModel.markAsSeen(threadId, item.itemId) moshkel ine ke callback barash ok nakardm barate update shodan main activty
                RealTimeService.run(
                    this@DirectActivity,
                    RealTime_MarkAsSeen(
                        viewModel.mThread.threadId,
                        item.itemId
                    )
                )
            }

            if (includeTime != null) {
                includeTime.txtTime.text =
                    viewModel.getTimeFromTimeStamps(item.timestamp)

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
                    Glide.with(applicationContext).load(viewModel.getProfilePic(item.userId))
                        .into(imgThreadProfileImage)
                }
            }
            if (layoutMessage != null) {
                layoutMessage.setOnClickListener(DoubleClick(object : DoubleClickListener {
                    override fun onDoubleClick(view: View?) {
//                        RealTimeService.run(this@DirectActivity,RealTime_SendReaction(item.itemId,"like",item.clientContext,threadId,"created"))
                        viewModel.sendReaction(
                            item.itemId,
                            viewModel.mThread.threadId,
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
                        this@DirectActivity.getDrawable(R.drawable.bg_message)
                } else {
                    layoutMessage.background =
                        this@DirectActivity.getDrawable(R.drawable.bg_message_2)
                }
            }
            if (txtSendername != null) {
                if (viewModel.mThread.isGroup && item.userId != viewModel.mThread.viewerId) {
                    visible(txtSendername)
                    txtSendername.text = viewModel.getUsername(item.userId)
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
                    dataBinding.txtMessage.setTextLinkHTML(this@DirectActivity, link.text)
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
                        Glide.with(applicationContext).load(link.linkContext.linkImageUrl)
                            .placeholder(R.drawable.placeholder_loading)
                            .into(dataBinding.imgLinkImage)
                    }

                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    if (item.reelShare.type == InstagramConstants.ReelType.REPLY.type) {
                        val dataBinding = holder.binding as LayoutReelShareReplyBinding
                        dataBinding.layoutStory.layoutDirection =
                            if (item.userId == viewModel.mThread.viewerId) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
                        if (item.reelShare.media?.imageVersions2 != null) {
                            val image = item.reelShare.media!!.imageVersions2!!.candidates[1]
                            val size =
                                viewModel.getStandardWidthAndHeight(image.width, image.height, 0.2f)
                            Glide.with(applicationContext)
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
                        if (item.userId == viewModel.mThread.viewerId) {
                            dataBinding.layoutParent.gravity = Gravity.RIGHT
                            dataBinding.layoutStory.layoutDirection = View.LAYOUT_DIRECTION_RTL
                            dataBinding.txtReelStatus.text = String.format(
                                getString(R.string.mentioned_person_in_your_story),
                                viewModel.mThread.users[0].username
                            )
                        } else {
                            dataBinding.txtReelStatus.text =
                                getString(R.string.mentioned_you_in_their_story)
                            dataBinding.layoutParent.gravity = Gravity.LEFT
                            dataBinding.layoutStory.layoutDirection = View.LAYOUT_DIRECTION_LTR
                        }
                        dataBinding.includeTime.txtTime.text =
                            viewModel.getTimeFromTimeStamps(
                                item.timestamp
                            )
                        if (item.reelShare.media.imageVersions2 != null) {
                            val image = item.reelShare.media!!.imageVersions2!!.candidates[1]
                            val size =
                                viewModel.getStandardWidthAndHeight(image.width, image.height, 0.2f)
                            val user = item.reelShare.media.user
                            Glide.with(applicationContext).load(image.url)
                                .override(size[0], size[1])
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imgStory)
                            Glide.with(applicationContext).load(user.profilePicUrl)
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
                                viewModel.getStandardWidthAndHeight(image.width, image.height, 0.2f)
                            Glide.with(applicationContext)
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
                            Glide.with(applicationContext)
                                .load(viewModel.getProfilePic(item.userId))
                                .into(dataBinding.imgThreadProfileImage)
                        }

                    }

                    layoutMessage?.setOnClickListener {
                        item.reelShare.media?.videoVersions?.also {
                            PlayVideoActivity.playUrl(this@DirectActivity, it[0].url)
                            return@setOnClickListener
                        }
                        item.reelShare.media?.imageVersions2?.also {
                            FullScreenActivity.openUrl(
                                this@DirectActivity,
                                it.candidates[0].url
                            )
                            return@setOnClickListener
                        }

                    }
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        val dataBinding = holder.binding as LayoutReelShareBinding
                        layoutMessage?.background = null
                        val images = item.storyShare.media.imageVersions2.candidates
                        val user = item.storyShare.media.user
                        val size =
                            viewModel.getStandardWidthAndHeight(
                                images[0].width,
                                images[0].height,
                                0.4f
                            )
                        Glide.with(applicationContext).load(images[0].url)
                            .placeholder(R.drawable.placeholder_loading).into(dataBinding.imgStory)
                        Glide.with(applicationContext).load(user.profilePicUrl)
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
                                viewModel.sendReaction(
                                    item.itemId,
                                    viewModel.mThread.threadId,
                                    item.clientContext
                                )
                            }

                            override fun onSingleClick(view: View?) {
                                if (item.storyShare.media.videoVersions != null) {
                                    PlayVideoActivity.playUrl(
                                        this@DirectActivity,
                                        item.storyShare.media.videoVersions[0].url
                                    )
                                } else {
                                    FullScreenActivity.openUrl(this@DirectActivity, images[0].url)
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
                        width = viewModel.getStandardVoiceWitdh(resources, duration)
                    }

                    dataBinding.seekbarPlay.tag = id
                    if (BaseApplication.currentPlayerId == id) {
                        BaseApplication.seekbarPlay = dataBinding.seekbarPlay
                        BaseApplication.btnPlay = dataBinding.btnPlayPause
                        dataBinding.btnPlayPause.setImageResource(R.drawable.ic_pause_circle)
                    } else {
                        dataBinding.seekbarPlay.progress = 0
                        dataBinding.btnPlayPause.setImageResource(R.drawable.ic_play_circle)
                    }
                    dataBinding.btnPlayPause.setOnClickListener {
                        if (BaseApplication.currentPlayerId != id) {
                            BaseApplication.startPlay(mediaSource)
                            dataBinding.btnPlayPause.setImageResource(R.drawable.ic_pause_circle)
                            BaseApplication.seekbarPlay = dataBinding.seekbarPlay
                            BaseApplication.btnPlay = dataBinding.btnPlayPause
                            BaseApplication.currentPlayerId = id
                            this@DirectActivity.stopAllAudio()
                        } else {
                            BaseApplication.currentPlayerId = ""
                            dataBinding.btnPlayPause.setImageResource(R.drawable.ic_play_circle)
                            BaseApplication.stopPlay()
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
                    if (item.media.isLocal) {
                        val originalSize =
                            MediaUtils.getMediaWidthAndHeight(item.media.localFilePath)
                        val size = viewModel.getStandardWidthAndHeight(
                            originalSize[0],
                            originalSize[1],
                            0.5f
                        )
                        if (item.media.mediaType == 1) {
                            Glide.with(applicationContext).load(File(item.media.localFilePath))
                                .override(size[0], size[1])
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imgMedia)
                            gone(dataBinding.btnPlay)
                        } else {
                            val options = RequestOptions().frame(0)
                            Glide.with(this@DirectActivity).asBitmap()
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
                            viewModel.getStandardWidthAndHeight(images.width, images.height, 0.5f)
                        Glide.with(applicationContext).load(images.url)
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
                                viewModel.sendReaction(
                                    item.itemId,
                                    viewModel.mThread.threadId,
                                    item.clientContext
                                )
                            }

                            override fun onSingleClick(view: View?) {
                                if (item.media.videoVersions != null) {
                                    PlayVideoActivity.playUrl(
                                        this@DirectActivity,
                                        item.media.videoVersions[0].url
                                    )
                                } else {
                                    FullScreenActivity.openUrl(this@DirectActivity, images.url)
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
                            viewModel.markAsSeenRavenMedia(item.itemId, item.clientContext)
                            FullScreenActivity.openUrl(
                                this@DirectActivity,
                                media.imageVersions2.candidates[0].url
                            )
                        }
                    } else if (media.videoVersions != null) {
                        dataBinding.txtMessage.text = getString(R.string.view_video)
                        dataBinding.layoutMedia.setOnClickListener {
                            viewModel.markAsSeenRavenMedia(item.itemId, item.clientContext)
                            PlayVideoActivity.playUrl(
                                this@DirectActivity,
                                media.videoVersions[0].url
                            )
                        }
                    } else {
                        dataBinding.txtMessage.text = getString(R.string.media_expired)
                    }
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    val dataBinding = holder.binding as LayoutLikeBinding
                    dataBinding.txtMessage.text = item.like
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    val dataBinding = holder.binding as LayoutMediaShareBinding
                    val media = item.mediaShare
                    val user = media.user
                    var type = ""
                    val id = item.mediaShare.id
                    if (media.videoVersions != null) {
                        gone(dataBinding.layoutImageView, dataBinding.imgMultipleItem)
                        dataBinding.layoutVideoView.visibility = View.VISIBLE
                        val videoSrc = item.mediaShare.videoVersions[0].url
                        val image = item.mediaShare.imageVersions2.candidates[1].url
                        Glide.with(applicationContext).load(image).into(dataBinding.imgPreviewVideo)
                        type = "video"
                    } else if (media.imageVersions2 != null) {
                        val image = media.imageVersions2
                        gone(dataBinding.layoutVideoView, dataBinding.imgMultipleItem)
                        dataBinding.layoutImageView.visibility = View.VISIBLE
                        Glide.with(applicationContext).load(image.candidates[0].url)
                            .placeholder(R.drawable.placeholder_loading).into(dataBinding.imageView)
                        dataBinding.layoutImageView.layoutParams.apply {
                            val sizeArray = viewModel.getStandardWidthAndHeight(
                                resources.dpToPx(image.candidates[0].width.toFloat()),
                                resources.dpToPx(image.candidates[0].height.toFloat())
                            )
                            width = sizeArray[0]
                            height = sizeArray[1]
                        }
                        type = "image"
                    } else if (media.carouselMedia != null) {
                        val image = media.carouselMedia[0].imageVersions2
                        gone(dataBinding.layoutVideoView)
                        visible(dataBinding.imgMultipleItem)
                        dataBinding.layoutImageView.visibility = View.VISIBLE
                        Glide.with(applicationContext).load(image.candidates[0].url)
                            .placeholder(R.drawable.placeholder_loading).into(dataBinding.imageView)
                        dataBinding.layoutImageView.layoutParams.apply {
                            val sizeArray = viewModel.getStandardWidthAndHeight(
                                resources.dpToPx(image.candidates[0].width.toFloat()),
                                resources.dpToPx(image.candidates[0].height.toFloat())
                            )
                            width = sizeArray[0]
                            height = sizeArray[1]
                        }
                        type = "carouasel"
                    }

                    layoutMessage?.setOnClickListener(DoubleClick(object : DoubleClickListener {
                        override fun onDoubleClick(view: View?) {
//                        RealTimeService.run(this@DirectActivity,RealTime_SendReaction(item.itemId,"like",item.clientContext,threadId,"created"))
                            viewModel.sendReaction(
                                item.itemId,
                                viewModel.mThread.threadId,
                                item.clientContext
                            )
                        }

                        override fun onSingleClick(view: View?) {
                            when (type) {
                                "image" -> {
                                    FullScreenActivity.openUrl(
                                        this@DirectActivity,
                                        media.imageVersions2.candidates[0].url
                                    )
                                }
                                "video" -> {
                                    PlayVideoActivity.playUrl(
                                        this@DirectActivity,
                                        item.mediaShare.videoVersions[0].url
                                    )
                                }
                                "carouasel" -> {
                                    FullScreenActivity.openPost(
                                        this@DirectActivity,
                                        media.id
                                    )
                                }
                            }
                        }
                    }))

                    Glide.with(applicationContext).load(user.profilePicUrl)
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
                    Glide.with(applicationContext).load(url).placeholder(R.drawable.load)
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
                        val sizeArray = viewModel.getStandardWidthAndHeight(
                            resources.dpToPx(image.candidates[0].width.toFloat()),
                            resources.dpToPx(image.candidates[0].height.toFloat()),
                            0.45f
                        )
                        width = sizeArray[0]
                        height = sizeArray[1]
                    }
                    dataBinding.txtThreadFelixShareUsername.text = user.username
                    Glide.with(applicationContext).load(image.candidates[0].url)
                        .into(dataBinding.imgMedia)
                    Glide.with(applicationContext).load(user.profilePicUrl)
                        .into(dataBinding.imgThreadFelixShareProfile)
                    dataBinding.layoutImage.setOnClickListener {
                        PlayVideoActivity.playUrl(this@DirectActivity, videoUrl)
                    }
                    dataBinding.btnShareLink.setOnClickListener {
                        shareText(videoUrl)
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
                    dataBinding.txtMessage.text = "Sorry ${item.itemType} not support"
                }
            }

            return item
        }

        private fun showPopupOptions(item: Message, view: View) {
            if (item.userId == viewModel.mThread.viewerId && item.isDelivered) {
                vibration(50)
                val dialog = Dialog(view.context)
                val viewDataBinding: LayoutMessageOptionBinding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.layout_message_option,
                    null,
                    false
                )
                viewDataBinding.remove.setOnClickListener {
                    viewModel.unsendMessage(item.itemId, item.clientContext)
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
        adapter.items.add(0, message)
        adapter.notifyItemInserted(0)
        binding.recyclerviewChats.scrollToPosition(0)
    }

    override fun onChangeMessage(message: Message) {
        for (i in adapter.items.indices) {
            val message = adapter.items[i]
            if (message is Message) {
                if (message.itemId == message.itemId) {
                    adapter.notifyItemChanged(i)
                }
            }
        }
    }

    override fun onChangeMessageWithClientContext(message: Message) {
        for (i in adapter.items.indices) {
            val message = adapter.items[i]
            if (message is Message) {
                if (message.clientContext == message.clientContext) {
                    adapter.notifyItemChanged(i)
                }
            }
        }
    }

    override fun realTimeCommand(realTimeCommand: RealTimeCommand) {
        RealTimeService.run(this@DirectActivity, realTimeCommand)
    }

    override fun removeMessage(itemId: String) {
        for (index in adapter.items.indices) {
            val item = adapter.items[index]
            if (item is Message && item.itemId == itemId) {
                adapter.items.remove(item)
                adapter.notifyItemRemoved(index)
                return
            }
        }
    }

}