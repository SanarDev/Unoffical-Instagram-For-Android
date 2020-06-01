package com.sanardev.instagrammqtt.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sanardev.instagrammqtt.base.BaseActivity

import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.databinding.ActivityMainBinding
import net.igenius.mqttservice.MQTTServiceCommand
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import javax.net.ssl.SSLContext
import com.google.gson.Gson
import com.google.gson.JsonObject
import net.igenius.mqttservice.MQTTServiceReceiver
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.content.Context
import net.igenius.mqttservice.MQTTService
import net.igenius.mqttservice.MQTTServiceLogger

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    private lateinit var client: MqttClient
    private var broker: String? = null
    private var mqttClient: MqttClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            connect("ssl")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private val DEFAULT_HOST = "edge-mqtt.facebook.com"
    private val DEFAULT_PORT = 443
    @Throws(Exception::class)
    fun connect(protogle: String) {
        MQTTServiceCommand.connect(this, "ssl://edge-mqtt.facebook.com:443",
            "882bf180-b865-445e-7", null,
            null)

        MQTTServiceLogger.setLoggerDelegate(object : MQTTServiceLogger.LoggerDelegate {
            override fun error(tag: String, message: String) {
                //your own implementation here
            }

            override fun error(tag: String, message: String, exception: Throwable) {
                //your own implementation here
            }

            override fun debug(tag: String, message: String) {
                MQTTServiceCommand.getBroadcastAction()
                //your own implementation here
            }

            override fun info(tag: String, message: String) {
                //your own implementation here
            }
        })
    }

    companion object {

        private val HOSTNAME = "mqtt-mini.facebook.com:443"
        private val USER_AGENT =
            "[FBAN/MQTT;FBAV/130.0.0.31.121;FBBV/200396014;FBDM/{density=3.0,width=1080,height=1920};FBLC/en_GB;FBCR/;FBMF/Xiaomi;FBBD/Xiaomi;FBPN/com.instagram.android;FBDV/Mi 8;FBSV/10;FBLR/0;FBBK/1;FBCA/arm64-v8a:;]"

        private val PORT = 443
        private val MAX_EXECUTION_TIME = 8
    }
}