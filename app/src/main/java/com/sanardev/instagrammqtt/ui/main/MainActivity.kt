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
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


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

    private val DEFAULT_HOST = "ssl://mqtt-mini.facebook.com:443"
    private val DEFAULT_PORT = 443
    @Throws(Exception::class)
    fun connect(protogle: String) {
        val topic = "MQTT Examples"
        val content = "Message from MqttPublishSample"
        val qos = 2
        val broker = DEFAULT_HOST
        val clientId = "882bf190-b878-448e-4"
        val persistence = MemoryPersistence()

        try {
            val trustAllCerts: Array<TrustManager> = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {

                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate>? {
                        return null
                    }
                }
            )
            val sslContext: SSLContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val sampleClient = MqttClient(broker, clientId, persistence)
            val connOpts = MqttConnectOptions()
            connOpts.socketFactory = sslContext.socketFactory
            connOpts.userName = null
            connOpts.password = null
            connOpts.keepAliveInterval = 900
            connOpts.isCleanSession = true
            Log.i("TEST_APPLICATION","Connecting to broker: $broker")
            sampleClient.connect(connOpts)
            Log.i("TEST_APPLICATION","Connected")
            Log.i("TEST_APPLICATION","Publishing message: $content")
            val message = MqttMessage(content.toByteArray())
            message.qos = qos
            sampleClient.publish(topic, message)
            Log.i("TEST_APPLICATION","Message published")
            sampleClient.disconnect()
            Log.i("TEST_APPLICATION","Disconnected")
            System.exit(0)
        } catch (me: MqttException) {
            Log.i("TEST_APPLICATION","reason " + me.reasonCode)
            Log.i("TEST_APPLICATION","msg " + me.message)
            Log.i("TEST_APPLICATION","loc " + me.localizedMessage)
            Log.i("TEST_APPLICATION","cause " + me.cause)
            Log.i("TEST_APPLICATION","excep $me")
            me.printStackTrace()
        }
    }

    companion object {

        private val HOSTNAME = "mqtt-mini.facebook.com:443"
        private val USER_AGENT =
            "[FBAN/MQTT;FBAV/130.0.0.31.121;FBBV/200396014;FBDM/{density=3.0,width=1080,height=1920};FBLC/en_GB;FBCR/;FBMF/Xiaomi;FBBD/Xiaomi;FBPN/com.instagram.android;FBDV/Mi 8;FBSV/10;FBLR/0;FBBK/1;FBCA/arm64-v8a:;]"

        private val PORT = 443
        private val MAX_EXECUTION_TIME = 8
    }
}