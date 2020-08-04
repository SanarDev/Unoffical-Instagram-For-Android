package com.idirect.app.ui.main

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.idirect.app.R
import com.idirect.app.core.BaseActivity
import com.idirect.app.databinding.ActivityMainBinding
import com.idirect.app.datasource.model.event.*
import com.idirect.app.realtime.commands.RealTime_ClearCache
import com.idirect.app.realtime.commands.RealTime_StartService
import com.idirect.app.realtime.commands.RealTime_StopService
import com.idirect.app.fbns.service.FbnsIntent
import com.idirect.app.fbns.service.FbnsService
import com.idirect.app.realtime.service.RealTimeService
import com.idirect.app.utils.Resource
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivity<ActivityMainBinding, ShareViewModel>() {

    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<ShareViewModel> {
        return ShareViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.mutableLiveData.observe(this, Observer {
            if(it.status == Resource.Status.SUCCESS){
                RealTimeService.run(
                    this,
                    RealTime_StartService(
                        it.data!!.seqId.toLong(),
                        it.data!!.snapshotAtMs
                    )
                )
            }
        })
        FbnsService.run(this, FbnsIntent.ACTION_CONNECT_SESSION)

        supportFragmentManager.addOnBackStackChangedListener(object :FragmentManager.OnBackStackChangedListener{
            override fun onBackStackChanged() {

            }
        })
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
    fun onMessageResponseEvent(event:MessageResponse){
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
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    override fun onDestroy() {
        super.onDestroy()
        RealTimeService.run(this, RealTime_StopService())
    }

}