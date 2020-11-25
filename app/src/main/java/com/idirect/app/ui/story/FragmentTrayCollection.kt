package com.idirect.app.ui.story

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.idirect.app.NavigationMainGraphDirections
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentTrayCollectionBinding
import com.idirect.app.manager.PlayManager
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.ui.userprofile.UserBundle
import com.idirect.app.utils.Resource
import javax.inject.Inject

class FragmentTrayCollection :
    BaseFragment<FragmentTrayCollectionBinding, TrayCollectionViewModel>() {

    private var isSingle: Boolean = false
    private var _mAdapter: StoriesAdapter? = null
    private val mAdapter: StoriesAdapter get() = _mAdapter!!
    private var userId: Long = 0

    @Inject
    lateinit var mHandler: Handler

    @Inject
    lateinit var mPlayManager: PlayManager

    override fun getViewModelClass(): Class<TrayCollectionViewModel> {
        return TrayCollectionViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_tray_collection
    }

    override fun getNameTag(): String {
        return "story"
    }

    private var lastPosition = -1
    private var isStarted = false
    private var _mStoryActionListener: StoryActionListener? = null
    private val mStoryActionListener: StoryActionListener get() = _mStoryActionListener!!
    private val fragments = HashMap<Int, FragmentStory>()
    private lateinit var videoView: PlayerView

    override fun isHideStatusBar(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        videoView = LayoutInflater.from(context)
            .inflate(R.layout.story_player, null, false) as PlayerView
        videoView.player = mPlayManager.player
        mPlayManager.player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        fragments[binding.viewPager.currentItem]?.pauseTimer()
                    }
                    Player.STATE_READY -> {
                        fragments[binding.viewPager.currentItem]?.stateReady()
                    }
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Hide status bar
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireArguments().let {
            userId = it.getString("user_id")!!.toLong()
            isSingle = it.getBoolean("is_single")
        }
        (requireActivity() as MainActivity).isHideNavigationBottom(true)

        _mStoryActionListener = object : StoryActionListener {
            override fun loadNextPage() {
                if (binding.viewPager.currentItem < binding.viewPager.adapter!!.count - 1) {
                    binding.viewPager.currentItem = binding.viewPager.currentItem + 1
                } else {
                    activity?.onBackPressed()
                }
            }

            override fun loadPreviousPage() {
                if (binding.viewPager.currentItem > 0) {
                    binding.viewPager.currentItem = binding.viewPager.currentItem - 1
                }
            }

            override fun onProfileClick(v: View, userId: Long, username: String) {
                val data = UserBundle().apply {
                    this.userId = userId
                    this.username = username
                }
                val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(data)
                v.findNavController().navigate(action)
                fragments[binding.viewPager.currentItem]?.mPlayManager?.stopPlay()
            }

            override fun viewPost(mediaId: String) {
                val action = NavigationMainGraphDirections.actionGlobalSinglePostFragment(mediaId)
                findNavController().navigate(action)
            }

            override fun viewPage(userId: Long, username: String) {
                val data = UserBundle().apply {
                    this.userId = userId
                    this.username = username
                }
                val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(data)
                findNavController().navigate(action)
            }
        }

        _mAdapter = StoriesAdapter(null, childFragmentManager)
        binding.viewPager.adapter = mAdapter
        binding.viewPager.offscreenPageLimit = 2
        binding.viewPager.setPageTransformer(true, RotateUpTransformer())
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (!isStarted && lastPosition == position) {
                    fragments[position]?.let {
                        Log.i(
                            InstagramConstants.DEBUG_TAG,
                            "start for position: " + position
                        )
                        var isPlayAfterLoad = false
                        if (!isStarted && position == lastPosition) {
                            isPlayAfterLoad = true
                            isStarted = true
                        } else {
                            isPlayAfterLoad = false
                        }
                        it.playItemAfterLoad = isPlayAfterLoad
                        isStarted = true
                    }
                }
                if (isStarted && position != -1 && positionOffsetPixels == 0 && lastPosition != position) {
                    Log.i(
                        InstagramConstants.DEBUG_TAG,
                        "lastPosition: " + lastPosition + "| " + "position: " + position
                    )
                    fragments[lastPosition]?.apply {
                        showPreviousItem()
                        onPause()
                    }
                    fragments[position]?.let {
                        it.isPauseAnyThing = false
                        it.playItemAfterLoad = true
                        it.showCurrentItem()
                    }
                    lastPosition = position
                    requireArguments().putString(
                        "user_id",
                        mAdapter.items!![position].user.pk.toString()
                    )
                } else if (lastPosition == position && position == binding.viewPager.adapter!!.count - 1) {
                    requireActivity().onBackPressed()
                }
//                fragments[binding.viewPager.currentItem]?.isTouchEnable = positionOffsetPixels == 0
            }

            override fun onPageSelected(position: Int) {

            }
        })
        viewModel.storiesData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                mAdapter.items = it.data!!
                mAdapter.notifyDataSetChanged()
                isStarted = false
                it.data!!.forEachIndexed { index, tray ->
                    if (tray.user.pk == userId) {
                        lastPosition = index
                    }
                }
                binding.viewPager.currentItem = lastPosition
            }
        })

        viewModel.getStoryData(userId, isSingle, true)
    }


    inner class StoriesAdapter(
        var items: List<com.sanardev.instagramapijava.model.story.Tray>?,
        fragmentManager: FragmentManager
    ) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        // Returns total number of pages
        override fun getCount(): Int {
            return if (items == null) 0 else items!!.size
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment {
            val item = items!![position]
            val fragment =
                FragmentStory(
                    item.user.pk,
                    mStoryActionListener,
                    mPlayManager,
                    videoView
                )
            fragments.put(position, fragment)
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            super.destroyItem(container, position, `object`)
            fragments[position]?.mStoryActionListener = null
            fragments.remove(position)
        }
    }

//

    override fun onKeyboardHide() {
        super.onKeyboardHide()
        fragments[binding.viewPager.currentItem]?.onKeyboardHide()
    }

    override fun onKeyboardOpen() {
        super.onKeyboardOpen()
        fragments[binding.viewPager.currentItem]?.onKeyboardOpen()
    }

    override fun onDestroyView() {
        _mAdapter = null
        _mStoryActionListener = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayManager.releasePlay()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
        if (isStarted) {
            fragments[lastPosition]?.let {
                it.isPauseAnyThing = false
                it.playItemAfterLoad = true
                it.showCurrentItem()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i(InstagramConstants.DEBUG_TAG, "FragmentTrayCollection: onStop")
        mPlayManager.stopPlay()
    }

}