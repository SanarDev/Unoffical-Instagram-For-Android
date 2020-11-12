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
import androidx.viewpager.widget.ViewPager
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.FragmentStoryBinding
import com.idirect.app.ui.main.MainActivity
import com.idirect.app.utils.Resource
import javax.inject.Inject

class FragmentStory : BaseFragment<FragmentStoryBinding, StoryViewModel>() {

    private var _mAdapter: StoriesAdapter? = null
    private val mAdapter: StoriesAdapter get() = _mAdapter!!
    private var userId: Long = 0

    @Inject lateinit var mHandler: Handler

    override fun getViewModelClass(): Class<StoryViewModel> {
        return StoryViewModel::class.java
    }

    override fun layoutRes(): Int {
        return R.layout.fragment_story
    }

    override fun getNameTag(): String {
        return "story"
    }

    private var lastPosition = 0
    private var _mStoryActionListener: StoryActionListener? = null
    private val mStoryActionListener: StoryActionListener get() = _mStoryActionListener!!
    private val fragments = HashMap<Int, FragmentStoryItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Hide status bar
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = requireArguments().getString("user_id")!!.toLong()
        val isSingle = requireArguments().getBoolean("is_single")
        viewModel.getStoryData(userId, isSingle)
        (requireActivity() as MainActivity).isHideNavigationBottom(true)

        _mStoryActionListener = object : StoryActionListener {
            override fun loadNextPage() {
                if (binding.viewPager.currentItem < binding.viewPager.adapter!!.count - 1) {
                    binding.viewPager.currentItem = binding.viewPager.currentItem + 1
                } else {
                    activity?.onBackPressed()
                }
            }

            override fun onProfileClick(v: View, userId: Long, username: String) {
//                val data = UserBundle().apply {
//                    this.userId = userId
//                    this.username = username
//                }
//                val action = NavigationMainGraphDirections.actionGlobalUserProfileFragment(data)
//                v.findNavController().navigate(action)
//                fragments[binding.viewPager.currentItem]?.mPlayManager?.stopPlay()
            }
        }
        _mAdapter = StoriesAdapter(null, childFragmentManager)
        binding.viewPager.adapter = mAdapter
        binding.viewPager.currentItem = lastPosition
        Log.i(InstagramConstants.DEBUG_TAG, "lastPosition $lastPosition")
        binding.viewPager.setPageTransformer(true, RotateUpTransformer())
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position != -1 && positionOffsetPixels == 0 && lastPosition != position) {
                    fragments[lastPosition]?.onPause()
                    fragments[lastPosition]?.apply {
                        currentPosition -= 1
                        onPause()
                    }
                    fragments[position]?.showNextItem()
                    lastPosition = position
                } else if (lastPosition == position && position == binding.viewPager.adapter!!.count - 1) {
//                    activity?.onBackPressed()
                }
                fragments[binding.viewPager.currentItem]?.isTouchEnable = positionOffsetPixels == 0
            }

            override fun onPageSelected(position: Int) {

            }
        })
        viewModel.storiesData.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                mAdapter.items = it.data!!
                mAdapter.notifyDataSetChanged()
                for (index in it.data!!.indices) {
                    if (it.data!![index].user.pk == userId) {
                        binding.viewPager.currentItem = index
                    }
                }
            }
        })

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
                FragmentStoryItem(item.user.pk, mStoryActionListener, item.user.pk == userId)
            fragments.put(position, fragment)
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            super.destroyItem(container, position, `object`)
            fragments[position]?.mPlayManager?.releasePlay()
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
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        for (item in fragments.entries) {
            item.value.playItemAfterLoad = false
            item.value.mPlayManager.stopPlay()
        }
    }

}