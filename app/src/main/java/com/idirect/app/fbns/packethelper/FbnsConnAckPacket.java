package com.idirect.app.fbns.packethelper;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

public class FbnsConnAckPacket extends MqttMessage {
    public FbnsConnAckPacket(MqttFixedHeader mqttFixedHeader) {
        super(mqttFixedHeader);
    }

    public FbnsConnAckPacket(MqttFixedHeader mqttFixedHeader, Object variableHeader) {
        super(mqttFixedHeader, variableHeader);
    }

    public FbnsConnAckPacket(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload) {
        super(new MqttFixedHeader(MqttMessageType.CONNACK,false, MqttQoS.AT_MOST_ONCE,false,0), variableHeader, payload);
    }

    public FbnsConnAckPacket(MqttFixedHeader mqttFixedHeader, Object variableHeader, Object payload, DecoderResult decoderResult) {
        super(mqttFixedHeader, variableHeader, payload, decoderResult);
    }

}
