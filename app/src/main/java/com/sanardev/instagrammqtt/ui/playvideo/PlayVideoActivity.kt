package com.sanardev.instagrammqtt.ui.playvideo

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.core.BaseActivity
import com.sanardev.instagrammqtt.core.BaseApplication
import com.sanardev.instagrammqtt.databinding.ActivityPlayVideoBinding
import java.io.File


class PlayVideoActivity : BaseActivity<ActivityPlayVideoBinding, PlayVideoViewModel>() {
    private lateinit var player: SimpleExoPlayer

    var isHideUiSystem:Boolean = true
    companion object {
        fun playUrl(context: Context, url: String) {
            context.startActivity(Intent(context, PlayVideoActivity::class.java).apply {
                putExtra("type", "url")
                putExtra("url", url)
            })
        }

        fun playFile(context: Context, filePath: String) {
            context.startActivity(Intent(context, PlayVideoActivity::class.java).apply {
                putExtra("type", "file")
                putExtra("file_path", filePath)
            })
        }
    }

    override fun layoutRes(): Int {
        return R.layout.activity_play_video
    }

    override fun getViewModelClass(): Class<PlayVideoViewModel> {
        return PlayVideoViewModel::class.java
    }

    override fun onResume() {
        super.onResume()
        val mAudioManager =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener {
                        //Handle Focus Change
                    }.build()
            )
        } else {
            mAudioManager.requestAudioFocus(
                { focusChange: Int -> },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras == null) {
            finish()
            return
        }

        val type = intent.extras!!.getString("type")
        val dataSource :DataSource.Factory
        val uri:Uri
        if (type == "url") {
            dataSource = DefaultHttpDataSourceFactory(Util.getUserAgent(this, "Instagram"))
            uri = Uri.parse(intent.extras!!.getString("url"))
        } else {
            dataSource = DefaultDataSourceFactory(this, Util.getUserAgent(this, "Instagram"))
            uri = Uri.fromFile(File(intent.extras!!.getString("file_path")!!))
        }
        val mediaSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSource)
                .createMediaSource(uri)
        BaseApplication.startPlay(mediaSource)
        binding.videoView.player = BaseApplication.player

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
    }
    override fun onStop() {
        super.onStop()
        BaseApplication.stopPlay()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
    }
    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}