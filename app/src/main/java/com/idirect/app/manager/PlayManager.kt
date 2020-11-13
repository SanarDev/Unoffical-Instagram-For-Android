package com.idirect.app.manager

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants

class PlayManager constructor(context: Context) {

    companion object {
        const val NONE = -1
    }

    interface OnPlayChangeListener {
        fun onStart()
        fun onStop()
    }

    var _player: SimpleExoPlayer? = null
    val player: SimpleExoPlayer get() = _player!!
    var playChangeLiveData = MutableLiveData<PlayProperties>()

    var currentPlayerId: String? = null

    // for voice media
    var seekbarPlay: ProgressBar? = null
    var btnPlay: AppCompatImageButton? = null
    private var _runnable: Runnable? = null
    private val runnable: Runnable get() = _runnable!!

    private var isFinishMedia = false
    private var listener: Player.EventListener? = null

    init {
        _player = SimpleExoPlayer.Builder(context).build()
        listener = object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        seekbarPlay?.progress = 0
                        btnPlay?.setImageResource(R.drawable.ic_play_circle)
                        currentPlayerId = ""
                        isFinishMedia = true
                        if(player.repeatMode != SimpleExoPlayer.REPEAT_MODE_OFF){
                            player.seekTo(0)
                        }
                    }
                    Player.STATE_READY -> {
                        isFinishMedia = false
                    }
                    else -> {

                    }
                }
            }
        }

        player.addListener(listener!!)
        val handler = Handler()
        _runnable = Runnable {
            if (!isFinishMedia && _player != null) {
                seekbarPlay?.progress = (player.currentPosition * 100 / player.duration).toInt()
            }
            if(_runnable != null){
                handler.postDelayed(runnable, 100)
            }
        }
        handler.post(runnable)
    }

    fun startPlay(mediaSource: MediaSource, playerId: String, seekTo: Long = 0) {
        seekbarPlay?.progress = 0
        btnPlay?.setImageResource(R.drawable.ic_play_circle)
        player.prepare(mediaSource)
        player.playWhenReady = true
        player.seekTo(seekTo)
        if (currentPlayerId != playerId) {
            playChangeLiveData.postValue(
                PlayProperties(
                    player.playWhenReady,
                    playerId,
                    currentPlayerId
                )
            )
            currentPlayerId = playerId
        }
    }

    fun releasePlay() {
        if (_player != null) {
            pausePlay()
            player.removeListener(listener!!)
            player.release()
        }
        _runnable = null
        _player = null
        listener = null
    }

    fun stopPlay() {
        pausePlay()
        player.stop()
        playChangeLiveData.postValue(PlayProperties(player.playWhenReady, currentPlayerId, null))
        currentPlayerId = ""
    }

    fun resumePlay() {
        player.playWhenReady = true
    }

    fun disableSound() {
        player.volume = 0.0f
    }

    fun pausePlay() {
        player.playWhenReady = false
    }
    fun replay() {
        player.seekTo(0)
    }

    fun enableSound() {
        player.volume = 1.0f
    }

    fun isSoundEnable(): Boolean {
        return player.volume == 1.0f
    }

    fun setRepeat(isRepeatEnable:Boolean){
        if(isRepeatEnable){
            player.repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL
        }else{
            player.repeatMode = SimpleExoPlayer.REPEAT_MODE_OFF
        }
    }

}