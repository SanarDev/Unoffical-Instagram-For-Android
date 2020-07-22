package com.sanardev.instagrammqtt.ui.playvideo

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.core.BaseActivity
import com.sanardev.instagrammqtt.databinding.ActivityPlayVideoBinding
import java.io.File


class PlayVideoActivity : BaseActivity<ActivityPlayVideoBinding, PlayVideoViewModel>() {
    private lateinit var player: SimpleExoPlayer

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
        player = SimpleExoPlayer.Builder(this).build()
        val mediaSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSource)
                .createMediaSource(uri)
        player.prepare(mediaSource)
        player.volume = 100f
        player.playWhenReady = true
        binding.videoView.player = player


    }

    override fun onPause() {
        super.onPause()
    }
    override fun onStop() {
        super.onStop()
        player.release()
    }
}