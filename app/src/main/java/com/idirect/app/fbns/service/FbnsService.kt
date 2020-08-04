package com.idirect.app.fbns.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.idirect.app.R
import com.idirect.app.constants.InstagramConstants
import com.idirect.app.fbns.network.NetworkHandler
import com.idirect.app.fbns.network.PayloadProcessor
import com.idirect.app.fbns.packethelper.*
import com.idirect.app.usecase.UseCase
import com.idirect.app.utils.DisplayUtils
import dagger.android.AndroidInjection
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelPipeline
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.mqtt.*
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.SslProvider
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import java.util.*
import javax.inject.Inject


/**
 * This class is the main class that controls the network connection.
 *
 * @author Hovan Yoo
 */
class FbnsService : Service() {
    private lateinit var sslContext: SslContext
    var mChannel: Channel? = null

    /**
     * [Service.onCreate]
     */

    @Inject
    lateinit var mUseCase: UseCase

    override fun onCreate() {
        AndroidInjection.inject(this);
        super.onCreate()

        sslContext =
            SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                .sslProvider(SslProvider.JDK).build()

        startNotificationForeground(
            "checking for new notification",
            "checking for new notification"
        )
    }

    inner class ConnectivityThread : Thread() {
        override fun run() {
            try {
                val fbns = mUseCase.getFbnsAuthData()
                val mqttotConnectionClientInfo =
                    MQTTotConnectionClientInfo()
//                mqttotConnectionClientInfo.userId = 0
                mqttotConnectionClientInfo.userId = fbns.userId
//                mqttotConnectionClientInfo.userAgent = "[FBAN/MQTT;FBAV/130.0.0.31.121;FBBV/567067343352427;FBDM/{density=4.0,width=1080,height=2038};FBLC/en_US;FBCR/;FBMF/LGE;FBBD/lge;FBPN/com.instagram.android;FBDV/RS988;FBSV/6.0.1;FBLR/0;FBBK/1;FBCA/armeabi-v7a:armeabi;]"
                mqttotConnectionClientInfo.userAgent =
                    "[FBAN/MQTT;FBAV/${InstagramConstants.APP_VERSION};FBBV/${InstagramConstants.APP_ID};FBDM/{density=4.0,width=${DisplayUtils.getScreenWidth()},height=${DisplayUtils.getScreenHeight()}};FBLC/en_US;FBCR/${""};FBMF/LGE;FBBD/lge;FBPN/com.instagram.android;FBDV/RS988;FBSV/6.0.1;FBLR/0;FBBK/1;FBCA/armeabi-v7a:armeabi;]"
                mqttotConnectionClientInfo.clientCapabilities = 183
                mqttotConnectionClientInfo.endpointCapabilities = 128
                mqttotConnectionClientInfo.publishFormat = 1
                mqttotConnectionClientInfo.noAutomaticForeground = true
                mqttotConnectionClientInfo.makeUserAvailableInForeground = false
                mqttotConnectionClientInfo.isInitiallyForeground = false
                mqttotConnectionClientInfo.networkType = 1
                mqttotConnectionClientInfo.networkSubtype = 0
//                mqttotConnectionClientInfo.clientMqttSessionId =
//                    System.currentTimeMillis()
                mqttotConnectionClientInfo.subscribeTopics = intArrayOf(76, 80, 231)
                mqttotConnectionClientInfo.clientType = "device_auth"
                mqttotConnectionClientInfo.deviceId = fbns.deviceId
                mqttotConnectionClientInfo.deviceSecret = fbns.deviceSecret
                mqttotConnectionClientInfo.appId = InstagramConstants.APP_ID.toLong()
                mqttotConnectionClientInfo.anotherUnknown = -1
                mqttotConnectionClientInfo.clientStack = 3
                val mqtToTConnectionData =
                    MQTToTConnectionData()
                mqtToTConnectionData.clientInfo = mqttotConnectionClientInfo
                mqtToTConnectionData.password = fbns.password
//                mqtToTConnectionData.clientIdentifier = UUID.randomUUID().toString().substring(0,20)
                mqtToTConnectionData.clientIdentifier = fbns.deviceId.substring(0, 20)
                val fbnsConnectPacket = FbnsConnectPacket(
                    null,
                    null,
                    PayloadProcessor.buildPayload(mqtToTConnectionData)
                )
                val bossGroup = NioEventLoopGroup()
                val bootstrap = Bootstrap()

                bootstrap.group(bossGroup)
                    .channel(NioSocketChannel::class.java)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(object : ChannelInitializer<SocketChannel?>() {
                        override fun initChannel(ch: SocketChannel?) {
                            val pipeline: ChannelPipeline = ch!!.pipeline()
//                            pipeline.addLast("tls",SslHandler )
                            pipeline.addLast("ssl", SslHandler(sslContext.newEngine(ch.alloc())))
                            pipeline.addLast("encoder2", MqttEncoder.INSTANCE)
                            pipeline.addLast("encoder", FbnsPacketEncoder())
//                            pipeline.addLast("encoder",MqttEncoder.INSTANCE)
                            pipeline.addLast("decoder", FbnsPacketDecoder())
                            pipeline.addLast("decoder2", MqttDecoder())
                            pipeline.addLast("handler", NetworkHandler(this@FbnsService))
                        }
                    })
                // Bind and start to accept incoming connections.
                val future = bootstrap.connect(
                    SERVER_URL,
                    SERVER_PORT
                ).sync()
                mChannel = future.channel()

                mChannel!!.writeAndFlush(fbnsConnectPacket)
            } catch (e: Exception) {
                Log.i(InstagramConstants.DEBUG_TAG, "Error in FbnsService : ${e.message}")
            } finally {
            }
        }
    }

    fun startNotificationForeground(title: String, message: String) {
        try {
            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(applicationContext, getString(R.string.packageName))
                    .setContentText(message)
                    .setOngoing(true)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setChannelId(getString(R.string.packageName))
                    .build()
            } else {
                Notification.Builder(applicationContext)
                    .setOngoing(true)
                    .setContentText(message)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build()
            }
            startForeground(10, notification)
        } catch (e: Exception) {
        }
    }

    protected val mHandler = Handler()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                FbnsIntent.ACTION_CONNECT_SESSION -> {
                    if ((mChannel != null && mChannel!!.isActive) || !mUseCase.isUserLogged()) {
                        return START_STICKY
                    }
                    mChannel?.close()
                    ConnectivityThread().start()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mChannel?.close()
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun onDisconnect() {
        if (mUseCase.isUserLogged()) {
            run(
                applicationContext,
                FbnsIntent.ACTION_CONNECT_SESSION
            )
        }
    }

    fun onRegisterResponse(json: String) {
        val map = Gson().fromJson(json, HashMap::class.java)
        val token = map["token"].toString()
        val savedToken = mUseCase.getLastFbnsRegisterToken()
        val timeStamp = mUseCase.getLastFbnsTokenTimeStamp()
        if (savedToken == null || (System.currentTimeMillis() - timeStamp > 86400000)) {
            mUseCase.pushRegister(token)
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

        fun run(context: Context, action: String): Boolean {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(Intent(action).setPackage("com.idirect.app"))
                } else {
                    context.startService(Intent(action).setPackage("com.idirect.app"))
                }
            } catch (e: java.lang.Exception) {
                return false
            } finally {
                return true
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FbnsService::class.java))
        }
    }


}