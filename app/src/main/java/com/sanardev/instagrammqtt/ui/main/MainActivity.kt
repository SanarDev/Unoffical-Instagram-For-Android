package com.sanardev.instagrammqtt.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sanardev.instagrammqtt.base.BaseActivity

import com.sanardev.instagrammqtt.R
import com.sanardev.instagrammqtt.databinding.ActivityMainBinding
import com.kozaris.android.k_mqtt.Connection.ConnectionStatus
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import com.kozaris.android.k_mqtt.*
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener


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

    private val DEFAULT_HOST = "edge-mqtt.facebook.com"
    private val DEFAULT_PORT = 443
    @Throws(Exception::class)
    fun connect(protogle: String) {
        val ClientId = "TestMQTTClient"
        val Qos = 0
        val ServerHostName = DEFAULT_HOST
        val ServerPort = DEFAULT_PORT
        val TlsConnection = true
//Initialize Connection object
        val mqttConnection =
            Connection.createConnection(ClientId, ServerHostName, ServerPort, this, TlsConnection)
        val conOptions = MqttConnectOptions()
        conOptions.setConnectionTimeout(10)
        conOptions.setKeepAliveInterval(200)
        conOptions.setCleanSession(true)
        mqttConnection.addConnectionOptions(conOptions)
//Property changed Listener
        mqttConnection.registerChangeListener(object:PropertyChangeListener{
            override fun propertyChange(evt: PropertyChangeEvent?) {

            }
        })
        mqttConnection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING)
        mqttConnection.getClient().setTraceCallback(MqttClient.MqttTraceCallback())
//Register the Activity as a Message Receiver if required (only if it receives mqtt messages)
        mqttConnection.addReceivedMessageListener(this)

        MqttClient.getInstance(this).setConnection(this,mqttConnection)

        //Retrieve the Connection Object
        val con = MqttClient.getInstance(this).connection
//connect
        if (con != null) {
            val callback = ActionListener(this, ActionListener.Action.CONNECT, con)
            con.client.setCallback(MqttCallbackHandler(this))
            try {
                con.client.connect(con.connectionOptions, null, callback)
            } catch (e: MqttException) {
                Log.e(
                    this.javaClass.canonicalName,
                    "MqttException occurred", e
                )
                mqttConnection.changeConnectionStatus(Connection.ConnectionStatus.ERROR)
            }

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