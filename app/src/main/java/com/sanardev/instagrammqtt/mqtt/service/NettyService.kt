package com.sanardev.instagrammqtt.mqtt.service

import android.content.Intent
import android.util.Log
import com.hovans.android.service.ServiceUtil
import com.hovans.android.service.WorkerService
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.mqtt.network.ChannelDecoder
import com.sanardev.instagrammqtt.mqtt.network.ChannelEncoder
import com.sanardev.instagrammqtt.mqtt.network.NetworkEventHandler
import com.sanardev.instagrammqtt.mqtt.network.PayloadProcessor
import com.sanardev.instagrammqtt.mqtt.packethelper.FbnsConnectPacket
import com.sanardev.instagrammqtt.mqtt.packethelper.MQTToTConnectionData
import com.sanardev.instagrammqtt.mqtt.packethelper.MQTTotConnectionClientInfo
import com.sanardev.instagrammqtt.mqtt.service.NettyService
import com.sanardev.instagrammqtt.mqtt.util.ThreadManager
import com.sanardev.instagrammqtt.mqtt.util.WakeLockWrapper
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Executors

/**
 * This class is the main class that controls the network connection.
 *
 * @author Hovan Yoo
 */
class NettyService : WorkerService() {
    var mChannel: Channel? = null
    override fun getWorkerTag(): String {
        return NettyService::class.java.simpleName
    }

    /**
     * [Service.onCreate]
     */
    override fun onCreate() {
        super.onCreate()
        ThreadManager.offer {
            val wakeLock =
                WakeLockWrapper.getWakeLockInstance(
                    this@NettyService,
                    workerTag
                )
            wakeLock.acquire()
            try {
                val mqttotConnectionClientInfo =
                    MQTTotConnectionClientInfo()
                mqttotConnectionClientInfo.userId = 0
                mqttotConnectionClientInfo.userAgent ="[FBAN/MQTT;FBAV/${InstagramConstants.APP_VERSION};FBBV/${InstagramConstants.APP_ID};FBDM/{density=4.0,width=1440,height=2392};FBLC/en_US;FBCR/;FBMF/LGE;FBBD/lge;FBPN/com.instagram.android;FBDV/RS988;FBSV/6.0.1;FBLR/0;FBBK/1;FBCA/armeabi-v7a:armeabi;]"
                mqttotConnectionClientInfo.clientCapabilities = 183
                mqttotConnectionClientInfo.endpointCapabilities = 128
                mqttotConnectionClientInfo.publishFormat = 1
                mqttotConnectionClientInfo.noAutomaticForeground = false
                mqttotConnectionClientInfo.deviceId = ""
                mqttotConnectionClientInfo.isInitiallyForeground = false
                mqttotConnectionClientInfo.networkSubtype = 0
                mqttotConnectionClientInfo.clientMqttSessionId =
                    System.currentTimeMillis()
                mqttotConnectionClientInfo.subscribeTopics = intArrayOf(76, 80, 231)
                mqttotConnectionClientInfo.clientType = "device_auth"
                mqttotConnectionClientInfo.appId = InstagramConstants.APP_ID.toLong()
                mqttotConnectionClientInfo.deviceSecret = ""
                mqttotConnectionClientInfo.anotherUnknown = -1
                mqttotConnectionClientInfo.clientStack = -1
                val mqtToTConnectionData =
                    MQTToTConnectionData()
                mqtToTConnectionData.clientIdentifier =
                    UUID.randomUUID().toString().substring(0, 20)
                mqtToTConnectionData.clientInfo = mqttotConnectionClientInfo
                mqtToTConnectionData.password = ""
                val fbnsConnectPacket = FbnsConnectPacket()
                fbnsConnectPacket.payload = PayloadProcessor.buildPayload(mqtToTConnectionData)
                val factory = NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool()
                )
                val bootstrap = ClientBootstrap(factory)

                // Set up the pipeline factory.
                bootstrap.pipelineFactory = ChannelPipelineFactory {
                    Channels.pipeline(
                        NetworkEventHandler(this@NettyService)
                    )
                }

                // Bind and start to accept incoming connections.
                val future = bootstrap.connect(
                    InetSocketAddress(
                        SERVER_URL,
                        SERVER_PORT
                    )
                )
                future.awaitUninterruptibly()
                mChannel = future.channel
                mChannel!!.write(fbnsConnectPacket)
            } finally {
                wakeLock.release()
            }
        }
    }

    override fun onWorkerRequest(intent: Intent, i: Int) {
        if (NettyIntent.ACTION_CONNECT_SESSION == intent.action) {
            if (mChannel != null) {
                disconnectSessionIfItNeeds()
            }
            connectSessionIfItNeeds()
        } else if (NettyIntent.ACTION_HEARTBEAT == intent.action) {
            if (checkConnection() == false) {
                connectSessionIfItNeeds()
            }
        } else if (NettyIntent.ACTION_CHECK_SESSION == intent.action) {
            scheduleToReconnect()
        } else if (NettyIntent.ACTION_DISCONNECT_SESSION == intent.action) {
            disconnectSessionIfItNeeds()
        }
    }

    /** Session 의 연결상태를 확인한다. 필요할 경우 HeartBeat을 전송. */
    fun checkConnection(): Boolean {
        var result = false
        if (mChannel != null && mChannel!!.isConnected == true) {
            //If it needs you should send a packet through the channel.
            result = true
        }
        Log.i(
            "TEST_APPLICATION",
            NettyService::class.java.simpleName + ".checkConnection(), mChannel: " + mChannel + ", result: " + result
        )
        return result
    }

    /** 연결을 다시 맺어야 할 경우 connection 을 닫는다.
     */
    fun disconnectSessionIfItNeeds() {
        if (checkConnection() == true) {
            val future = mChannel!!.disconnect()
            future.awaitUninterruptibly()
        }
    }

    /** Schedule a reconnect event by using [android.app.AlarmManager] */
    fun scheduleToReconnect() {
        if (isConnectAlreadyScheduled == true) {
            return
        }

        //Random Integer 를 더해서 서버에 접속 부하를 줄인다.
        if (INTERVAL_RECONNECT_EXPONENTIAL_BACKOFF < INTERVAL_RECONNECT_MAXIMUM) {
            INTERVAL_RECONNECT_EXPONENTIAL_BACKOFF += Random()
                .nextInt(1000).toLong()
            Log.i(
                "TEST_APPLICATION",
                String.format(
                    "%s.scheduleToReconnect() delay: %dsec",
                    NettyService::class.java.simpleName,
                    INTERVAL_RECONNECT_EXPONENTIAL_BACKOFF / 1000
                )
            )
            ServiceUtil.startSchedule(
                this,
                NettyIntent.ACTION_CONNECT_SESSION,
                INTERVAL_RECONNECT_EXPONENTIAL_BACKOFF
            )
            INTERVAL_RECONNECT_EXPONENTIAL_BACKOFF *= 2
            isConnectAlreadyScheduled = true
        }
    }

    fun connectSessionIfItNeeds() {
        if (checkConnection() == false) {
            ServiceUtil.startSchedule(
                this,
                NettyIntent.ACTION_CHECK_SESSION,
                INTERVAL_WAIT_FOR_RESPONSE
            )
            ServiceUtil.stopSchedule(this, NettyIntent.ACTION_HEARTBEAT)
        }
    }

    companion object {
        var isConnectAlreadyScheduled = false
        const val SERVER_URL = "mqtt-mini.facebook.com"
        const val SERVER_PORT = 443

        /** request후 response가 30초 이내에 응답이 와야 함  */
        const val INTERVAL_WAIT_FOR_RESPONSE = 30 * 1000.toLong()
        const val INTERVAL_RECONNECT_MINIMUM = 10 * 1000.toLong()
        var INTERVAL_RECONNECT_EXPONENTIAL_BACKOFF =
            INTERVAL_RECONNECT_MINIMUM

        /** 이 값에 도달하면 서비스를 재시작 해본다. */
        const val INTERVAL_RECONNECT_MAXIMUM = 30 * 60 * 1000.toLong()
    }
}