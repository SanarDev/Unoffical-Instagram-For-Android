package com.sanardev.instagrammqtt.service.realtime

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.sanardev.instagrammqtt.constants.InstagramConstants
import com.sanardev.instagrammqtt.datasource.model.Message
import com.sanardev.instagrammqtt.datasource.model.ParsedMessage
import com.sanardev.instagrammqtt.datasource.model.Seen
import com.sanardev.instagrammqtt.datasource.model.event.*
import com.sanardev.instagrammqtt.datasource.model.realtime.*
import com.sanardev.instagrammqtt.fbns.packethelper.FbnsConnectPacket
import com.sanardev.instagrammqtt.fbns.packethelper.FbnsPacketEncoder
import com.sanardev.instagrammqtt.fbns.packethelper.MQTToTConnectionData
import com.sanardev.instagrammqtt.fbns.packethelper.MQTTotConnectionClientInfo
import com.sanardev.instagrammqtt.realtime.PayloadProcessor
import com.sanardev.instagrammqtt.realtime.commands.*
import com.sanardev.instagrammqtt.realtime.network.NetworkHandler
import com.sanardev.instagrammqtt.realtime.packethelper.ForegroundStateConfig
import com.sanardev.instagrammqtt.realtime.subcribers.GraphQLSubscriptions
import com.sanardev.instagrammqtt.realtime.subcribers.SkywalkerSubscriptions
import com.sanardev.instagrammqtt.usecase.UseCase
import com.sanardev.instagrammqtt.utils.DisplayUtils
import com.sanardev.instagrammqtt.utils.NetworkUtils
import com.sanardev.instagrammqtt.utils.ZlibUtis
import dagger.android.AndroidInjection
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
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
import org.greenrobot.eventbus.EventBus
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RealTimeService : Service() {

    private lateinit var sslContext: SslContext
    var mChannel: Channel? = null

    @Inject
    lateinit var mUseCase: UseCase

    @Inject
    lateinit var mGson: Gson

    private var seqID: Long = 0
    private var snapShotAt: Long = 0
    private var directCommands: DirectCommands? = null
    private var newMessageList = ArrayList<MessageEvent>().toMutableList()


    override fun onBind(intent: Intent?): IBinder? {
        return null
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

        fun run(context: Context, value: RealTimeCommand): Boolean {
            try {
                context.startService(
                    Intent(value.action).setPackage("com.sanardev.instagrammqtt")
                        .putExtra("data", value as Parcelable)
                );
            } catch (e: Exception) {
                return false
            } finally {
                return true
            }
        }
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
        if (intent == null || intent.extras == null) {
            return super.onStartCommand(intent, flags, startId)
        }
        if(intent.action != RealTimeIntent.ACTION_CONNECT_SESSION){
            if(mChannel == null || directCommands == null){
                return super.onStartCommand(intent, flags, startId)
            }
        }

        when (intent.action) {
            RealTimeIntent.ACTION_CONNECT_SESSION -> {
                if (mChannel != null && mChannel!!.isActive) {
                    return super.onStartCommand(intent, flags, startId)
                }
                if(NetworkUtils.getConnectionType(applicationContext) == NetworkUtils.NetworkType.NONE){
                    return super.onStartCommand(intent, flags, startId)
                }
                EventBus.getDefault().postSticky(ConnectionStateEvent(ConnectionStateEvent.State.CONNECTING))
                val data = intent.extras!!.getParcelable<RealTime_StartService>("data")!!
                connect(data.seqId,data.snapShotAt)
            }
            RealTimeIntent.ACTION_DISCONNECT_SESSION ->{
                if (mChannel != null){
                    mChannel!!.close()
                    stopSelf()
                }
            }
            RealTimeIntent.ACTION_SEND_TEXT_MESSAGE -> {
                val data = intent.extras!!.getParcelable<RealTime_SendMessage>("data")!!
                directCommands!!.sendText(data.text,data.clientContext!!,data.threadId)
            }

            RealTimeIntent.ACTION_MARK_AS_SEEN ->{
                val data = intent.extras!!.getParcelable<RealTime_MarkAsSeen>("data")!!
                directCommands!!.markAsSeen(data.threadId,data.itemId)
            }

            RealTimeIntent.ACTION_SEND_MEDIA -> {
                val data = intent.extras!!.getParcelable<RealTime_SendMedia>("data")!!
                directCommands!!.sendMedia(data.text,data.mediaId,data.threadId,data.clientContext)
            }

            RealTimeIntent.ACTION_SEND_LOCATION -> {
                val data = intent.extras!!.getParcelable<RealTime_SendLocation>("data")!!
                directCommands!!.sendLocation(data.text,data.locationId,data.threadId,data.clientContext)
            }

            RealTimeIntent.ACTION_SEND_REACTION -> {
                val data = intent.extras!!.getParcelable<RealTime_SendReaction>("data")!!
                directCommands!!.sendReaction(data.itemId,data.reactionType,data.clientContext,data.threadId,data.reactionStatus)
            }

            RealTimeIntent.ACTION_SEND_TYPING_STATE -> {
                val data = intent.extras!!.getParcelable<RealTime_SendTypingState>("data")!!
                directCommands!!.indicateActivity(data.threadId,data.isActive,data.clientContext)
            }

            RealTimeIntent.ACTION_SEND_USER_STORY ->{
                val data = intent.extras!!.getParcelable<RealTime_SendUserStory>("data")!!
                directCommands!!.sendUserStory(data.text,data.storyId,data.threadId,data.clientContext)
            }

            RealTimeIntent.ACTION_SEND_LIKE ->{
                val data = intent.extras!!.getParcelable<RealTime_SendLike>("data")!!
                directCommands!!.sendLike(data.threadId,data.clientContext)
            }

            RealTimeIntent.ACTION_SEND_PROFILE -> {
                val data = intent.extras!!.getParcelable<RealTime_SendProfile>("data")!!
                directCommands!!.sendProfile(data.text,data.userId,data.threadId,data.clientContext)
            }

            RealTimeIntent.ACTION_SEND_HASH_TAG -> {
                val data =intent.extras!!.getParcelable<RealTime_SendHashTag>("data")!!
                directCommands!!.sendHashtag(data.text,data.threadId,data.hashTag,data.clientContext)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun sendForegroundState(
        inForegroundApp: Boolean,
        inForegroundDevice: Boolean,
        keepAliveTimeout: Int
    ) {
        val topicName = InstagramConstants.RealTimeTopics.FOREGROUND_STATE.id.toString()
        val packetID = Random().nextInt(65535)
        val payload = PayloadProcessor.buildForegroundStateThrift(ForegroundStateConfig().apply {
            this.inForegroundApp = inForegroundApp
            this.inForegroundDevice = inForegroundDevice
            this.keepAliveTimeOut = keepAliveTimeOut
        })
        val mqttPublishMessage = MqttPublishMessage(
            MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, false, 0),
            MqttPublishVariableHeader(topicName, packetID),
            payload
        )
        Log.i(InstagramConstants.DEBUG_TAG, "RealTime Update foregroundState $inForegroundApp with id $packetID")
        mChannel?.writeAndFlush(mqttPublishMessage)
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
            mqttotConnectionClientInfo.userAgent = "Instagram ${InstagramConstants.APP_VERSION} Android (29/10; 408dpi; ${DisplayUtils.getScreenWidth()}x${DisplayUtils.getScreenHeight()}; Xiaomi/xiaomi; Mi A2; jasmine_sprout; qcom; en_US; 200396019)"
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
//                            "\"async_ads_subscribe\":${GraphQLSubscriptions.QueryIDs.asyncAdSub}"+
//                            "\"inapp_notification_subscribe_default\":\"17899377895239777\"," +
                            "\"inapp_notification_subscribe_comment\":\"17899377895239777\"," +
                            "\"inapp_notification_subscribe_comment_mention_and_reply\":\"17899377895239777\"," +
                            "\"video_call_participant_state_delivery\":\"17977239895057311\"," +
//                            "\"business_import_page_media_delivery_subscribe\":\"17940467278199720\""+
                            "\"presence_subscribe\":\"17846944882223835\"" +
                            '}'
                )
                put(
                    "User-Agent",
                    "Instagram ${InstagramConstants.APP_VERSION} Android (29/10; 408dpi; ${DisplayUtils.getScreenWidth()}x${DisplayUtils.getScreenHeight()}; Xiaomi/xiaomi; Mi A2; jasmine_sprout; qcom; en_US; 200396019)"
                )
                put("Accept-Language", "en-US")
                put("platform", "android")
                put("ig_mqtt_route", "django")
//                put("pubsub_msg_type_blacklist", "direct, typing_type")
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
            directCommands = DirectCommands(mChannel!!, Gson())
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
            InstagramConstants.RealTimeTopics.PUBSUB.id.toString(),
            HashMap<String, Any>().apply {
                put(
                    "sub",
                    arrayOf(
                        SkywalkerSubscriptions.directSub(user!!.pk.toString()),
                        SkywalkerSubscriptions.liveSub(user!!.pk.toString())
                    )
                )
            }
        )
        updateSubscriptions(
            InstagramConstants.RealTimeTopics.IRIS_SUB.id.toString(),
            HashMap<String, Any>().apply {
                put("seq_id", seqID.toString())
                put("sub", emptyArray<String>())
                put("snapshot_at_ms", snapShotAt.toString())
            }
        )

//        directCommands!!.sendText(text = "Salam",threadId = "340282366841710300949128267726276550694")
//        directCommands!!.sendLike(threadId = "340282366841710300949128267726276550694")
    }

    fun updateSubscriptions(topicId: String, data: HashMap<String, Any>) {
        val payload = ZlibUtis.compress(Gson().toJson(data).toByteArray(CharsetUtil.UTF_8))
        mChannel!!.writeAndFlush(
            MqttPublishMessage(
                MqttFixedHeader(
                    MqttMessageType.PUBLISH,
                    false,
                    MqttQoS.AT_LEAST_ONCE,
                    false,
                    payload.size
                ),
                MqttPublishVariableHeader(topicId, generatePacketID()),
                Unpooled.copiedBuffer(payload)
            )
        )
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
        Log.i(InstagramConstants.DEBUG_TAG, "Generate Packet $packetID")
        return packetID
    }

    fun onMessageEvent(parseData: ParsedMessage) {
        val map = mGson.fromJson(parseData.payload, HashMap::class.java)
        val data = map["data"]
        val realtimeSubDirectDataWrapper = jacksonObjectMapper().convertValue(
            (data as ArrayList<LinkedTreeMap<String, String>>).get(0),
            RealtimeSubDirectDataWrapper::class.java
        )
        if (realtimeSubDirectDataWrapper.path.startsWith("/direct_v2/threads/")) {
            val param = realtimeSubDirectDataWrapper.path.split("/")
//            val r = Random().nextInt(5)
//            val threadId = when(r){
//                0 -> "340282366841710300949128307061304472486"
//                1 -> "340282366841710300949128150005151891234"
//                2 -> "340282366841710300949128416459859281266"
//                3 -> "340282366841710300949128222260910610954"
//                4 -> "340282366841710300949128140613727009858"
//                else -> "340282366841710300949128150005151891234"
//            }//param[3]
            val threadId = param[3]
            val event = param[4]
            when (event) {
                InstagramConstants.RealTimeEvent.NEW_MESSAGE.id -> {
                    val msg = MessageEvent(
                        threadId,mGson.fromJson(
                        realtimeSubDirectDataWrapper.value,
                        Message::class.java
                    ))
                    newMessageList.add(msg)
                    EventBus.getDefault().postSticky(newMessageList)
                }

                InstagramConstants.RealTimeEvent.ACTIVITY_INDICATOR_ID.id -> {
                    EventBus.getDefault().post(
                        TypingEvent(
                            threadId
                        )
                    )
                }

                InstagramConstants.RealTimeEvent.PARTICIPANTS.id -> {
                    EventBus.getDefault().postSticky(
                        UpdateSeenEvent(
                            threadId, mGson.fromJson(
                                realtimeSubDirectDataWrapper.value,
                                Seen::class.java
                            )
                        )
                    )
                }
            }
            Log.i("TEST", "TEST")
        }
    }

    fun onActivityEvent(parseData: ParsedMessage) {
        when (parseData.topicName) {
            GraphQLSubscriptions.QueryIDs.appPresence -> {
                val map = mGson.fromJson(parseData.payload, HashMap::class.java)
                val event = jacksonObjectMapper().convertValue(
                    map["presence_event"],
                    PresenceEvent::class.java
                )
                EventBus.getDefault().post(event)
            }
        }
    }

    fun onSendMessageResponse(json: String) {
        val messageResponseEvent = mGson.fromJson(json, MessageResponse::class.java)
        EventBus.getDefault().post(messageResponseEvent)
    }

}