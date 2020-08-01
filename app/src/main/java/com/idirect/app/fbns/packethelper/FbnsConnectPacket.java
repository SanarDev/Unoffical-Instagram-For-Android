package com.idirect.app.fbns.packethelper;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

public class FbnsConnectPacket extends MqttMessage
{
    public FbnsConnectPacket(MqttFixedHeader mqttFixedHeader) {
        super(mqttFixedHeader);
    }

    public FbnsConnectPacket(MqttFixedHeader mqttFixedHeader, Object variableHeader) {
        super(mqttFixedHeader, variableHeader);
    }

    public FbnsConnectPacket(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload) {
        super(new MqttFixedHeader(MqttMessageType.CONNECT,false, MqttQoS.AT_MOST_ONCE,false,0), variableHeader, payload);
    }

    public FbnsConnectPacket(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload, DecoderResult decoderResult) {
        super(mqttFixedHeader, variableHeader, payload, decoderResult);
    }

    public int getConnectFlags() {
        return 194;
    }

    public int getProtocolLevel() {
        return 3;
    }

    public int getKeepAliveInSeconds() {
        return 900;
    }

    @Override
    public Object payload() {
        return super.payload();
    }

    public String getProtocolName() {
        return "MQTToT";
    }

}
