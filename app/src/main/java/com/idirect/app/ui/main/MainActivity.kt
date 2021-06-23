package com.idirect.app.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.idirect.app.R
import com.idirect.app.core.BaseActivity
import com.idirect.app.core.BaseFragment
import com.idirect.app.databinding.ActivityMainBinding
import com.idirect.app.datasource.model.event.*
import com.idirect.app.extentions.color
import com.idirect.app.realtime.commands.RealTime_ClearCache
import com.idirect.app.realtime.commands.RealTime_StartService
import com.idirect.app.realtime.commands.RealTime_StopService
import com.idirect.app.realtime.service.RealTimeService
import com.idirect.app.ui.direct.FragmentDirect
import com.idirect.app.ui.forward.ForwardBundle
import com.idirect.app.ui.forward.ForwardFragment
import com.idirect.app.ui.forward.ForwardListener
import com.idirect.app.ui.home.FragmentHome
import com.idirect.app.ui.inbox.FragmentInbox
import com.idirect.app.ui.profile.FragmentProfile
import com.idirect.app.ui.search.FragmentSearch
import com.idirect.app.utils.Resource
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivity<ActivityMainBinding, ShareViewModel>() {

    private var onBackPress: Boolean = false
    private lateinit var navHostFragment: NavHostFragment

    companion object {
        const val HOME_POSITION = 0
        const val INBOX_POSITION = 1
        const val SEARCH_POSITION = 2
        const val PROFILE_POSITION = 2
    }

    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<ShareViewModel> {
        return ShareViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        val attributes = window.attributes
//        attributes.flags =
//            attributes.flags or (WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        window.attributes = attributes
        super.onCreate(savedInstanceState)

        viewModel.mutableLiveData.observe(this, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                RealTimeService.run(
                    this,
                    RealTime_StartService(
                        it.data!!.seqId.toLong(),
                        it.data!!.snapshotAtMs
                    )
                )
            }
        })
        attachKeyboardListeners()
        navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)

        navHostFragment.childFragmentManager.addOnBackStackChangedListener {
            val lastFragment = getLastFragment()
            when (lastFragment.getNameTag()) {
                FragmentHome.NAME_TAG -> {
                    binding.ahbottomNavigation.currentItem = HOME_POSITION
                }
                FragmentInbox.NAME_TAG -> {
                    binding.ahbottomNavigation.currentItem = INBOX_POSITION
                }
                FragmentProfile.NAME_TAG -> {
                    binding.ahbottomNavigation.currentItem = PROFILE_POSITION
                }
//                FragmentSearch.NAME_TAG ->{
//                    binding.ahbottomNavigation.currentItem = SEARCH_POSITION
//                }
            }
        }
        val homeItem =
            AHBottomNavigationItem(getString(R.string.home), R.drawable.instagram_home_outline_24)
        val directItem = AHBottomNavigationItem(
            getString(R.string.direct),
            R.drawable.instagram_direct_outline_24
        )
        val searchItem = AHBottomNavigationItem(
            getString(R.string.direct),
            R.drawable.instagram_search_outline_24
        )
        val profileItem =
            AHBottomNavigationItem(getString(R.string.direct), R.drawable.profile_single)

        binding.ahbottomNavigation.addItem(homeItem)
        binding.ahbottomNavigation.addItem(directItem)
//        binding.ahbottomNavigation.addItem(searchItem)
        binding.ahbottomNavigation.addItem(profileItem)

        binding.ahbottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_HIDE
        binding.ahbottomNavigation.defaultBackgroundColor = color(R.color.navigation_background)
        binding.ahbottomNavigation.inactiveColor = color(R.color.navigation_inactiveItem)
        binding.ahbottomNavigation.accentColor = color(R.color.navigation_currentItem)
        binding.ahbottomNavigation.setOnTabSelectedListener(object :
            AHBottomNavigation.OnTabSelectedListener {
            override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
                if (onBackPress) {
                    onBackPress = false
                    return true
                }
                if (wasSelected) {
                    val nameTagLastFragment = getLastFragment().getNameTag()
                    if( (nameTagLastFragment == FragmentHome.NAME_TAG && position == HOME_POSITION) ||
                        (nameTagLastFragment == FragmentInbox.NAME_TAG && position == INBOX_POSITION) ||
                        (nameTagLastFragment == FragmentProfile.NAME_TAG && position == PROFILE_POSITION)){
//                        (nameTagLastFragment == FragmentSearch.NAME_TAG && position == SEARCH_POSITION)){
                        return false
                    }
                }
                when (position) {
                    INBOX_POSITION -> {
                        navHostFragment.navController.navigate(R.id.action_global_inboxFragment)
                    }
                    HOME_POSITION -> {
                        navHostFragment.navController.navigate(R.id.action_global_homeFragment)
                    }
                    PROFILE_POSITION -> {
                        navHostFragment.navController.navigate(R.id.action_global_profileFragment)
                    }
//                    SEARCH_POSITION ->{
//                        navHostFragment.navController.navigate(R.id.action_global_searchFragment)
//                    }
                }
                return true
            }
        })
//        FbnsService.run(this, FbnsIntent.ACTION_CONNECT_SESSION)
    }

    fun isHideNavigationBottom(isHide: Boolean) {
        if (isHide) {
            binding.ahbottomNavigation.visibility = View.GONE
        } else {
            binding.ahbottomNavigation.visibility = View.VISIBLE
        }
    }

    fun showShareWindow(forwardBundle: ForwardBundle, forwardListener: ForwardListener? = null) {
        ForwardFragment()
            .setBundle(forwardBundle)
            .setListener(forwardListener)
            .show(supportFragmentManager, ForwardFragment::class.java.name)
    }

    private fun getLastFragment(): BaseFragment<*, *> {
        return navHostFragment.childFragmentManager.fragments[0] as BaseFragment<*, *>
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onConnectionStateEvent(connectionStateEvent: ConnectionStateEvent) {
        viewModel.setConnectionState(connectionStateEvent)
        when (connectionStateEvent.connection) {
            ConnectionStateEvent.State.CONNECTING -> {

            }
            ConnectionStateEvent.State.CONNECTED -> {

            }
            ConnectionStateEvent.State.NETWORK_DISCONNECTED -> {

            }
            ConnectionStateEvent.State.CHANNEL_DISCONNECTED -> {
                viewModel.reloadDirects()
            }
            ConnectionStateEvent.State.NETWORK_CONNECTION_RESET -> {
                viewModel.reloadDirects()
            }
            ConnectionStateEvent.State.NEED_TO_REALOD_DIRECT -> {
                EventBus.getDefault().removeStickyEvent(connectionStateEvent)
                viewModel.reloadDirects()
            }
            else -> {

            }
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(events: MutableList<MessageItemEvent>) {
        for (event in events) {
            viewModel.onMessageReceive(event)
        }
        RealTimeService.run(this, RealTime_ClearCache())
        EventBus.getDefault().removeStickyEvent(events)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageResponseEvent(event: MessageResponse) {
        viewModel.onMessageResponseEvent(event)
        EventBus.getDefault().removeStickyEvent(event)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageRemoveEvent(event: MutableList<MessageRemoveEvent>) {
        for (item in event) {
            viewModel.deleteMessage(item)
        }
        EventBus.getDefault().removeStickyEvent(event)
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateSeenEvent(event: UpdateSeenEvent) {
        viewModel.onUpdateSeenEvent(event)
        EventBus.getDefault().removeStickyEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTypingEvent(event: TypingEvent) { /* Do something */
        viewModel.onTyping(event)
        EventBus.getDefault().removeStickyEvent(event)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPresenceEvent(event: PresenceEvent) { /* Do something */
        viewModel.onPresenceEvent(event)
        EventBus.getDefault().removeStickyEvent(event)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        RealTimeService.run(this, RealTime_StopService())
    }

    override fun onShowKeyboard(keyboardHeight: Int) {
        super.onShowKeyboard(keyboardHeight)
        navHostFragment.let {
            val frg = it.childFragmentManager.fragments[0] as BaseFragment<*, *>
            frg.onKeyboardOpen()
        }
    }

    override fun onHideKeyboard() {
        super.onHideKeyboard()
        navHostFragment.let {
            val frg = it.childFragmentManager.fragments[0] as BaseFragment<*, *>
            frg.onKeyboardHide()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onBackPress = true
    }

}