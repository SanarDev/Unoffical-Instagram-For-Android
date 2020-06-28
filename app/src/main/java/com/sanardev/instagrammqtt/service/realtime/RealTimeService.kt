package com.sanardev.instagrammqtt.service.realtime

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.fbns.network.PayloadProcessor
import com.sanardev.instagrammqtt.fbns.packethelper.*
import com.sanardev.instagrammqtt.realtime.network.NetworkHandler
import com.sanardev.instagrammqtt.realtime.subcribers.GraphQLSubscriptions
import com.sanardev.instagrammqtt.service.fbns.FbnsService
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.DisplayUtils
import com.sanardev.instagrammqtt.utils.ZlibUtis
import dagger.android.AndroidInjection
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
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
import io.netty.util.CharsetUtil
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RealTimeService : Service() {

    private lateinit var sslContext: SslContext
    var mChannel: Channel? = null

    @Inject
    lateinit var mUseCase: UseCase

    private var seqID: Long = 0
    private var snapShotAt: Long = 0


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()

        sslContext =
            SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                .sslProvider(
                    SslProvider.JDK
                ).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        connect(intent!!.extras!!.getLong("seq_id"), intent!!.extras!!.getLong("snap_shot_at"))

        return super.onStartCommand(intent, flags, startId)
    }

    private fun connect(seqID: Long, snapShotAt: Long) {
        try {
            this.seqID = seqID
            this.snapShotAt = snapShotAt

            val cookie = mUseCase.getCookie()
            val user = mUseCase.getUserData()

            val mqttotConnectionClientInfo =
                MQTTotConnectionClientInfo()
            mqttotConnectionClientInfo.userId = user!!.pk!!
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
            mqttotConnectionClientInfo.clientMqttSessionId =
                System.currentTimeMillis()
            mqttotConnectionClientInfo.subscribeTopics = intArrayOf(88, 135, 149, 150, 133, 146)
            mqttotConnectionClientInfo.clientType = "cookie_auth"
            mqttotConnectionClientInfo.deviceId = cookie.phoneID
            mqttotConnectionClientInfo.deviceSecret = ""
            mqttotConnectionClientInfo.appId = InstagramConstants.APP_ID.toLong()
            mqttotConnectionClientInfo.anotherUnknown = -1
            mqttotConnectionClientInfo.clientStack = 3
            val mqtToTConnectionData =
                MQTToTConnectionData()
            mqtToTConnectionData.clientInfo = mqttotConnectionClientInfo
            mqtToTConnectionData.password = "sessionid=${cookie.sessionID}"
//            mqtToTConnectionData.password = "sessionid=${UUID.randomUUID().toString()}"
            mqtToTConnectionData.clientIdentifier = cookie.phoneID.substring(0, 20)
            val appSpecificInfo = HashMap<String, String>().apply {
                put("app_version", InstagramConstants.APP_VERSION)
                put("X-IG-Capabilities", InstagramConstants.DEVICE_CAPABILITIES)
                put(
                    "everclear_subscriptions", "{" +
                            "\"inapp_notification_subscribe_comment\":\"17899377895239777\"," +
                            "\"inapp_notification_subscribe_comment_mention_and_reply\":\"17899377895239777\"," +
                            "\"video_call_participant_state_delivery\":\"17977239895057311\"," +
                            "\"presence_subscribe\":\"17846944882223835\"" +
                            '}'
                )
                put("User-Agent", "[FBAN/MQTT;FBAV/${InstagramConstants.APP_VERSION};FBBV/${InstagramConstants.APP_ID};FBDM/{density=4.0,width=${DisplayUtils.getScreenWidth()},height=${DisplayUtils.getScreenHeight()}};FBLC/en_US;FBCR/${""};FBMF/LGE;FBBD/lge;FBPN/com.instagram.android;FBDV/RS988;FBSV/6.0.1;FBLR/0;FBBK/1;FBCA/armeabi-v7a:armeabi;]")
                put("Accept-Language", "en-US")
                put("platform", "android")
                put("ig_mqtt_route", "django")
                put("pubsub_msg_type_blacklist", "direct, typing_type")
                put("auth_cache_enabled", "0")
            }
            mqtToTConnectionData.appSpecificInfo = appSpecificInfo

            mqtToTConnectionData.clientIdentifier = (UUID.randomUUID().toString().substring(0, 20))
            val fbnsConnectPacket =
                FbnsConnectPacket(null, null, PayloadProcessor.buildPayload(mqtToTConnectionData))

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
                        pipeline.addLast("decoder2", MqttDecoder())
                        pipeline.addLast("handler", NetworkHandler(this@RealTimeService))
                    }
                })
            // Bind and start to accept incoming connections.
            val future = bootstrap.connect(
                RealTimeService.SERVER_URL,
                RealTimeService.SERVER_PORT
            ).sync()
            mChannel = future.channel()

            mChannel!!.writeAndFlush(fbnsConnectPacket)
        } finally {
        }
    }

    fun onConnAck() {
        val user = mUseCase.getUserData()
        val cookie = mUseCase.getCookie()
        val list = ArrayList<Pair<MqttQoS, String>>().apply {
            add(Pair(MqttQoS.AT_MOST_ONCE, InstagramConstants.RealTimeTopics.MESSAGE_SYNC.path))
            add(
                Pair(
                    MqttQoS.AT_MOST_ONCE,
                    InstagramConstants.RealTimeTopics.SEND_MESSAGE_RESPONSE.path
                )
            )
            add(
                Pair(
                    MqttQoS.AT_MOST_ONCE,
                    InstagramConstants.RealTimeTopics.IRIS_SUB_RESPONSE.path
                )
            )
        }
        sendSubscribe(list)

        updateSubscriptions(
            InstagramConstants.RealTimeTopics.REALTIME_SUB.id.toString(),
            HashMap<String, Any>().apply {
                put(
                    "sub",
                    arrayOf(
                        GraphQLSubscriptions.getAppPresenceSubscription(),
                        GraphQLSubscriptions.getZeroProvisionSubscription(cookie.phoneID),
                        GraphQLSubscriptions.getDirectStatusSubscription(),
                        GraphQLSubscriptions.getDirectTypingSubscription(user!!.pk!!.toString()),
                        GraphQLSubscriptions.getAsyncAdSubscription(user!!.pk!!.toString())
                    )
                )
            }
        )

        updateSubscriptions(
            InstagramConstants.RealTimeTopics.IRIS_SUB.id.toString(),
            HashMap<String,Any>().apply {
                put("seq_id",seqID.toString())
                put("sub", emptyArray<String>())
                put("snapshot_at_ms",snapShotAt.toString())
            }
        )
    }

    fun updateSubscriptions(topicId: String, data: HashMap<String, Any>) {
        val payload = ZlibUtis.compress(Gson().toJson(data).toByteArray(CharsetUtil.UTF_8))
        mChannel!!.writeAndFlush(MqttPublishMessage(
            MqttFixedHeader(
                MqttMessageType.PUBLISH,
                false,
                MqttQoS.AT_LEAST_ONCE,
                false,
                payload.size
            ),
            MqttPublishVariableHeader(topicId, generatePacketID()),
            Unpooled.copiedBuffer(payload)
        ))
    }

    public fun sendSubscribe(list: List<Pair<MqttQoS, String>>): Int {
        val packetID = generatePacketID()
        val subscribePacketBuilder = MqttMessageBuilders.subscribe()
        for (item in list) {
            subscribePacketBuilder.addSubscription(
                item.first,
                item.second
            )
        }
        subscribePacketBuilder.messageId(packetID)
        subscribePacketBuilder.build()
        mChannel!!.writeAndFlush(subscribePacketBuilder)
        return packetID
    }


    private fun generatePacketID(): Int {
        val packetID = Random().nextInt(65000)
        Log.i(InstagramConstants.DEBUG_TAG,"Generate Packet $packetID")
        return packetID
    }

    companion object {
        var isConnectAlreadyScheduled = false
        const val SERVER_URL = "edge-mqtt.facebook.com"
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