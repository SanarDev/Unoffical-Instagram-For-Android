package com.sanardev.instagrammqtt.ui.direct

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.record_view.OnRecordListener
import com.github.piasy.rxandroidaudio.RxAudioPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.core.BaseAdapter
import com.sanardev.instagrammqtt.databinding.*
import com.sanardev.instagrammqtt.datasource.model.DirectDate
import com.sanardev.instagrammqtt.datasource.model.Message
import com.sanardev.instagrammqtt.datasource.model.Thread
import com.sanardev.instagrammqtt.datasource.model.event.*
import com.sanardev.instagrammqtt.datasource.model.realtime.RealTime_MarkAsSeen
import com.sanardev.instagrammqtt.datasource.model.realtime.RealTime_SendLike
import com.sanardev.instagrammqtt.datasource.model.realtime.RealTime_SendMessage
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.extensions.*
import com.sanardev.instagrammqtt.service.realtime.RealTimeIntent
import com.sanardev.instagrammqtt.service.realtime.RealTimeService
import com.sanardev.instagrammqtt.utils.*
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.ios.IosEmojiProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import run.tripa.android.extensions.dpToPx
import run.tripa.android.extensions.vibration
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class DirectActivity : BaseActivity<ActivityDirectBinding, DirectViewModel>() {

    private var thread: Thread? = null
    private lateinit var threadId: String
    private var username: String? = null
    private var profileImage: String? = null

    private lateinit var dataSourceFactory: DataSource.Factory
    private val mRxAudioPlayer = RxAudioPlayer.getInstance()
    private lateinit var mPlayarManager: PlayerManager
    private lateinit var adapter: ChatsAdapter
    private lateinit var emojiPopup: EmojiPopup
    private val players = HashMap<String, SimpleExoPlayer>()
    private var currentPlayerId: String? = null
    private var isLoading = false
    private var olderMessageExist = true
    private var lastSeenAt: Long = 0

    companion object {
        fun open(context: Context, bundle: Bundle) {
            context.startActivity(Intent(context, DirectActivity::class.java).apply {
                putExtras(bundle)
            })
        }

        const val TAG = "TEST"
    }

    override fun layoutRes(): Int {
        return R.layout.activity_direct
    }

    override fun getViewModelClass(): Class<DirectViewModel> {
        return DirectViewModel::class.java
    }

    private val mHandler = Handler()
    private var endTypeAtMs: Long = 0

    private var currentAudioId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        EmojiManager.install(IosEmojiProvider())
        super.onCreate(savedInstanceState)

        threadId = intent.extras!!.getString("thread_id")!!
        profileImage = intent.extras!!.getString("profile_image")
        username = intent.extras!!.getString("username")
        val seqID = intent.extras!!.getInt("seq_id")

        binding.txtProfileName.text = username
        Picasso.get().load(profileImage).into(binding.imgProfileImage)
        viewModel.init(threadId, seqID)

        adapter = ChatsAdapter(ArrayList<Any>(), viewModel.getUserProfile())
        binding.recyclerviewChats.adapter = adapter

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
        binding.recordView.setOnRecordListener(object : OnRecordListener {
            override fun onFinish(recordTime: Long) {
                visible(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto)
                gone(binding.recordView)
                viewModel.stopRecording()
            }

            override fun onLessThanSecond() {
                visible(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto)
                gone(binding.recordView)
                viewModel.cancelAudioRecording()
            }

            override fun onCancel() {
                vibration(50)
                visible(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto)
                gone(binding.recordView)
                viewModel.cancelAudioRecording()
            }

            override fun onStart() {
                vibration(100)
                visible(binding.recordView)
                gone(binding.btnEmoji, binding.edtTextChat, binding.btnAddPhoto)
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
            RealTimeService.run(this,RealTime_SendLike(threadId,clientContext))
            val message = MessageGenerator.like(adapter.user.pk!!,clientContext)
            EventBus.getDefault().postSticky(MessageEvent(threadId, message))
        }

        viewModel.mutableLiveData.observe(this, Observer {
            if (it.status == Resource.Status.LOADING) {
                binding.progressbar.visibility = View.VISIBLE
            }
            binding.progressbar.visibility = View.GONE
            if (it.status == Resource.Status.SUCCESS) {
                thread = it.data!!.thread
                if (thread!!.lastSeenAt[thread!!.users[0].pk.toString()] != null) {
                    lastSeenAt =
                        viewModel.convertToStandardTimeStamp(thread!!.lastSeenAt[thread!!.users[0].pk.toString()]!!.timeStamp)
                }
                olderMessageExist = it . data !!. thread !!. oldestCursor != null
                if (it.data!!.thread!!.releasesMessage.size > adapter.items.size) {
                    adapter.items = it.data!!.thread!!.releasesMessage
                    adapter.notifyDataSetChanged()
                }
                isLoading = false
                adapter.setLoading(isLoading)
            }
        })

        viewModel.mutableLiveDataAddMessage.observe(this, Observer {
            adapter.items.add(0, it)
            adapter.notifyItemInserted(0)
            binding.recyclerviewChats.scrollToPosition(0)
        })
        viewModel.fileLiveData.observe(this, Observer {


        })

        binding.recyclerviewChats.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = (binding.recyclerviewChats.layoutManager as LinearLayoutManager)
                val visibleItemCount = binding.recyclerviewChats.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemIndex = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && olderMessageExist) {
                    if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
                        if (totalItemCount - 2 < 0) {
                            return
                        }
                        if (adapter.items[totalItemCount - 2] is Message) {
                            viewModel.loadMoreItem(
                                (adapter.items[totalItemCount - 2] as Message).itemId,
                                threadId,
                                seqID
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

    private fun initPlayer() {
        dataSourceFactory =
            DefaultHttpDataSourceFactory(Util.getUserAgent(this, "Instagram"))
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
        if (threadId == event.threadId) {
            endTypeAtMs = System.currentTimeMillis() + 3 * 1000
            binding.txtProfileDec.text = getString(R.string.typing)
            mHandler.postDelayed({
                if (endTypeAtMs < System.currentTimeMillis()) {
                    binding.txtProfileDec.text = ""
                }
            }, 3000)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.threadId == threadId) {
            viewModel.onMessageReceive(event)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageResponseEvent(event: MessageResponseEvent) {
        if (event.action == "item_ack" && event.status == "ok" && event.payload.threadId == threadId) {
            for (i in adapter.items.indices) {
                val item = adapter.items[i]
                if (item is Message && item.clientContext == event.payload.clientContext) {
                    item.timestamp = event.payload.timestamp.toLong()
                    item.isDelivered = true
                    adapter.notifyItemChanged(i)
                }
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateSeenEvent(event: UpdateSeenEvent) {
        if (event.threadId == threadId) {
            lastSeenAt = viewModel.convertToStandardTimeStamp(event.seen.timeStamp)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
        if (currentPlayerId != null) {
            for (item in players.entries) {
                if (item.key == currentPlayerId) {
                    item.value.playWhenReady = true
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
        for (item in players.entries) {
            item.value.playWhenReady = false
        }
    }

    inner class ChatsAdapter(var items: MutableList<Any>, var user: InstagramLoggedUser) :
        BaseAdapter() {
        override fun onViewRecycled(holder: BaseViewHolder) {
            if (holder.binding is LayoutMediaShareBinding) {
                val dataBinding = holder.binding as LayoutMediaShareBinding
                if (dataBinding.videoView.player != null) {
                    (dataBinding.videoView.player as SimpleExoPlayer).volume = 0f
                    for (item in players.entries) {
                        if (item.value == dataBinding.videoView.player) {
                            players.remove(item.key)
                            return
                        }
                    }
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


            val includeTime = when (item.itemType) {
                InstagramConstants.MessageType.TEXT.type -> {
                    (holder.binding as LayoutMessageBinding).includeTime
                }
                InstagramConstants.MessageType.LINK.type -> {
                    (holder.binding as LayoutLinkBinding).includeTime
                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    when (item.reelShare.type) {
                        InstagramConstants.ReelType.REPLY.type -> {
                            (holder.binding as LayoutReelShareReplyBinding).includeTime
                        }
                        InstagramConstants.ReelType.MENTION.type -> {
                            (holder.binding as LayoutReelShareBinding).includeTime
                        }
                        else -> { // item.reelShare.type == InstagramConstants.ReelType.REACTION.type
                            (holder.binding as LayoutReactionStoryBinding).includeTime
                        }
                    }
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        (holder.binding as LayoutReelShareBinding).includeTime
                    } else
                        (holder.binding as LayoutStoryShareNotLinkedBinding).includeTime
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    (holder.binding as LayoutVoiceMediaBinding).includeTime
                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                    null
                }
                InstagramConstants.MessageType.MEDIA.type -> {
                    (holder.binding as LayoutMediaBinding).includeTime
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    (holder.binding as LayoutRavenMediaBinding).includeTime
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    (holder.binding as LayoutLikeBinding).includeTime
//                            holder.binding as LayoutLikeBinding
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    (holder.binding as LayoutMediaShareBinding).includeTime
                }
                InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                    null
//                            (holder.binding as LayoutAnimatedMediaBinding)
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
//                            (holder.binding as LayoutFelixShareBinding)
                    null
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {
                    null
                }
                else -> {
                    (holder.binding as LayoutMessageBinding).includeTime
                }
            }
            val layoutParent = when (item.itemType) {
                InstagramConstants.MessageType.TEXT.type -> {
                    (holder.binding as LayoutMessageBinding).layoutParent
                }
                InstagramConstants.MessageType.LINK.type -> {
                    (holder.binding as LayoutLinkBinding).layoutParent
                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    when (item.reelShare.type) {
                        InstagramConstants.ReelType.REPLY.type -> {
                            (holder.binding as LayoutReelShareReplyBinding).layoutParent
                        }
                        InstagramConstants.ReelType.MENTION.type -> {
                            (holder.binding as LayoutReelShareBinding).layoutParent
                        }
                        else -> { // item.reelShare.type == InstagramConstants.ReelType.REACTION.type
                            (holder.binding as LayoutReactionStoryBinding).layoutParent
                        }
                    }
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        (holder.binding as LayoutReelShareBinding).layoutParent
                    } else
                        (holder.binding as LayoutStoryShareNotLinkedBinding).layoutParent
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    (holder.binding as LayoutVoiceMediaBinding).layoutParent
                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                    null
                }
                InstagramConstants.MessageType.MEDIA.type -> {
                    (holder.binding as LayoutMediaBinding).layoutParent
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    (holder.binding as LayoutRavenMediaBinding).layoutParent
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    (holder.binding as LayoutLikeBinding).layoutParent
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    (holder.binding as LayoutMediaShareBinding).layoutParent
                }
                InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                    (holder.binding as LayoutAnimatedMediaBinding).layoutParent
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
                    (holder.binding as LayoutFelixShareBinding).layoutParent
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {
                    null
                }
                else -> {
                    (holder.binding as LayoutMessageBinding).layoutParent
                }
            }
            val imgThreadProfileImage = when (item.itemType) {
                InstagramConstants.MessageType.TEXT.type -> {
                    (holder.binding as LayoutMessageBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.LINK.type -> {
                    (holder.binding as LayoutLinkBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    when (item.reelShare.type) {
                        InstagramConstants.ReelType.REPLY.type -> {
                            (holder.binding as LayoutReelShareReplyBinding).imgThreadProfileImage
                        }
                        InstagramConstants.ReelType.MENTION.type -> {
                            (holder.binding as LayoutReelShareBinding).imgThreadProfileImage
                        }
                        else -> { // item.reelShare.type == InstagramConstants.ReelType.REACTION.type
                            (holder.binding as LayoutReactionStoryBinding).imgThreadProfileImage
                        }
                    }
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        (holder.binding as LayoutReelShareBinding).imgThreadProfileImage
                    } else
                        (holder.binding as LayoutStoryShareNotLinkedBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    (holder.binding as LayoutVoiceMediaBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                    null
                }
                InstagramConstants.MessageType.MEDIA.type -> {
                    (holder.binding as LayoutMediaBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    (holder.binding as LayoutRavenMediaBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    (holder.binding as LayoutLikeBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    (holder.binding as LayoutMediaShareBinding).imgThreadProfileImage
                }
                InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
//                            (holder.binding as LayoutAnimatedMediaBinding).imgThreadProfileImage
                    null
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
//                            (holder.binding as LayoutFelixShareBinding).imgThreadProfileImage
                    null
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {
                    null
                }
                else -> {
                    (holder.binding as LayoutMessageBinding).imgThreadProfileImage
                }
            }
            val layoutMessage = when (item.itemType) {
                InstagramConstants.MessageType.TEXT.type -> {
                    (holder.binding as LayoutMessageBinding).layoutMessage
                }
                InstagramConstants.MessageType.LINK.type -> {
                    (holder.binding as LayoutLinkBinding).layoutMessage
                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    when (item.reelShare.type) {
                        InstagramConstants.ReelType.REPLY.type -> {
                            (holder.binding as LayoutReelShareReplyBinding).layoutMessage
                        }
                        InstagramConstants.ReelType.MENTION.type -> {
                            null
                        }
                        else -> { // item.reelShare.type == InstagramConstants.ReelType.REACTION.type
                            null
                        }
                    }
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    null
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    null
                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                    null
                }
                InstagramConstants.MessageType.MEDIA.type -> {
                    (holder.binding as LayoutMediaBinding).layoutMessage
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    null
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    null
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    (holder.binding as LayoutMediaShareBinding).layoutMessage
                }
                InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                    null
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
                    (holder.binding as LayoutFelixShareBinding).layoutMessage
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {
                    null
                }
                else -> {
                    (holder.binding as LayoutMessageBinding).layoutMessage
                }
            }
            val includeReaction = when (item.itemType) {
                InstagramConstants.MessageType.TEXT.type -> {
                    (holder.binding as LayoutMessageBinding).includeReaction
                }
                InstagramConstants.MessageType.LINK.type -> {
                    (holder.binding as LayoutLinkBinding).includeReaction
                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    when (item.reelShare.type) {
                        InstagramConstants.ReelType.REPLY.type -> {
                            (holder.binding as LayoutReelShareReplyBinding).includeReaction
                        }
                        InstagramConstants.ReelType.MENTION.type -> {
                            (holder.binding as LayoutReelShareBinding).includeReaction
                            null
                        }
                        else -> { // item.reelShare.type == InstagramConstants.ReelType.REACTION.type
                            (holder.binding as LayoutReactionStoryBinding).includeReaction
                        }
                    }
                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        (holder.binding as LayoutReelShareBinding).includeReaction
                    } else
                        (holder.binding as LayoutStoryShareNotLinkedBinding).includeReaction
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    (holder.binding as LayoutVoiceMediaBinding).includeReaction
                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                    null
                }
                InstagramConstants.MessageType.MEDIA.type -> {
                    (holder.binding as LayoutMediaBinding).includeReaction
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    (holder.binding as LayoutRavenMediaBinding).includeReaction
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    null
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    (holder.binding as LayoutMediaShareBinding).includeReaction
                }
                InstagramConstants.MessageType.ANIMATED_MEDIA.type -> {
                    null
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
                    (holder.binding as LayoutFelixShareBinding).includeReaction
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {
                    null
                }
                else -> {
                    (holder.binding as LayoutMessageBinding).includeReaction
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
                    }
                    includeReaction.layoutReactionsParent.visibility = View.VISIBLE
                }
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
                        RealTimeService.run(this@DirectActivity,RealTime_MarkAsSeen(threadId,item.itemId))
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
                    if (item.itemType == InstagramConstants.MessageType.REEL_SHARE.type) {
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
                    Picasso.get().load(profileImage).into(imgThreadProfileImage)
                }
            }
            if (layoutMessage != null) {
                if (item.userId == user.pk) {
                    layoutMessage.background =
                        this@DirectActivity.getDrawable(R.drawable.bg_message)
                } else {
                    layoutMessage.background =
                        this@DirectActivity.getDrawable(R.drawable.bg_message_2)
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
                    dataBinding.txtMessage.setTextHTML(this@DirectActivity, link.text)
                    if (link.linkContext.linkTitle.isNullOrBlank()) {
                        gone(dataBinding.layoutLinkDes)
                    } else {
                        visible(dataBinding.layoutLinkDes)
                        dataBinding.txtLinkSummary.text = link.linkContext.linkSummary
                        dataBinding.txtLinkTitle.text = link.linkContext.linkTitle
                    }
                    if (link.linkContext.linkImageUrl.isNullOrBlank()) {
                        dataBinding.imgLinkImage.visibility = View.GONE
                    } else {
                        Picasso.get().load(link.linkContext.linkImageUrl)
                            .placeholder(R.drawable.placeholder_loading)
                            .into(dataBinding.imgLinkImage)
                    }

                }
                InstagramConstants.MessageType.REEL_SHARE.type -> {
                    if (item.reelShare.type == InstagramConstants.ReelType.REPLY.type) {
                        val dataBinding = holder.binding as LayoutReelShareReplyBinding
                        if (item.reelShare.media?.imageVersions2 != null) {
                            dataBinding.imgStory.layoutParams.apply {
                                width = 300
                                height = 400
                            }
                            Picasso.get()
                                .load(item.reelShare.media.imageVersions2.candidates[0].url)
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
                        if (item.userId == user.pk) {
                            dataBinding.layoutParent.gravity = Gravity.RIGHT
                            dataBinding.txtReelStatus.text = String.format(
                                getString(R.string.mentioned_person_in_your_story), username
                            )
                        } else {
                            dataBinding.txtReelStatus.text =
                                getString(R.string.mentioned_you_in_their_story)
                            dataBinding.layoutParent.gravity = Gravity.LEFT
                        }
                        dataBinding.includeTime.txtTime.text =
                            viewModel.getTimeFromTimeStamps(
                                item.timestamp
                            )
                        if (item.reelShare.media.imageVersions2 != null) {
                            val images = item.reelShare.media.imageVersions2.candidates
                            val user = item.reelShare.media.user
                            Picasso.get().load(images[1].url)
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imgStory)
                            Picasso.get().load(user.profilePicUrl).into(dataBinding.imgProfile)
                            dataBinding.txtUsername.text = user.username
                        } else {
                            visible(dataBinding.txtNoDataAvailable)
                            gone(dataBinding.imgStory, dataBinding.imgProfile)
                        }

                        if (item.timestamp <= lastSeenAt) {
                            dataBinding.includeTime.imgMessageStatus.setImageResource(R.drawable.ic_check_multiple)
                            dataBinding.includeTime.imgMessageStatus.setColorFilter(color(R.color.checked_message_color))
                        } else {

                            dataBinding.includeTime.imgMessageStatus.setImageResource(R.drawable.ic_check)
                            dataBinding.includeTime.imgMessageStatus.setColorFilter(color(R.color.text_light))
                        }

                        if (item.userId == user.pk) {
                            dataBinding.includeTime.imgMessageStatus.visibility = View.VISIBLE
                            dataBinding.layoutParent.gravity = Gravity.RIGHT
                            dataBinding.layoutParent.layoutDirection = View.LAYOUT_DIRECTION_RTL
                            dataBinding.imgThreadProfileImage.visibility = View.GONE
                        } else {
                            dataBinding.includeTime.imgMessageStatus.visibility = View.GONE
                            dataBinding.layoutParent.gravity = Gravity.LEFT
                            dataBinding.layoutParent.layoutDirection = View.LAYOUT_DIRECTION_LTR

                            dataBinding.imgThreadProfileImage.visibility = View.VISIBLE
                            Picasso.get().load(profileImage).into(dataBinding.imgThreadProfileImage)
                        }

                    }
                    if (item.reelShare.type == InstagramConstants.ReelType.REACTION.type) {
                        val dataBinding = holder.binding as LayoutReactionStoryBinding
                        if (item.reelShare.media?.imageVersions2 != null) {
                            dataBinding.imgStory.layoutParams.apply {
                                width = 300
                                height = 400
                            }
                            Picasso.get()
                                .load(item.reelShare.media.imageVersions2.candidates[0].url)
                                .placeholder(R.drawable.placeholder_loading)
                                .into(dataBinding.imgStory)
                        } else {

//                            gone(dataBinding.layoutImgStory)
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


                        if (item.timestamp <= lastSeenAt) {
                            dataBinding.includeTime.imgMessageStatus.setImageResource(R.drawable.ic_check_multiple)
                            dataBinding.includeTime.imgMessageStatus.setColorFilter(color(R.color.checked_message_color))
                        } else {

                            dataBinding.includeTime.imgMessageStatus.setImageResource(R.drawable.ic_check)
                            dataBinding.includeTime.imgMessageStatus.setColorFilter(color(R.color.text_light))
                        }

                        if (item.userId == user.pk) {
                            dataBinding.includeTime.imgMessageStatus.visibility = View.VISIBLE
                            dataBinding.txtReactedStatus.text =
                                getString(R.string.you_reacted_to_their_story)
                            dataBinding.layoutParent.gravity = Gravity.RIGHT
                            dataBinding.layoutParent.layoutDirection = View.LAYOUT_DIRECTION_RTL
                            dataBinding.imgThreadProfileImage.visibility = View.GONE
                        } else {
                            dataBinding.includeTime.imgMessageStatus.visibility = View.GONE
                            dataBinding.txtReactedStatus.text =
                                getString(R.string.reacted_to_your_story)
                            dataBinding.layoutParent.layoutDirection = View.LAYOUT_DIRECTION_LTR

                            dataBinding.imgThreadProfileImage.visibility = View.VISIBLE
                            Picasso.get().load(profileImage).into(dataBinding.imgThreadProfileImage)
                            dataBinding.layoutParent.gravity = Gravity.LEFT
                        }

                    }

                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        val dataBinding = holder.binding as LayoutReelShareBinding
                        val images = item.storyShare.media.imageVersions2.candidates
                        val user = item.storyShare.media.user
                        Picasso.get().load(images[1].url)
                            .placeholder(R.drawable.placeholder_loading).into(dataBinding.imgStory)
                        Picasso.get().load(user.profilePicUrl).into(dataBinding.imgProfile)
                        dataBinding.txtUsername.text = user.username
                        dataBinding.txtReelStatus.text = String.format(
                            getString(R.string.send_story_from), user.username
                        )
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
                    val audioSrc = item.voiceMediaData.voiceMedia.audio.audioSrc
                    val id = item.voiceMediaData.voiceMedia.id
                    if (players[id] == null) {
                        players[id] = SimpleExoPlayer.Builder(this@DirectActivity).build()
                    }
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(audioSrc))
                    players[id]!!.prepare(mediaSource)
                    dataBinding.videoView.player = players[id]
                    players[id]!!.addListener(object : Player.EventListener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (isPlaying) {
                                stopForPlay(id)
                            }
                        }
                    })
                    dataBinding.layoutVoice.minimumWidth =
                        (DisplayUtils.getScreenWidth() * 0.6).toInt()

                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                    val dataBinding = holder.binding as LayoutEventBinding
                    dataBinding.txtEventDes.text = item.videoCallEvent.description
                }
                InstagramConstants.MessageType.MEDIA.type -> {
                    val dataBinding = holder.binding as LayoutMediaBinding
                    val images = item.media.imageVersions2.candidates
                    Picasso.get().load(images[0].url).placeholder(R.drawable.placeholder_loading)
                        .into(dataBinding.imgMedia)
                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {
                    Log.i("TEST", "TEST")
                }
                InstagramConstants.MessageType.LIKE.type -> {
                    val dataBinding = holder.binding as LayoutLikeBinding
                    dataBinding.txtMessage.text = item.like
                }
                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    val dataBinding = holder.binding as LayoutMediaShareBinding
                    val media = item.mediaShare
                    val user = media.user
                    val id = item.mediaShare.id
                    if (media.videoVersions != null) {
                        dataBinding.layoutVideoView.visibility = View.VISIBLE
                        val videoSrc = item.mediaShare.videoVersions[1].url
                        if (players[id] == null) {
                            players[id] = SimpleExoPlayer.Builder(this@DirectActivity).build()
                        }
                        val mediaSource: MediaSource =
                            ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(Uri.parse(videoSrc))
                        players[id]!!.prepare(mediaSource)
                        players[id]!!.playWhenReady = true
                        players[id]!!.volume = 0f
                        dataBinding.videoView.setOnClickListener {
                            dataBinding.imgVolume.visibility = View.VISIBLE
                            if (players[id]!!.volume == 0f) {
                                dataBinding.imgVolume.setImageResource(R.drawable.ic_volume_high)
                                players[id]!!.volume = 100f
                                players[id]!!.playWhenReady = true
                                this@DirectActivity.stopForPlay(id)
                            } else {
                                dataBinding.imgVolume.setImageResource(R.drawable.ic_volume_off)
                                players[id]!!.volume = 0f
                            }
                            hideAfterSeconds(dataBinding.imgVolume, 1500)
                        }

                        dataBinding.btnReplay.setOnClickListener {
                            fullRotation(dataBinding.btnReplay)
                            players[id]!!.seekTo(0)
                            players[id]!!.playWhenReady = true
                        }
                        dataBinding.layoutVideoView.layoutParams.apply {
                            val sizeArray = viewModel.getStandardWidthAndHeight(
                                resources.dpToPx(media.videoVersions[1].width.toFloat()),
                                resources.dpToPx(media.videoVersions[1].height.toFloat())
                            )
                            width = sizeArray[0]
                            height = sizeArray[1]
                        }
                        dataBinding.videoView.player = players[id]!!
                    } else if (media.imageVersions2 != null) {
                        val image = media.imageVersions2
                        dataBinding.layoutImageView.visibility = View.VISIBLE
                        Picasso.get().load(image.candidates[0].url)
                            .placeholder(R.drawable.placeholder_loading).into(dataBinding.imageView)
                        dataBinding.layoutImageView.layoutParams.apply {
                            val sizeArray = viewModel.getStandardWidthAndHeight(
                                resources.dpToPx(media.imageVersions2.candidates[1].width.toFloat()),
                                resources.dpToPx(media.imageVersions2.candidates[1].height.toFloat())
                            )
                            width = sizeArray[0]
                            height = sizeArray[1]
                        }
                    }

                    Picasso.get().load(user.profilePicUrl).into(dataBinding.imgProfile)
                    dataBinding.txtUsername.text = user.username
                    dataBinding.txtCaption.text = media.caption.text

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
                    Picasso.get().load(url).placeholder(R.drawable.load).into(dataBinding.imgAnim)
                }
                InstagramConstants.MessageType.FELIX_SHARE.type -> {
                    Log.i("TEST", "TEST")
                }
                InstagramConstants.MessageType.ACTION_LOG.type -> {

                }
                else -> {
                    val dataBinding = holder.binding as LayoutMessageBinding
                    dataBinding.txtMessage.text = "Sorry ${item.itemType} not support"
                }
            }

            return item
        }

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                items.add(LoadingEvent())
                notifyItemInserted(items.size - 1)
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
                InstagramConstants.MessageType.ACTION_LOG.type -> {
                    return R.layout.layout_nothing
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

    private fun stopForPlay(id: String) {
        currentPlayerId = id
        for (item in players.entries) {
            if (item.key != id) {
                item.value.volume = 0f
                item.value.playWhenReady = false
                item.value.seekTo(0)
            } else {
                item.value.volume = 100f
            }
        }
    }


    fun onSendMessageClick(v: View) {
        val clientContext = InstagramHashUtils.getClientContext()
        RealTimeService.run(this,RealTime_SendMessage(threadId,clientContext,binding.edtTextChat.text.toString()))
        val message = MessageGenerator.text(binding.edtTextChat.text.toString(),adapter.user.pk!!,clientContext)
        EventBus.getDefault().postSticky(MessageEvent(threadId, message))
        binding.edtTextChat.setText("")
    }


}