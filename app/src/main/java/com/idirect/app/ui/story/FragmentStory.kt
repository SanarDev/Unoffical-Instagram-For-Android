package com.idirect.app.ui.story

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentStoryBinding
import com.idirect.app.databinding.LayoutEmojiBinding
import com.idirect.app.datasource.model.EmojiModel
import com.idirect.app.datasource.model.Story
import com.idirect.app.datasource.model.Tray
import com.idirect.app.extensions.fadeIn
import com.idirect.app.extensions.fadeOut
import com.idirect.app.extensions.setProgressColor
import com.idirect.app.extentions.color
import com.idirect.app.extentions.dpToPx
import com.idirect.app.extentions.hideKeyboard
import com.idirect.app.manager.PlayManager
import com.idirect.app.ui.forward.ForwardBundle
import com.idirect.app.ui.forward.ForwardListener
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.ui.main.ShareViewModel
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.vanniktech.emoji.EmojiPopup
import javax.inject.Inject

class FragmentStory : BaseFragment<FragmentStoryBinding, StoryViewModel>(), ForwardListener {

    @Inject
    lateinit var mGlide: RequestManager

    @Inject
    lateinit var mHandler: Handler

    @Inject
    lateinit var mPlayManager: PlayManager

    companion object {
        const val SEND_MESSAGE_ROTATE = 27f
        const val FORWARD_ROTATE = 0f
    }

    private val displayWidth = DisplayUtils.getScreenWidth()
    private val displayHeight = DisplayUtils.getScreenHeight()
    private val centerScreenPosition = displayWidth / 2

    private lateinit var items: List<Story>
    private val progressBars = ArrayList<ProgressBar>().toMutableList()
    private var currentPosition: Int = -1
    private var actionDownTimestamp: Long = 0
    private var isCancelValueAnimator: Boolean = false
    private var isPauseAnyThing = false
    private var isForwardWindowHide: Boolean = false
    private lateinit var dataSource: DataSource.Factory
    private lateinit var mGlideRequestListener: RequestListener<Bitmap>
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var sharedViewModel: ShareViewModel
    private lateinit var currentTray: Tray
    val valueAnimator = ValueAnimator.ofInt(0, 100).apply {
        duration = 5000
    }

    override fun getViewModelClass(): Class<StoryViewModel> {
        return StoryViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_story
    }

    override fun getNameTag(): String {
        return "story"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.decorView.systemUiVisibility = 0

        val userId = requireArguments().getString("user_id")!!.toLong()
        viewModel.getStoryData(userId)

        dataSource = DefaultHttpDataSourceFactory(Util.getUserAgent(requireContext(), "Instagram"))
        sharedViewModel = ViewModelProvider(requireActivity()).get(ShareViewModel::class.java)
        emojiPopup =
            EmojiPopup.Builder.fromRootView(binding.layoutParent)
                .setOnEmojiPopupDismissListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_emoji)
                }.setOnEmojiPopupShownListener {
                    binding.btnEmoji.setImageResource(R.drawable.ic_keyboard_outline)
                }.build(binding.edtMessage);

        binding.recyclerviewEmoji.adapter = EmojiAdapter(initEmoji())
        viewModel.storyMediaLiveData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                currentTray = it.data!!
                items = it.data!!.items
                initLayout(it.data!!)
            }
        })
        viewModel.storyReactionResult.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                val toast: Toast = Toast.makeText(
                    requireContext(),
                    getString(R.string.reaction_send),
                    Toast.LENGTH_LONG
                )
                val view = toast.view
                view.setBackgroundResource(R.drawable.bg_toast)
                val text: TextView = view.findViewById(android.R.id.message)
                text.setTextColor(Color.WHITE)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        })

        binding.layoutMessage.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                isPauseAnyThing = false
                if (System.currentTimeMillis() - actionDownTimestamp < 200) {
                    isCancelValueAnimator = true
                    valueAnimator.cancel()
                    if (event.rawX < centerScreenPosition) {
                        // left
                        showPreviousItem()
                    } else {
                        //right
                        binding.layoutItems.fadeIn(0)
                        showNextItem()
                    }
                } else {
                    binding.layoutItems.fadeIn(100)
                    valueAnimator.resume()
                    mPlayManager.resumePlay()
                }
                actionDownTimestamp = 0
            }
            if (event.action == MotionEvent.ACTION_DOWN) {
                isPauseAnyThing = true
                valueAnimator.pause()
                mPlayManager.pausePlay()
                actionDownTimestamp = System.currentTimeMillis()
                mHandler.postDelayed({
                    if (actionDownTimestamp != 0.toLong()) {
                        binding.layoutItems.fadeOut(100)
                    }
                }, 500)
            }
            return@setOnTouchListener true
        }

        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Int
            progressBars[currentPosition].progress = value
        }

        valueAnimator.doOnEnd {
            if (!isCancelValueAnimator) {
                showNextItem()
            } else {
                isCancelValueAnimator = false
            }
        }
        binding.videoView.layoutParams.apply {
            width = displayWidth
            height = displayHeight
        }
        mPlayManager.player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        valueAnimator.pause()
                    }
                    Player.STATE_READY -> {
                        if (!isPauseAnyThing)
                            valueAnimator.resume()
                    }
                }
            }
        })
        mGlideRequestListener = object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {

                return true
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: com.bumptech.glide.load.DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                valueAnimator.resume()
                binding.imgPhoto.setImageBitmap(resource)
                return true
            }
        }
        binding.btnEmoji.setOnClickListener {
            if (emojiPopup.isShowing) {
                emojiPopup.dismiss()
            } else {
                emojiPopup.toggle()
            }
        }
        binding.edtMessage.setOnClickListener {
            emojiPopup.dismiss()
        }
        binding.edtMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isEmpty()) {
                    binding.btnSend.rotation = FORWARD_ROTATE
                    binding.recyclerviewEmoji.visibility = View.VISIBLE
                } else {
                    binding.btnSend.rotation = SEND_MESSAGE_ROTATE
                    binding.recyclerviewEmoji.visibility = View.INVISIBLE
                }
            }
        })

        binding.btnSend.setOnClickListener {
            if (it.rotation == SEND_MESSAGE_ROTATE) {

            } else {
                val currentItem = currentTray.items[currentPosition]
                val bundle = ForwardBundle(
                    mediaId = currentItem.id,
                    mediaType = currentItem.mediaType,
                    isStoryShare = true,
                    reelId = currentItem.pk
                )
                (requireActivity() as MainActivity).showShareWindow(bundle, this)
                isForwardWindowHide = false
                onPause()
            }
        }
    }

    private fun initEmoji(): MutableList<EmojiModel> {
        val emojis = ArrayList<EmojiModel>().toMutableList()
        emojis.add(EmojiModel("\uD83D\uDE02", "1"))
        emojis.add(EmojiModel("\uD83D\uDE2E", "2"))
        emojis.add(EmojiModel("\uD83D\uDE0D", "3"))
        emojis.add(EmojiModel("\uD83D\uDE22", "4"))
        emojis.add(EmojiModel("\uD83D\uDC4F", "5"))
        emojis.add(EmojiModel("\uD83D\uDD25", "6"))
        emojis.add(EmojiModel("\uD83C\uDF89", "7"))
        emojis.add(EmojiModel("\uD83D\uDCAF", "8"))
        return emojis
    }

    private fun initLayout(tray: Tray) {
        binding.txtUsername.text = tray.user.username
        mGlide.load(tray.user.profilePicUrl).into(binding.imgProfile)
        for (item in tray.items) {
            if (item.mediaType == InstagramConstants.MediaType.IMAGE.type) {
                mGlide.load(item.imageVersions2.candidates[0].url)
                    .override(displayWidth, displayHeight).preload()
            }
            val progressBar = createProgressBar()
            progressBars.add(progressBar)
            binding.layoutSeekbars.addView(progressBar)
        }
        showNextItem()
    }

    private fun createProgressBar(): ProgressBar {
        val progressBar =
            ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        resources.dpToPx(1.5f)
                    ).apply {
                        this.weight = 1f
                        this.marginStart = resources.dpToPx(3f)
                        this.marginEnd = resources.dpToPx(3f)
                    }
                setProgressColor(context.color(R.color.white))
            }
        return progressBar
    }

    private fun showNextItem() {
        currentPosition += 1
        if (currentPosition >= progressBars.size) {
            currentPosition = progressBars.size - 1
            return
        }
        loadItem(items[currentPosition])
        for (index in 0 until currentPosition) {
            progressBars[index].progress = 100
        }
        startProgressAnimator()
    }

    private fun loadItem(item: Story) {
        if (item.mediaType == InstagramConstants.MediaType.IMAGE.type) {
            mPlayManager.stopPlay()
            binding.videoView.visibility = View.INVISIBLE
            binding.imgPhoto.visibility = View.VISIBLE
            mGlide.asBitmap().load(item.imageVersions2.candidates[0].url)
                .addListener(mGlideRequestListener).preload()
            valueAnimator.duration = 5000
        }
        if (item.mediaType == InstagramConstants.MediaType.VIDEO.type) {
            binding.videoView.visibility = View.VISIBLE
            binding.imgPhoto.visibility = View.INVISIBLE
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSource)
                    .createMediaSource(Uri.parse(item.videoVersions[0].url))
            mPlayManager.startPlay(mediaSource, item.id)
            binding.videoView.player = mPlayManager.player
            valueAnimator.duration = (item.videoDuration * 1000).toLong()
        }
    }

    private fun showPreviousItem() {
        currentPosition -= 1
        if (currentPosition >= progressBars.size || currentPosition < 0) {
            currentPosition = 0
            return
        }
        loadItem(items[currentPosition])

        for (index in 0 until currentPosition) {
            progressBars[index].progress = 100
        }
        for (index in currentPosition until progressBars.size) {
            progressBars[index].progress = 0
        }
        startProgressAnimator()
    }

    private fun startProgressAnimator() {
        valueAnimator.start()
        valueAnimator.pause()
    }

    override fun onKeyboardOpen() {
        super.onKeyboardOpen()
        if(!isForwardWindowHide){
            return
        }
        isPauseAnyThing = true
        valueAnimator.pause()
        mPlayManager.pausePlay()
        binding.recyclerviewEmoji.visibility = View.VISIBLE
        binding.layoutMessage.setBackgroundColor(Color.parseColor("#aa000000"))
    }

    override fun onKeyboardHide() {
        super.onKeyboardHide()
        if(!isForwardWindowHide){
            return
        }
        isPauseAnyThing = false
        valueAnimator.resume()
        mPlayManager.resumePlay()
        binding.edtMessage.setText("")
        binding.recyclerviewEmoji.visibility = View.GONE
        binding.layoutMessage.setBackgroundColor(Color.parseColor("#00000000"))
    }

    override fun onPause() {
        super.onPause()
        mPlayManager.pausePlay()
        valueAnimator.pause()
    }

    override fun onResume() {
        super.onResume()
        if(!isForwardWindowHide){
            return
        }
        mPlayManager.resumePlay()
        valueAnimator.resume()
    }

    inner class EmojiAdapter constructor(var items: MutableList<EmojiModel>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = items[position]
            val dataBinding = holder.binding as LayoutEmojiBinding
            dataBinding.root.setOnClickListener {
                val currentStory = currentTray.items[currentPosition]
                viewModel.sendStoryReaction(
                    sharedViewModel.getThreadIdByUserId(currentTray.user.pk),
                    currentStory.id,
                    item.emoji,
                    currentStory.pk
                )
                requireActivity().hideKeyboard()
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.layout_emoji
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }

    override fun onDismiss() {
        isForwardWindowHide = true
        onResume()
    }
}