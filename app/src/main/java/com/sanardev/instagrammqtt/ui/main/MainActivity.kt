package com.sanardev.instagrammqtt.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.kozaris.android.k_mqtt.Connection
import com.kozaris.android.k_mqtt.ReceivedMessage
import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.base.BaseActivity
import com.sanardev.instagrammqtt.databinding.ActivityMainBinding
import org.eclipse.paho.android.service.MqttService
import org.fusesource.hawtbuf.Buffer
import org.fusesource.hawtbuf.UTF8Buffer
import org.fusesource.mqtt.client.Callback
import org.fusesource.mqtt.client.Listener
import org.fusesource.mqtt.client.MQTT


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() ,Connection.IReceivedMessageListener{
    override fun onMessageReceived(message: ReceivedMessage?) {

    }

    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    private var broker: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            connect("ssl")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onPause() {
        Log.i("t","t")
        super.onPause()
    }

    private val DEFAULT_HOST = "mqtt-mini.facebook.com"
    private val DEFAULT_PORT = 443
    @Throws(Exception::class)
    fun connect(protogle: String) {
        ContextCompat.startForegroundService(this@MainActivity,Intent(this@MainActivity, MqttService::class.java))
    }

    companion object {

        private val HOSTNAME = "mqtt-mini.facebook.com:443"
        private val USER_AGENT =
            "[FBAN/MQTT;FBAV/130.0.0.31.121;FBBV/200396014;FBDM/{density=3.0,width=1080,height=1920};FBLC/en_GB;FBCR/;FBMF/Xiaomi;FBBD/Xiaomi;FBPN/com.instagram.android;FBDV/Mi 8;FBSV/10;FBLR/0;FBBK/1;FBCA/arm64-v8a:;]"

        private val PORT = 443
        private val MAX_EXECUTION_TIME = 8
    }
}