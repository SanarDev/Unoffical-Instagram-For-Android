package com.sanardev.instagrammqtt.core

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.di.component.DaggerAppComponent
import com.sanardev.instagrammqtt.receiver.NetworkChangeReceiver
import com.sanardev.instagrammqtt.utils.StorageUtils
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import java.io.File
import javax.inject.Inject


class BaseApplication : Application() , HasActivityInjector, HasServiceInjector{

    companion object{
        private lateinit var player:SimpleExoPlayer
        var currentPlayerId:String = ""
        var seekbarPlay:ProgressBar?=null
        var btnPlay:AppCompatImageButton?=null
        private var isFinishMedia = false

        fun startPlay(mediaSource: MediaSource){
            seekbarPlay?.progress = 0
            btnPlay?.setImageResource(R.drawable.ic_play_circle)
            player.prepare(mediaSource)
            player.playWhenReady = true

        }
        fun stopPlay(){
            player.playWhenReady = false
        }
    }

    private lateinit var runnable: Runnable

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    override fun activityInjector(): DispatchingAndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

    val br:BroadcastReceiver = NetworkChangeReceiver()

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)

        StorageUtils.deleteDir(File(StorageUtils.APPLICATION_DIR))

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        registerReceiver(br, filter)

        initializePlayer()

/*
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        */
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build()

        player.addListener(object :Player.EventListener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when(playbackState){
                    Player.STATE_ENDED ->{
                        seekbarPlay?.progress = 0
                        btnPlay?.setImageResource(R.drawable.ic_play_circle)
                        currentPlayerId = ""
                        isFinishMedia = true
                    }
                    Player.STATE_READY ->{
                        isFinishMedia = false
                    }
                    else ->{

                    }
                }
            }
        })
        val handler = Handler()
        runnable = Runnable {
            if(!isFinishMedia){
                seekbarPlay?.progress = (player.currentPosition * 100 / player.duration).toInt()
            }
            handler.postDelayed(runnable, 100)
        }
        handler.postDelayed(runnable, 0)
    }
}