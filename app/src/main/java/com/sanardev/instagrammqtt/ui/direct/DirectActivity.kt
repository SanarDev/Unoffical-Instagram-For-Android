package com.sanardev.instagrammqtt.ui.direct

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TimeUtils
import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.devlomi.record_view.OnRecordListener
import com.github.piasy.rxandroidaudio.RxAudioPlayer
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
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
import com.sanardev.instagrammqtt.datasource.model.response.InstagramLoggedUser
import com.sanardev.instagrammqtt.extensions.color
import com.sanardev.instagrammqtt.extensions.gone
import com.sanardev.instagrammqtt.extensions.visible
import com.sanardev.instagrammqtt.utils.PlayerManager
import com.sanardev.instagrammqtt.utils.Resource
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.ios.IosEmojiProvider
import run.tripa.android.extensions.dpToPx
import run.tripa.android.extensions.vibration
import java.util.concurrent.TimeUnit


class DirectActivity : BaseActivity<ActivityDirectBinding, DirectViewModel>() {

    private var username: String? = null
    private var profileImage: String? = null

    private lateinit var dataSourceFactory: DataSource.Factory
    private val mRxAudioPlayer = RxAudioPlayer.getInstance()
    private lateinit var mPlayarManager: PlayerManager
    private lateinit var adapter: ChatsAdapter
    private lateinit var emojiPopup: EmojiPopup

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

    private var currentAudioId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        EmojiManager.install(IosEmojiProvider())
        super.onCreate(savedInstanceState)

        val threadId = intent.extras!!.getString("thread_id")!!
        profileImage = intent.extras!!.getString("profile_image")
        username = intent.extras!!.getString("username")
        val seqID = intent.extras!!.getInt("seq_id")
        val lastActivityAt = intent.extras!!.getLong("last_activity_at")

        binding.txtProfileName.text = username
        Picasso.get().load(profileImage).into(binding.imgProfileImage)
        viewModel.init(threadId, seqID)

        adapter = ChatsAdapter(emptyList(), viewModel.getUserProfile())
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
        viewModel.liveData.observe(this, Observer {
            if (it.status == Resource.Status.LOADING) {
                binding.progressbar.visibility = View.VISIBLE
            }
            binding.progressbar.visibility = View.GONE
            if (it.status == Resource.Status.SUCCESS) {
                adapter.items = it.data!!.thread!!.releasesMessage
                adapter.notifyDataSetChanged()
            }
        })
        viewModel.fileLiveData.observe(this, Observer {


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

    inner class ChatsAdapter(var items: List<Any>, var user: InstagramLoggedUser) :
        BaseAdapter() {
        override fun onViewRecycled(holder: BaseViewHolder) {
            if (holder.binding is LayoutMediaShareBinding) {
                val dataBinding = holder.binding as LayoutMediaShareBinding
                dataBinding.videoView.player?.release()
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
            item as Message
            when (item.itemType) {
                InstagramConstants.MessageType.TEXT.type -> {
                    val dataBinding = holder.binding as LayoutMessageBinding
                    dataBinding.txtMessage.text = item.text
                    dataBinding.txtTime.text =
                        viewModel.getTimeFromTimeStamps(item.timestamp / 1000)
                    if (item.userId == user.pk) {
                        dataBinding.layoutParent.gravity = Gravity.RIGHT
                        dataBinding.layoutMessage.background =
                            this@DirectActivity.getDrawable(R.drawable.bg_message)
                    } else {
                        dataBinding.layoutMessage.background =
                            this@DirectActivity.getDrawable(R.drawable.bg_message_2)
                        dataBinding.layoutParent.gravity = Gravity.LEFT
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
                                .into(dataBinding.imgStory)
                        }
                        dataBinding.txtMessage.text = item.reelShare.text
                        dataBinding.txtTime.text =
                            viewModel.getTimeFromTimeStamps(item.timestamp / 1000)
                        if (item.userId == user.pk) {
                            dataBinding.layoutMessage.background =
                                this@DirectActivity.getDrawable(R.drawable.bg_message)
                            if (item.userId != item.reelShare.reelOwnerId) {
                                dataBinding.txtReelTypeInfo.text =
                                    getString(R.string.reply_to_their_story)
                            }else{
                                dataBinding.txtReelTypeInfo.text =
                                    getString(R.string.reply_to_their_story)
                            }
                            dataBinding.txtMessage.gravity = Gravity.RIGHT
                            dataBinding.layoutParent.gravity = Gravity.RIGHT
                            dataBinding.layoutParent.layoutDirection = View.LAYOUT_DIRECTION_RTL
                        } else {
                            dataBinding.layoutMessage.background =
                                this@DirectActivity.getDrawable(R.drawable.bg_message_2)
                            if (item.userId != item.reelShare.reelOwnerId) {
                                dataBinding.txtReelTypeInfo.text =
                                    getString(R.string.reply_to_your_story)
                            }else{
                                dataBinding.txtReelTypeInfo.text =
                                    getString(R.string.you_viewed_their_story)
                            }
                            dataBinding.txtMessage.gravity = Gravity.LEFT
                            dataBinding.layoutParent.gravity = Gravity.LEFT
                            dataBinding.layoutParent.layoutDirection = View.LAYOUT_DIRECTION_LTR
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
                        dataBinding.txtTime.text =
                            viewModel.getTimeFromTimeStamps(item.timestamp / 1000)
                        if (item.reelShare.media.imageVersions2 != null) {
                            val images = item.reelShare.media.imageVersions2.candidates
                            val user = item.reelShare.media.user
                            Picasso.get().load(images[1].url).into(dataBinding.imgStory)
                            Picasso.get().load(user.profilePicUrl).into(dataBinding.imgProfile)
                            dataBinding.txtUsername.text = user.username
                        } else {
                            visible(dataBinding.txtNoDataAvailable)
                            gone(dataBinding.imgStory, dataBinding.imgProfile)
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
                                .into(dataBinding.imgStory)
                        }
                        dataBinding.txtMessage.text = item.reelShare.text
                        if (item.userId == user.pk) {
                            dataBinding.layoutParent.gravity = Gravity.RIGHT
                        } else {
                            dataBinding.layoutParent.gravity = Gravity.LEFT
                        }
                    }

                }
                InstagramConstants.MessageType.STORY_SHARE.type -> {
                    if (item.storyShare.media != null) {
                        val dataBinding = holder.binding as LayoutReelShareBinding
                        val images = item.storyShare.media.imageVersions2.candidates
                        val user = item.storyShare.media.user
                        Picasso.get().load(images[1].url).into(dataBinding.imgStory)
                        Picasso.get().load(user.profilePicUrl).into(dataBinding.imgProfile)
                        dataBinding.txtUsername.text = user.username
                        dataBinding.txtReelStatus.text = String.format(
                            getString(R.string.send_story_from), user.username
                        )
                    } else {
                        val dataBinding = holder.binding as LayoutStoryShareNotLinkedBinding
                        dataBinding.txtTitle.text = item.storyShare.title
                        dataBinding.txtMessage.text = item.storyShare.message
                        dataBinding.txtTime.text =
                            viewModel.getTimeFromTimeStamps(item.timestamp / 1000)
                        if (item.userId == user.pk) {
                            dataBinding.layoutParent.gravity = Gravity.RIGHT
                        } else {
                            dataBinding.layoutParent.gravity = Gravity.LEFT
                        }
                    }
                }
                InstagramConstants.MessageType.VOICE_MEDIA.type -> {
                    val dataBinding = holder.binding as LayoutVoiceMediaBinding
                    val audioSrc = item.voiceMediaData.voiceMedia.audio.audioSrc
                    val id = item.voiceMediaData.voiceMedia.id
                    val player = SimpleExoPlayer.Builder(this@DirectActivity).build()
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(audioSrc))
                    player.prepare(mediaSource)
                    dataBinding.videoView.player = player
                    dataBinding.txtTime.text =
                        viewModel.getTimeFromTimeStamps(item.timestamp / 1000)
                    if (item.userId == user.pk) {
                        dataBinding.layoutParent.gravity = Gravity.RIGHT
                    } else {
                        dataBinding.layoutParent.gravity = Gravity.LEFT
                    }
                }
                InstagramConstants.MessageType.VIDEO_CALL_EVENT.type -> {
                    val dataBinding = holder.binding as LayoutEventBinding
                    dataBinding.txtEventDes.text = item.videoCallEvent.description
                }

                InstagramConstants.MessageType.MEDIA.type -> {
                    val dataBinding = holder.binding as LayoutMediaBinding
                    val images = item.media.imageVersions2.candidates
                    Picasso.get().load(images[0].url).into(dataBinding.imgMedia)

                    if (item.userId == user.pk) {
                        dataBinding.layoutParent.gravity = Gravity.RIGHT
                    } else {
                        dataBinding.layoutParent.gravity = Gravity.LEFT
                    }

                }
                InstagramConstants.MessageType.RAVEN_MEDIA.type -> {

                }
                InstagramConstants.MessageType.LIKE.type -> {
                    val dataBinding = holder.binding as LayoutLikeBinding
                    dataBinding.txtMessage.text = item.like
                }

                InstagramConstants.MessageType.MEDIA_SHARE.type -> {
                    val dataBinding = holder.binding as LayoutMediaShareBinding
                    val videoSrc = item.mediaShare.videoVersions[0].url
                    val media = item.mediaShare
                    val user = media.user
                    val id = item.mediaShare.id
                    val player = SimpleExoPlayer.Builder(this@DirectActivity).build()
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(videoSrc))
                    player.prepare(mediaSource)
                    player.playWhenReady = true
                    player.volume = 0f
                    dataBinding.videoView.setOnClickListener {
                        dataBinding.imgVolume.visibility = View.VISIBLE
                        if (player!!.volume == 0f) {
                            dataBinding.imgVolume.setImageResource(R.drawable.ic_volume_high)
                            player.volume = 100f
                        } else {
                            dataBinding.imgVolume.setImageResource(R.drawable.ic_volume_off)
                            player.volume = 0f
                        }
                        hideAfterSeconds(dataBinding.imgVolume, 1500)
                    }
                    dataBinding.btnReplay.setOnClickListener {
                        fullRotation(dataBinding.btnReplay)
                        player!!.seekTo(0)
                        player.playWhenReady = true
                    }
                    dataBinding.layoutVideoView.layoutParams.apply {
                        width =
                            this@DirectActivity.resources.dpToPx(media.videoVersions[1].width.toFloat())
                        height =
                            this@DirectActivity.resources.dpToPx(media.videoVersions[1].height.toFloat())
                    }
                    dataBinding.videoView.player = player
                    Picasso.get().load(user.profilePicUrl).into(dataBinding.imgProfile)
                    dataBinding.txtUsername.text = user.username
                    dataBinding.txtCaption.text = media.caption.text
                    dataBinding.txtTime.text =
                        viewModel.getTimeFromTimeStamps(item.timestamp / 1000)
                    if (item.userId == user.pk) {
                        dataBinding.layoutParent.gravity = Gravity.RIGHT
                        dataBinding.layoutMessage.background =
                            this@DirectActivity.getDrawable(R.drawable.bg_message)
                    } else {
                        dataBinding.layoutMessage.background =
                            this@DirectActivity.getDrawable(R.drawable.bg_message_2)
                        dataBinding.layoutParent.gravity = Gravity.LEFT
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
                    Picasso.get().load(url).placeholder(R.drawable.load).into(dataBinding.imgAnim)
                }
                else -> {
                    val dataBinding = holder.binding as LayoutMessageBinding
                    dataBinding.txtMessage.text = "Sorry ${item.itemType} not support"
                    dataBinding.txtTime.text =
                        viewModel.getTimeFromTimeStamps(item.timestamp / 1000)
                    if (item.userId == user.pk) {
                        dataBinding.layoutParent.gravity = Gravity.RIGHT
                        dataBinding.layoutMessage.background =
                            this@DirectActivity.getDrawable(R.drawable.bg_message)
                    } else {
                        dataBinding.layoutMessage.background =
                            this@DirectActivity.getDrawable(R.drawable.bg_message_2)
                        dataBinding.layoutParent.gravity = Gravity.LEFT
                    }
                }
            }
            return item
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
                else -> {
                    return R.layout.layout_message
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }


}