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
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
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
import com.idirect.app.databinding.FragmentStoryItemBinding
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

class FragmentStoryItem(var userId: Long,var mStoryActionListener: StoryActionListener,var playItemAfterLoad:Boolean=false,var isTouchEnable:Boolean = true) : BaseFragment<FragmentStoryItemBinding, StoryItemViewModel>(), ForwardListener {

    private var moduleSource: String = "feed_timeline"

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
    var currentPosition: Int = -3
    private var actionDownTimestamp: Long = 0
    private var isCancelValueAnimator: Boolean = false
    private var isPauseAnyThing = false
    private var isForwardWindowShow: Boolean = false
    private lateinit var dataSource: DataSource.Factory
    private lateinit var mGlideRequestListener: RequestListener<Bitmap>
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var sharedViewModel: ShareViewModel
    private var currentTray: Tray?=null
    val valueAnimator = ValueAnimator.ofInt(0, 100).apply {
        duration = 5000
    }

    override fun getViewModelClass(): Class<StoryItemViewModel> {
        return StoryItemViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_story_item
    }

    override fun getNameTag(): String {
        return "story"
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.decorView.systemUiVisibility = 0

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

        binding.imgPhoto.layoutParams.apply {
            width = displayWidth
            height = displayHeight
        }

        viewModel.getStoryData(userId)
        viewModel.storyMediaLiveData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                currentTray = it.data!!
                items = it.data!!.items
                initLayout(it.data!!)
                if(playItemAfterLoad){
                    showNextItem()
                }
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
            if(event.action == MotionEvent.ACTION_CANCEL){
                binding.layoutItems.fadeIn(100)
                valueAnimator.resume()
                mPlayManager.resumePlay()
                actionDownTimestamp = 0
            }
            if (event.action == MotionEvent.ACTION_UP) {
                isPauseAnyThing = false
                if (System.currentTimeMillis() - actionDownTimestamp < 200) {
                    Log.i(InstagramConstants.DEBUG_TAG,"175")
                    binding.layoutItems.fadeIn(0)
                    isCancelValueAnimator = true
                    valueAnimator.cancel()
                    if (event.rawX < centerScreenPosition) {
                        // left
                        showPreviousItem()
                    } else {
                        //right
                        showNextItem()
                    }
                } else {
                    Log.i(InstagramConstants.DEBUG_TAG,"185")
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
            if(currentPosition != -1){
                val value = it.animatedValue as Int
                progressBars[currentPosition].progress = value
            }
        }

        valueAnimator.doOnStart {
            binding.recyclerviewEmoji.visibility = View.INVISIBLE
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
                        if (!isPauseAnyThing){
                            valueAnimator.resume()
                        }
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
                val currentItem = currentTray!!.items[currentPosition]
                val bundle = ForwardBundle(
                    mediaId = currentItem.id,
                    mediaType = currentItem.mediaType,
                    isStoryShare = true,
                    reelId = currentItem.pk
                )
                (requireActivity() as MainActivity).showShareWindow(bundle, this)
                isForwardWindowShow = true
                onPause()
            }
        }
        binding.layoutHeader.setOnClickListener {
            mStoryActionListener.onProfileClick(it,userId,binding.txtUsername.text.toString())
        }
    }

    private fun initEmoji(): MutableList<String> {
        val emojis = ArrayList<String>().toMutableList()
        emojis.add("\uD83D\uDE02")
        emojis.add("\uD83D\uDE2E")
        emojis.add("\uD83D\uDE0D")
        emojis.add("\uD83D\uDE22")
        emojis.add("\uD83D\uDC4F")
        emojis.add("\uD83D\uDD25")
        emojis.add("\uD83C\uDF89")
        emojis.add("\uD83D\uDCAF")
        return emojis
    }

    private fun initLayout(tray: Tray) {
        progressBars.clear()
        binding.layoutSeekbars.removeAllViews()
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

    fun showNextItem() {
        if(currentTray == null){
            return
        }
        currentPosition += 1
        if(currentPosition < 0){
            currentPosition = 0
        }
        if (currentPosition >= progressBars.size) {
            currentPosition = items.size - 1
            if(moduleSource == "feed_timeline"){
                mStoryActionListener.loadNextPage()
            }else{
                requireActivity().onBackPressed()
            }
            return
        }
        binding.layoutItems.fadeIn(0)
        startProgressAnimator()
        loadItem(items[currentPosition])
        for (index in 0 until currentPosition) {
            progressBars[index].progress = 100
        }
    }

    private fun loadItem(item: Story) {
        if (item.mediaType == InstagramConstants.MediaType.IMAGE.type) {
            mPlayManager.stopPlay()
            valueAnimator.pause()
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
        binding.layoutItems.fadeIn(0)
        startProgressAnimator()
        loadItem(items[currentPosition])

        for (index in 0 until currentPosition) {
            progressBars[index].progress = 100
        }
        for (index in currentPosition until progressBars.size) {
            progressBars[index].progress = 0
        }
    }

    private fun startProgressAnimator() {
        valueAnimator.start()
    }

    override fun onKeyboardOpen() {
        super.onKeyboardOpen()
        if(isForwardWindowShow){
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
        if(isForwardWindowShow){
            return
        }
        isPauseAnyThing = false
        valueAnimator.resume()
        mPlayManager.resumePlay()
        binding.edtMessage.setText("")
        binding.recyclerviewEmoji.visibility = View.INVISIBLE
        binding.layoutMessage.setBackgroundColor(Color.parseColor("#00000000"))
    }

    override fun onPause() {
        super.onPause()
        mPlayManager.pausePlay()
        valueAnimator.pause()
    }

    override fun onResume() {
        super.onResume()
        if(isForwardWindowShow){
            return
        }
        mPlayManager.resumePlay()
        valueAnimator.resume()
    }

    inner class EmojiAdapter constructor(var items: MutableList<String>) : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val emoji = items[position]
            val dataBinding = holder.binding as LayoutEmojiBinding
            dataBinding.txtEmoji.text = emoji
            dataBinding.root.setOnClickListener {
                val currentStory = currentTray!!.items[currentPosition]
                viewModel.sendStoryReaction(
                    sharedViewModel.getThreadIdByUserId(currentTray!!.user.pk),
                    currentStory.id,
                    emoji,
                    currentStory.pk
                )
                requireActivity().hideKeyboard()
            }
            return emoji
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.layout_emoji
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }

    override fun onDismiss() {
        isForwardWindowShow = false
        onResume()
    }

    override fun onStop() {
        super.onStop()
    }
    override fun onDestroy() {
        super.onDestroy()
        mPlayManager.releasePlay()
    }
}