package com.sanardev.instagrammqtt.mqtt.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.sanardev.instagrammqtt.constants.InstagramConstants;
import com.sanardev.instagrammqtt.mqtt.network.NetworkHandler;
import com.sanardev.instagrammqtt.mqtt.network.PayloadProcessor;
import com.sanardev.instagrammqtt.mqtt.packethelper.FbnsConnectPacket;
import com.sanardev.instagrammqtt.mqtt.packethelper.FbnsPacketEncoder;
import com.sanardev.instagrammqtt.mqtt.packethelper.MQTToTConnectionData;
import com.sanardev.instagrammqtt.mqtt.packethelper.MQTTotConnectionClientInfo;

import java.util.UUID;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ThreadPerChannelEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.sctp.SctpInboundByteStreamHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

public class NettyService extends Service {

    static final String SERVER_URL = "mqtt-mini.facebook.com";
    static final int SERVER_PORT = 443;
    private Channel channel;
    private NioEventLoopGroup group;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("com.sanardev.instagrammqtt.ACTION_CONNECT".equals(intent.getAction())) {
            if (channel == null) {
                connect();
            } else if (!channel.isActive()) {
                connect();
            } else {
                Log.i("TEST_APPLICATION", "Channel Already actived");
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void connect() {
        if (channel != null) return;
        if (group == null) {
            //NIO thread group
            group = new NioEventLoopGroup();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    MQTTotConnectionClientInfo mqttotConnectionClientInfo = new MQTTotConnectionClientInfo();
                    mqttotConnectionClientInfo.userId = 0;
                    mqttotConnectionClientInfo.userAgent = "Instagram " + InstagramConstants.APP_VERSION + " Android (29/10; 408dpi; 1080x2038; Xiaomi/xiaomi; Mi A2; jasmine_sprout; qcom; en_US; 200396019)";
                    mqttotConnectionClientInfo.clientCapabilities = 183;
                    mqttotConnectionClientInfo.endpointCapabilities = 128;
                    mqttotConnectionClientInfo.publishFormat = 1;
                    mqttotConnectionClientInfo.noAutomaticForeground = false;
                    mqttotConnectionClientInfo.deviceId = "";
                    mqttotConnectionClientInfo.isInitiallyForeground = false;
                    mqttotConnectionClientInfo.networkSubtype = 0;
                    mqttotConnectionClientInfo.clientMqttSessionId = System.currentTimeMillis();
                    mqttotConnectionClientInfo.subscribeTopics = new int[]{76, 80, 231};
                    mqttotConnectionClientInfo.clientType = "device_auth";
                    mqttotConnectionClientInfo.appId = Long.parseLong("567067343352427");
                    mqttotConnectionClientInfo.deviceSecret = "";
                    mqttotConnectionClientInfo.anotherUnknown = -1;
                    mqttotConnectionClientInfo.clientStack = -1;

                    MQTToTConnectionData mqtToTConnectionData = new MQTToTConnectionData();
                    mqtToTConnectionData.clientIdentifier = UUID.randomUUID().toString().substring(0, 20);
                    mqtToTConnectionData.clientInfo = mqttotConnectionClientInfo;
                    mqtToTConnectionData.password = "";

                    FbnsConnectPacket fbnsConnectPacket = new FbnsConnectPacket();
                    fbnsConnectPacket.payload = PayloadProcessor.buildPayload(mqtToTConnectionData);
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group)
                            .channel(NioSocketChannel.class)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.SO_KEEPALIVE, true)
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                            .handler(new ChannelInitializer<NioSocketChannel>() {
                                @Override
                                protected void initChannel(NioSocketChannel ch) throws Exception {
                                    //End with line breaks
                                    ChannelPipeline pipeline = ch.pipeline();
                                    pipeline.addLast(new NetworkHandler());
                                    pipeline.addLast("encoder", new FbnsPacketEncoder());
                                    pipeline.addLast("decoder", new StringDecoder());

                                }
                            });
                    //Initiate an asynchronous connection operation
                    ChannelFuture channelFuture = null;
                    channelFuture = bootstrap.connect(SERVER_URL, SERVER_PORT).sync();
                    channel = channelFuture.channel();
                    //Waiting for server listening port to close
                    channel.writeAndFlush(fbnsConnectPacket);
                    channel.flush();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void disConnect() {
        if (null != group) {
            group.shutdownGracefully();
        }
        group = null;
        channel = null;
    }
}
