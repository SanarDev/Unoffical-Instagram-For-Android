package com.sanardev.instagrammqtt.mqtt.network;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.sanardev.instagrammqtt.mqtt.service.NettyService;
import com.sanardev.instagrammqtt.mqtt.util.WakeLockWrapper;

import io.netty.channel.ChannelInboundHandlerAdapter;

public class NetworkEventHandler extends ChannelInboundHandlerAdapter {

	NettyService mService;

	public NetworkEventHandler(NettyService service) {
		mService = service;
	}

	@Override
	public void channelActive(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}
}