package com.idirect.app.ui.fullscreen

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.transition.TransitionInflater
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.core.BaseApplication
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.ActivityFullScreenBinding
import com.idirect.app.extensions.visible
import com.idirect.app.ui.posts.FragmentCollection
import com.idirect.app.utils.Resource
import java.io.File

class FullScreenFragment : BaseFragment<ActivityFullScreenBinding, FullScreenViewModel>() {

    //        viewModel = ViewModelProvider(requireActivity()).get(getViewModelClass())
    companion object {
        const val TYPE_URL = 1
        const val TYPE_FILE = 2
        const val TYPE_POST = 3
    }
    val fragments = ArrayList<FragmentCollection>().toMutableList()

    override fun layoutRes(): Int {
        return R.layout.activity_full_screen
    }

    override fun getViewModelClass(): Class<FullScreenViewModel> {
        return FullScreenViewModel::class.java
    }

    /*
    requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = requireArguments()
        val type = bundle.getInt("type")
        val data = bundle.getString("data")!!
        val id = bundle.getString("id")

        ViewCompat.setTransitionName(binding.imageView,"media_${id}")
        when (type) {
            TYPE_URL -> {
                Glide.with(requireContext()).load(data).into(binding.imageView)
                visible(binding.imageView)
            }
            TYPE_FILE -> {
                Glide.with(requireContext()).load(File(data)).into(binding.imageView)
                visible(binding.imageView)
            }
            TYPE_POST -> {
                viewModel.getMediaById(data)
            }
        }

        viewModel.liveDataPost.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.ERROR -> {

                }
                Resource.Status.SUCCESS -> {
//                    visible(binding.viewPager)
//                    val items = it.data!!.items[0].carouselMedias
//                    for(item in items){
//                        val fragment = if (item.videoVersions != null) {
//                            FragmentCollection(
//                                InstagramConstants.MediaType.VIDEO,
//                                item.videoVersions[0].url
//                            )
//                        } else {
//                            FragmentCollection(
//                                InstagramConstants.MediaType.IMAGE,
//                                item.imageVersions2.candidates[0].url
//                            )
//                        }
//                        fragments.add(fragment)
//                    }
//                    binding.viewPager.adapter =
//                        CollectionPagerAdapter(fragments, requireActivity().supportFragmentManager)
//
//                    binding.viewPager.addOnPageChangeListener(object:ViewPager.OnPageChangeListener{
//                        override fun onPageScrollStateChanged(state: Int) {
//                        }
//
//                        override fun onPageScrolled(
//                            position: Int,
//                            positionOffset: Float,
//                            positionOffsetPixels: Int
//                        ) {
//                            if(fragments[position].itemType == InstagramConstants.MediaType.VIDEO){
////                                stopAllSounds()
//                                fragments[position].play()
//                            }
//                        }
//
//                        override fun onPageSelected(position: Int) {
//                        }
//                    })
                }
            }
        })
    }


//    private fun stopAllSounds(){
//        val mAudioManager =
//            getSystemService(Context.AUDIO_SERVICE) as AudioManager
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mAudioManager.requestAudioFocus(
//                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                    .setAudioAttributes(
//                        AudioAttributes.Builder()
//                            .setUsage(AudioAttributes.USAGE_GAME)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                            .build()
//                    )
//                    .setAcceptsDelayedFocusGain(true)
//                    .setOnAudioFocusChangeListener {
//                        //Handle Focus Change
//                    }.build()
//            )
//        } else {
//            mAudioManager.requestAudioFocus(
//                { focusChange: Int -> },
//                AudioManager.STREAM_MUSIC,
//                AudioManager.AUDIOFOCUS_GAIN
//            )
//        }
//    }
    inner class CollectionPagerAdapter(
        var items: List<Fragment>,
        fragmentManager: FragmentManager
    ) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        // Returns total number of pages
        override fun getCount(): Int {
            return items.size
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment {
            val item = items[position]
            return item
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun getNameTag(): String {
        return "full_screen_fragment"
    }

}