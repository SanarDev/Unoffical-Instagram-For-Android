package com.sanardev.instagrammqtt.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sanardev.instagrammqtt.base.BaseActivity

import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.databinding.ActivityMainBinding

import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import java.util.UUID
import java.util.logging.Logger

class MainActivity : BaseActivity<ActivityMainBinding,MainViewModel>() {
    override fun layoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

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

    @Throws(Exception::class)
    fun connect(protogle: String) {

        this.broker = "$protogle://$HOSTNAME"
        this.mqttClient = MqttClient(broker, "567310203415052", MemoryPersistence())

        val connOpts = MqttConnectOptions()
        connOpts.keepAliveInterval = 100
        connOpts.isCleanSession = true
        Log.i("TEST_APP_1", "Connecting to broker: " + broker!!)
        Log.i("TEST_APP_1", "isConnected:" + mqttClient!!.isConnected)
        try {
            val cn = mqttClient!!.connectWithResult(connOpts)
            Log.i("TEST_APP_1", "connected")
        } catch (me: MqttException) {
            Log.i("TEST_APP_1", "reason " + me.reasonCode)
            Log.i("TEST_APP_1", "msg " + me.message)
            Log.i("TEST_APP_1", "loc " + me.localizedMessage!!)
            Log.i("TEST_APP_1", "cause " + me.cause)
            Log.i("TEST_APP_1", "excep $me")
            return
        }


        this.mqttClient!!.setCallback(object : MqttCallback {
            override fun connectionLost(me: Throwable) {
                Log.i("TEST_APP_1", "Connection lost")
                Log.i("TEST_APP_1", "msg " + me.message)
                Log.i("TEST_APP_1", "loc " + me.localizedMessage!!)
                Log.i("TEST_APP_1", "cause " + me.cause)
                Log.i("TEST_APP_1", "excep $me")
            }

            @Throws(Exception::class)
            override fun messageArrived(s: String, mqttMessage: MqttMessage) {
                Log.i("TEST_APP_1", "message Arrived")
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.i("TEST_APP_1", "deliverd--------")
                try {
                    val token = iMqttDeliveryToken as MqttDeliveryToken
                    val h = token.message.toString()
                    Log.i("TEST_APP_1", "deliverd message :$h")
                } catch (me: MqttException) {
                    Log.i("TEST_APP_1", "reason " + me.reasonCode)
                    Log.i("TEST_APP_1", "msg " + me.message)
                    Log.i("TEST_APP_1", "loc " + me.localizedMessage!!)
                    Log.i("TEST_APP_1", "cause " + me.cause)
                    Log.i("TEST_APP_1", "excep $me")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })

    }

    companion object {

        private val HOSTNAME = "edge-mqtt.facebook.com:443"
        private val USER_AGENT =
            "[FBAN/MQTT;FBAV/130.0.0.31.121;FBBV/200396014;FBDM/{density=3.0,width=1080,height=1920};FBLC/en_GB;FBCR/;FBMF/Xiaomi;FBBD/Xiaomi;FBPN/com.instagram.android;FBDV/Mi 8;FBSV/10;FBLR/0;FBBK/1;FBCA/arm64-v8a:;]"

        private val PORT = 443
        private val MAX_EXECUTION_TIME = 8
    }
}