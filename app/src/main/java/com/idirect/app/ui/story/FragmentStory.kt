package com.idirect.app.ui.story

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.setMargins
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.idirect.app.R
import com.idirect.app.constants.AppConstants
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseAdapter
import com.idirect.app.core.BaseFragment
import com.idirect.app.customview.textdrawable.TextDrawable
import com.idirect.app.customview.toast.CustomToast
import com.idirect.app.databinding.FragmentStoryBinding
import com.idirect.app.databinding.FragmentStoryItemBinding
import com.idirect.app.databinding.LayoutEmojiBinding
import com.idirect.app.databinding.LayoutStoryItemPopupBinding
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
import com.idirect.app.ui.story.question.ActionListener
import com.idirect.app.ui.story.question.FragmentQuestion
import com.idirect.app.utils.BitmapUtils
import com.idirect.app.utils.DisplayUtils
import com.idirect.app.utils.Resource
import com.sanardev.instagramapijava.model.story.*
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener
import javax.inject.Inject

class FragmentStory(
    var userId: Long,
    var mStoryActionListener: StoryActionListener? = null,
    val mPlayManager: PlayManager,
    val videoView: PlayerView,
    var playItemAfterLoad: Boolean = false,
    var isTouchEnable: Boolean = true
) : BaseFragment<FragmentStoryBinding, StoryViewModel>(), ForwardListener,
    OnEmojiPopupDismissListener, OnEmojiPopupShownListener {

    private lateinit var storyAdapter: ViewPagerAdapter
    private var moduleSource: String = "feed_timeline"

    @Inject
    lateinit var mHandler: Handler

    companion object {
        const val SEND_MESSAGE_ROTATE = 27f
        const val FORWARD_ROTATE = 0f
        const val MAX_VALUE_PROGRESS = 100
        const val PROGRESS_DURATION_MILI_SECONDS = 4000L
    }

    private val displayWidth = DisplayUtils.getScreenWidth()
    private val displayHeight = DisplayUtils.getScreenHeight()
    private val centerScreenPosition = displayWidth / 2

    private val progressBars = ArrayList<ProgressBar>().toMutableList()
    var currentPosition: Int = -3
    private var actionDownTimestamp: Long = 0
    private var isCancelValueAnimator: Boolean = false
    private var isPopupShow: Boolean = false
    private var isPauseAnyThing = false
    private var isForwardWindowShow: Boolean = false
    private lateinit var dataSource: DataSource.Factory
    private var emojiPopup: EmojiPopup? = null
    private lateinit var sharedViewModel: ShareViewModel
    private var currentTray: Tray? = null
    private var valueAnimatorUpdateListener =
        // change
        ValueAnimator.AnimatorUpdateListener { animation ->
            if (currentPosition != -1) {
                val value = animation!!.animatedValue as Int
                progressBars[currentPosition].progress = value
            }
        }

    var _valueAnimator: ValueAnimator? = null
    val valueAnimator: ValueAnimator get() = _valueAnimator!!

    private var _mGlide: RequestManager? = null
    private val mGlide: RequestManager get() = _mGlide!!

    override fun getViewModelClass(): Class<StoryViewModel> {
        return StoryViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_story
    }

    override fun getNameTag(): String {
        return "story"
    }

    override fun onDestroyView() {
        valueAnimator.removeUpdateListener(valueAnimatorUpdateListener)
        emojiPopup?.releaseMemory()
        progressBars.clear()
        _mGlide = null
        super.onDestroyView()
    }

    //    fun releaseMemory() {
//        if (Build.VERSION.SDK_INT < 16) {
//            binding.layoutParent.getViewTreeObserver().removeGlobalOnLayoutListener(emojiPopup.onGlobalLayoutListener)
//        } else {
//            binding.layoutParent.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _mGlide = Glide.with(this@FragmentStory)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataSource = DefaultHttpDataSourceFactory(Util.getUserAgent(requireContext(), "Instagram"))
        sharedViewModel = ViewModelProvider(requireActivity()).get(ShareViewModel::class.java)

        _valueAnimator = ValueAnimator.ofInt(0, MAX_VALUE_PROGRESS).apply {
            duration = PROGRESS_DURATION_MILI_SECONDS
            interpolator = LinearInterpolator()
        }
        valueAnimator.addUpdateListener(valueAnimatorUpdateListener)

        valueAnimator.doOnStart {
            binding.recyclerviewEmoji.visibility = View.INVISIBLE
        }
        valueAnimator.doOnEnd {
            //change
            if (!isCancelValueAnimator) {
                showNextItem()
            } else {
                isCancelValueAnimator = false
            }
        }

        emojiPopup =
            EmojiPopup.Builder
                .fromRootView(binding.root)
                .setOnEmojiPopupDismissListener(this@FragmentStory)
                .setOnEmojiPopupShownListener(this@FragmentStory)
                .build(binding.edtMessage);

        binding.recyclerviewEmoji.adapter = EmojiAdapter(initEmoji())
        storyAdapter = ViewPagerAdapter()
        binding.viewPager.adapter = storyAdapter
        binding.viewPager.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)

        viewModel.getStoryData(userId)
        viewModel.storyMediaLiveData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                currentTray = it.data!!
                initLayout(it.data!!)
                storyAdapter.notifyDataSetChanged()
                if (playItemAfterLoad) {
                    showCurrentItem()
                }
            }
        })
        viewModel.storyReactionResult.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                CustomToast.show(
                    requireContext(),
                    getString(R.string.reaction_send),
                    Toast.LENGTH_LONG
                )
            }
        })

        binding.viewPager.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_CANCEL) {
                binding.layoutItems.fadeIn(100)
                binding.viewPager.isTouchMovable = false
                resumeTimer()
                actionDownTimestamp = 0
            }
            if (event.action == MotionEvent.ACTION_UP) {
                isPauseAnyThing = false
                if (System.currentTimeMillis() - actionDownTimestamp < 200) {
                    Log.i(InstagramConstants.DEBUG_TAG, "175")
                    binding.layoutItems.fadeIn(0)
                    binding.viewPager.isTouchMovable = false
                    if(isPopupShow){
                        resumeTimer()
                        isPopupShow = false
                    }else{
                        cancelTimer()
                        if (event.rawX < centerScreenPosition) {
                            // left
                            showPreviousItem(true)
                        } else {
                            //right
                            showNextItem()
                        }
                    }
                } else {
                    binding.layoutItems.fadeIn(100)
                    binding.viewPager.isTouchMovable = false
                    resumeTimer()
                }
                actionDownTimestamp = 0
            }
            if (event.action == MotionEvent.ACTION_DOWN) {
                isPauseAnyThing = true
                pauseTimer()
                actionDownTimestamp = System.currentTimeMillis()
                mHandler.postDelayed({
                    if (actionDownTimestamp != 0.toLong()) {
                        binding.layoutItems.fadeOut(100)
                        binding.viewPager.isTouchMovable = true
                    }
                }, 500)
            }
            return@setOnTouchListener true
        }
//
//        binding.videoView.layoutParams.apply {
//            height = displayHeight
//        }

        binding.btnEmoji.setOnClickListener {
            if (emojiPopup!!.isShowing) {
                emojiPopup!!.dismiss()
            } else {
                emojiPopup!!.toggle()
            }
        }
        binding.edtMessage.setOnClickListener {
            emojiPopup!!.dismiss()
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
                val currentStory = currentTray!!.items[currentPosition]
                viewModel.replyStory(
                    sharedViewModel.getThreadIdByUserId(currentTray!!.user.pk),
                    currentStory.id,
                    currentStory.mediaType,
                    binding.edtMessage.text.toString(),
                    currentStory.pk
                )
                binding.edtMessage.setText("")
                emojiPopup?.dismiss()
                requireActivity().hideKeyboard()
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
            mStoryActionListener?.onProfileClick(it, userId, binding.txtUsername.text.toString())
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
                        resources.dpToPx(3f)
                    ).apply {
                        this.weight = 1f
                        this.marginStart = resources.dpToPx(2f)
                        this.marginEnd = resources.dpToPx(2f)
                    }
                setProgressColor(context.color(R.color.white))
            }
        return progressBar
    }

    fun showCurrentItem(){
        if(currentPosition == 0){
            currentPosition = -1;
        }
        showNextItem()
    }
    fun showNextItem() {
        if (currentTray == null) {
            return
        }
        currentPosition += 1
        if (currentPosition < 0) {
            currentPosition = 0
        }
        if (currentPosition >= progressBars.size) {
            currentPosition = currentTray!!.items.size - 1
            if (moduleSource == "feed_timeline") {
                mStoryActionListener?.loadNextPage()
            } else {
                requireActivity().onBackPressed()
            }
            return
        }
        val item = currentTray!!.items!![currentPosition]
        Log.i(InstagramConstants.DEBUG_TAG,currentTray!!.user!!.username)
        Log.i(InstagramConstants.DEBUG_TAG,item.id)
        viewModel.markStoryAsSeen(item.id, item.takenAt)
        binding.layoutItems.fadeIn(0)
        startProgressAnimator()
        showItemAtPosition(currentPosition)
        for (index in 0 until currentPosition) {
            progressBars[index].progress = 100
        }
    }

    private fun cancelTimer() {
        isCancelValueAnimator = true
        valueAnimator.cancel()
    }

    fun showPreviousItem(isTouchedByUser:Boolean = false) {
        if((valueAnimator.animatedValue as Int) > (MAX_VALUE_PROGRESS / 2)){
            resetTimer()
            return
        }
        currentPosition -= 1
        if (currentPosition >= progressBars.size || currentPosition < 0) {
            currentPosition = 0
            if(isTouchedByUser){
                mStoryActionListener?.loadPreviousPage()
            }
            return
        }
        binding.layoutItems.fadeIn(0)
        startProgressAnimator()

        // for play video need
        showItemAtPosition(currentPosition)

        for (index in 0 until currentPosition) {
            progressBars[index].progress = 100
        }
        for (index in currentPosition until progressBars.size) {
            progressBars[index].progress = 0
        }
    }

    private fun showItemAtPosition(currentPosition: Int) {
        currentTray?.let {
            if(it.items[currentPosition].mediaType == InstagramConstants.MediaType.VIDEO.type){
                storyAdapter.notifyItemChanged(currentPosition)
            }
        }
        binding.viewPager.scrollToPosition(currentPosition)
    }

    fun stateReady(){
        if (!isPauseAnyThing) {
            resumeTimer()
        }
    }
    fun pauseTimer(){
        valueAnimator.pause()
        mPlayManager.pausePlay()
    }
    fun resumeTimer(){
        valueAnimator.resume()
        mPlayManager.resumePlay()
    }
    private fun resetTimer(){
        valueAnimator.cancel()
        mPlayManager.replay()
        startProgressAnimator()
    }

    private fun startProgressAnimator() {
        mPlayManager.stopPlay()
        valueAnimator.start()
        valueAnimator.resume()
    }

    inner class ViewPagerAdapter : BaseAdapter() {
        override fun getObjForPosition(holder: BaseViewHolder, position: Int): Any {
            val item = currentTray!!.items[position]
            val dataBinding = holder.binding as FragmentStoryItemBinding
            dataBinding.root.layoutParams.apply {
                width = displayWidth
                height = displayHeight
            }
            dataBinding.layoutStoryItems.layoutParams.apply {
                width = displayWidth
                height = displayHeight
            }
            Log.i(InstagramConstants.DEBUG_TAG,"item type"+item.mediaType)
            if (item.mediaType == InstagramConstants.MediaType.IMAGE.type) {
                mPlayManager.stopPlay()
                dataBinding.imgPhoto.visibility = View.VISIBLE
                mGlide.asBitmap().load(item.imageVersions2.candidates[0].url).into(dataBinding.imgPhoto)
                valueAnimator.duration = 5000
            }
            if (item.mediaType == InstagramConstants.MediaType.VIDEO.type) {
                dataBinding.imgPhoto.visibility = View.INVISIBLE
                if(playItemAfterLoad){
                    val mediaSource: MediaSource =
                        ProgressiveMediaSource.Factory(dataSource)
                            .createMediaSource(Uri.parse(item.videoVersions[0].url))
                    videoView.parent?.let{
                        if(it is ViewGroup){
                            it.removeView(videoView)
                        }
                    }
                    dataBinding.layoutPlayer.addView(videoView)
                    mPlayManager.startPlay(mediaSource, item.id)
                }
                valueAnimator.duration = (item.videoDuration * 1000).toLong()
            }

            dataBinding.layoutStoryItems.removeAllViews()
            val storyItems = ArrayList<Any>().toMutableList()
            item.reelMentions?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }
            item.storyPolls?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }
            item.storyFeedMedia?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }
            item.storyHashtags?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }
            item.storyLocations?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }
            item.storyQuestions?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }
            item.storyQuizs?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }
            item.storyCountDowns?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }
            item.storySliders?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }
            item.storyAntiBullyStickers?.let {
                for (`object` in it) {
                    storyItems.add(`object`)
                }
            }

            for (item in storyItems) {
                when (item::class.java.simpleName) {
                    ReelMention::class.java.simpleName -> {
                        dataBinding.layoutStoryItems.addView(
                            createReelMention(
                                binding.viewPager,
                                item as ReelMention
                            )
                        )
                    }
                    StoryLocation::class.java.simpleName -> {
                        dataBinding.layoutStoryItems.addView(
                            createLocation(
                                binding.viewPager,
                                item as StoryLocation
                            )
                        )
                    }
                    StoryHashtag::class.java.simpleName -> {
                        dataBinding.layoutStoryItems.addView(
                            createHashtag(
                                binding.viewPager,
                                item as StoryHashtag
                            )
                        )
                    }
                    StoryPoll::class.java.simpleName -> {
                        dataBinding.layoutStoryItems.addView(
                            createPoll(
                                binding.viewPager,
                                item as StoryPoll
                            )
                        )
                    }
                    StoryQuestion::class.java.simpleName -> {
                        dataBinding.layoutStoryItems.addView(
                            createQuestion(
                                binding.viewPager,
                                item as StoryQuestion
                            )
                        )
                    }
                    StorySlider::class.java.simpleName -> {
                        dataBinding.layoutStoryItems.addView(
                            createSlider(
                                binding.viewPager,
                                item as StorySlider
                            )
                        )
                    }
                }
            }
            return item
        }

        override fun getLayoutIdForPosition(position: Int): Int {
            return R.layout.fragment_story_item
        }

        override fun getItemCount(): Int {
            return if(currentTray == null){
                 0
            }else{
                currentTray!!.items.size
            }
        }

    }

    override fun onKeyboardOpen() {
        super.onKeyboardOpen()
        if (isForwardWindowShow) {
            return
        }
        isPauseAnyThing = true
        pauseTimer()
        binding.recyclerviewEmoji.visibility = View.VISIBLE
        binding.layoutMessage.setBackgroundColor(Color.parseColor("#aa000000"))
    }

    override fun onKeyboardHide() {
        super.onKeyboardHide()
        if (isForwardWindowShow) {
            return
        }
        isPauseAnyThing = false
        resumeTimer()
        binding.edtMessage.setText("")
        binding.recyclerviewEmoji.visibility = View.INVISIBLE
        binding.layoutMessage.setBackgroundColor(Color.parseColor("#00000000"))
    }

    override fun onPause() {
        super.onPause()
        pauseTimer()
    }

    override fun onResume() {
        super.onResume()
        if (isForwardWindowShow) {
            return
        }
        resumeTimer()
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
    }

    override fun onEmojiPopupDismiss() {
        binding.btnEmoji.setImageResource(R.drawable.ic_emoji)
    }

    override fun onEmojiPopupShown() {
        binding.btnEmoji.setImageResource(R.drawable.ic_keyboard_outline)
    }

    fun createLocation(v: View, storyLocation: StoryLocation): FrameLayout {
        val viewGroup = createClickableLayout(
            v, storyLocation
        )
        viewGroup.setOnClickListener {
            pauseTimer()
            isPopupShow = true
            val popup = PopupWindow(requireContext())
            val layout: LayoutStoryItemPopupBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.layout_story_item_popup,
                null,
                false
            )
            layout.txtInfo.text = requireContext().getString(R.string.view_location)
            layout.imgProfile.visibility = View.GONE
            popup.contentView = layout.root
            popup.isOutsideTouchable = true
            popup.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            );
            popup.showAsDropDown(viewGroup)
        }
        return viewGroup
    }

    fun createHashtag(v: View, storyHashtag: StoryHashtag): FrameLayout {
        val viewGroup = createClickableLayout(v, storyHashtag)
        viewGroup.setOnClickListener {
            pauseTimer()
            isPopupShow = true
            val popup = PopupWindow(context)
            val layout: LayoutStoryItemPopupBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.layout_story_item_popup,
                null,
                false
            )
            layout.txtInfo.text = requireContext().getString(R.string.view_hashtag)
            layout.imgProfile.visibility = View.GONE
            popup.contentView = layout.root
            popup.isOutsideTouchable = true
            popup.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            );
            popup.showAsDropDown(viewGroup)
        }
        return viewGroup
    }

    fun createQuestion(v: View, storyQuestion: StoryQuestion): FrameLayout {
        val viewGroup = createClickableLayout(v, storyQuestion)
        viewGroup.setOnClickListener {
            pauseTimer()
            val fragmentQuestion = FragmentQuestion(storyQuestion.questionSticker,object:ActionListener{
                override fun onDismiss() {
                    resumeTimer()
                }

                override fun onSendResponse(response: String) {
                    viewModel.sendStoryQuestionResponse(currentTray!!.items[currentPosition].id,storyQuestion.questionSticker.questionId,response)
                }
            })
            fragmentQuestion.show(childFragmentManager,null)
        }
        return viewGroup
    }

    fun createSlider(v: View, storySlider: StorySlider): ViewGroup {
        val displayWidth = v.width
        val displayHeight = v.height
        val viewGroup = CardView(requireContext()).apply {
            val width = (storySlider.width * displayWidth).toInt()
            val height = (storySlider.height * displayHeight).toInt()
            layoutParams = ConstraintLayout.LayoutParams(width, height).apply {
                this.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                this.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                this.topMargin = (displayHeight * storySlider.y).toInt() - height / 2
                this.leftMargin = (displayWidth * storySlider.x).toInt() - width / 2
            }
            this.isClickable = true
            this.isFocusable = true
            this.rotation = (storySlider.rotation * 360).toFloat()
            this.radius = dpToPx(10f, resources).toFloat()
            this.setCardBackgroundColor(Color.parseColor(storySlider.sliderSticker.backgroundColor))
        }

        val linearLayout = LinearLayout(requireContext()).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(dpToPx(10f, resources))
            }
            this.orientation = LinearLayout.VERTICAL
        }
        viewGroup.addView(linearLayout)

        val questionText = AppCompatTextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setTextColor(Color.parseColor(storySlider.sliderSticker.textColor))
            text = storySlider.sliderSticker.question
            gravity = Gravity.CENTER
        }
        linearLayout.addView(questionText)

        val seekBar = SeekBar(requireContext()).apply {
            layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
                    weight = 1f
                }
            max = 10
            thumb = TextDrawable(requireContext()).apply {
                text = storySlider.sliderSticker.emoji
                textAlign = Layout.Alignment.ALIGN_CENTER
            }
        }
        if(storySlider.sliderSticker.viewerVote != -1.0){
            seekBar.progress = (storySlider.sliderSticker.viewerVote * 10).toInt()
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.voteSlider(
                    currentPosition,
                    currentTray!!.items[currentPosition].id,
                    storySlider.sliderSticker.sliderId,
                    (seekBar!!.progress.toFloat() / 10.toFloat())
                )
            }
        })
//        seekBar.setOnTouchListener { v, event ->
//            return@setOnTouchListener storySlider.sliderSticker.viewerVote != -1.0
//        }
        linearLayout.addView(seekBar)

        return viewGroup
    }

    fun createReelMention(v: View, reelMention: ReelMention): FrameLayout {
        val viewGroup = createClickableLayout(v, reelMention)
        viewGroup.setOnClickListener {
            pauseTimer()
            isPopupShow = true
            val popup = PopupWindow(context)
            val layout: LayoutStoryItemPopupBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.layout_story_item_popup,
                null,
                false
            )
            mGlide.load(reelMention.user.profilePicUrl).into(layout.imgProfile)
            layout.txtInfo.text = reelMention.user.fullName
            layout.imgProfile.visibility = View.VISIBLE
            popup.contentView = layout.root
            popup.isOutsideTouchable = true
            popup.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            );
            popup.showAsDropDown(viewGroup)
        }
        return viewGroup
    }

    fun createClickableLayout(v: View, baseStoryItem: BaseStoryItem): FrameLayout {
        val displayWidth = v.width
        val displayHeight = v.height

        val viewGroup = FrameLayout(requireContext()).apply {
            val width = (baseStoryItem.width * displayWidth).toInt()
            val height = (baseStoryItem.height * displayHeight).toInt()
            layoutParams = ConstraintLayout.LayoutParams(width, height).apply {
                this.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                this.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                this.topMargin = (displayHeight * baseStoryItem.y).toInt() - height / 2
                this.leftMargin = (displayWidth * baseStoryItem.x).toInt() - width / 2
            }
            this.isClickable = true
            this.isFocusable = true
            this.rotation = (baseStoryItem.rotation * 360).toFloat()
        }
        return viewGroup
    }

    fun createPoll(v: View, storyPoll: StoryPoll): FrameLayout {
        val displayWidth = v.width
        val displayHeight = v.height

        val viewGroup = FrameLayout(requireContext()).apply {
            val width = (storyPoll.width * displayWidth).toInt()
            val height = (storyPoll.height * displayHeight).toInt()
            layoutParams = ConstraintLayout.LayoutParams(width, height).apply {
                this.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                this.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                this.topMargin = (displayHeight * storyPoll.y).toInt() - height / 2
                this.leftMargin = (displayWidth * storyPoll.x).toInt() - width / 2
            }
            this.isClickable = true
            this.isFocusable = true
            this.rotation = (storyPoll.rotation * 360).toFloat()
        }
        viewGroup.setOnClickListener {
            CustomToast.show(requireContext(), "Poll", Toast.LENGTH_SHORT)
        }

        return viewGroup
    }
}