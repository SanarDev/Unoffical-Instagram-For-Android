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

class PlayManager constructor(context:Context){

    companion object{
        const val NONE = -1
    }

    interface OnPlayChangeListener{
        fun onStart()
        fun onStop()
    }
    var player: SimpleExoPlayer = SimpleExoPlayer.Builder(context).build()
    var playChangeLiveData = MutableLiveData<PlayProperties>()

    var currentPlayerId: String? = null

    // for voice media
    var seekbarPlay: ProgressBar? = null
    var btnPlay: AppCompatImageButton? = null
    private lateinit var runnable: Runnable

    private var isFinishMedia = false

    init {

        player.addListener(object : Player.EventListener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when(playbackState){
                    Player.STATE_ENDED ->{
                        seekbarPlay?.progress = 0
                        btnPlay?.setImageResource(R.drawable.ic_play_circle)
                        currentPlayerId = ""
                        isFinishMedia = true
                        player.seekTo(0)
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
    fun startPlay(mediaSource: MediaSource,playerId:String,seekTo:Long=0) {
        seekbarPlay?.progress = 0
        btnPlay?.setImageResource(R.drawable.ic_play_circle)
        player.prepare(mediaSource)
        player.playWhenReady = true
        player.seekTo(seekTo)
        playChangeLiveData.postValue(PlayProperties(player.playWhenReady,playerId,currentPlayerId))
        currentPlayerId = playerId
    }

    fun releasePlay() {
        player.release()
    }

    fun stopPlay() {
        player.stop()
        playChangeLiveData.postValue(PlayProperties(player.playWhenReady,currentPlayerId,null))
        currentPlayerId = ""
    }

    fun resumePlay() {
        player.playWhenReady = true
    }

    fun disableSound(){
        player.volume = 0.0f
    }
    fun pausePlay(){
        player.playWhenReady = false
    }
    fun enableSound(){
        player.volume = 1.0f
    }
    fun isSoundEnable(): Boolean {
        return player.volume == 1.0f
    }


}