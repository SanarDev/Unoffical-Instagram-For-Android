package com.sanardev.instagrammqtt.realtime;

import com.sanardev.instagrammqtt.fbns.packethelper.MQTToTConnectionData;
import com.sanardev.instagrammqtt.fbns.packethelper.MQTTotConstants;
import com.sanardev.instagrammqtt.realtime.packethelper.ForegroundStateConfig;
import com.sanardev.instagrammqtt.utils.ZlibUtis;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TMemoryBuffer;

import java.io.IOException;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PayloadProcessor {


    private static TCompactProtocol thrift;
    private static TMemoryBuffer tMemoryInputTransport;

    public static ByteBuf buildPayload(MQTToTConnectionData mqtToTConnectionData) {
        try {
            tMemoryInputTransport = new TMemoryBuffer(500);
            thrift = new TCompactProtocol(tMemoryInputTransport);
            byte[] payloadData = toThrift(mqtToTConnectionData);

            return Unpooled.copiedBuffer(ZlibUtis.compress(payloadData));
        } catch (TException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ByteBuf buildForegroundStateThrift(ForegroundStateConfig foregroundStateConfig){
        try {
            tMemoryInputTransport = new TMemoryBuffer(500);
            thrift = new TCompactProtocol(tMemoryInputTransport);
            byte[] payloadData = toThrift(foregroundStateConfig);

            return Unpooled.copiedBuffer(ZlibUtis.compress(payloadData));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;

    }

    private static byte[] toThrift(ForegroundStateConfig foregroundStateConfig) throws TException {
        writeBoolean(MQTTotConstants.ForegroundStateConfig.IN_FOREGROUND_APP,foregroundStateConfig.inForegroundApp);
        writeBoolean(MQTTotConstants.ForegroundStateConfig.IN_FOREGROUND_DEVICE,foregroundStateConfig.inForegroundDevice);
        writeInt32(MQTTotConstants.ForegroundStateConfig.KEEP_ALIVE_TIME_OUT,foregroundStateConfig.keepAliveTimeOut);
        writeListBinary(MQTTotConstants.ForegroundStateConfig.SUBSCRIBE_TOPICS,foregroundStateConfig.subscribeTopics);
        writeListBinary(MQTTotConstants.ForegroundStateConfig.SUBSCRIBE_GENERIC_TOPICS,foregroundStateConfig.subscribeGenericTopics);
        writeListBinary(MQTTotConstants.ForegroundStateConfig.UNSUBSCRIBE_TOPICS,foregroundStateConfig.unsubscribeTopics);
        writeListBinary(MQTTotConstants.ForegroundStateConfig.UNSUBSCRIBE_GENERIC_TOPICS,foregroundStateConfig.unsubscribeGenericTopics);
        writeInt64(MQTTotConstants.ForegroundStateConfig.REQUEST_ID,foregroundStateConfig.requestId);
        writeFieldStop();
        return tMemoryInputTransport.getArray();
    }

    private static byte[] toThrift(MQTToTConnectionData mqtToTConnectionData) throws TException {
            writeString(MQTTotConstants.MQTToTConnectionData.CLIENT_IDENTIFIER, mqtToTConnectionData.clientIdentifier);

            //write struct client info
            writeStructBegin(MQTTotConstants.MQTToTConnectionData.CLIENT_INFO);
            writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.USER_ID, mqtToTConnectionData.clientInfo.userId);
            writeString(MQTTotConstants.MQTTotConnectionClientInfo.USER_AGENT, mqtToTConnectionData.clientInfo.userAgent);
            writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.CLIENT_CAPABILITIES, mqtToTConnectionData.clientInfo.clientCapabilities);
            writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.END_POINT_CAPABILITIES, mqtToTConnectionData.clientInfo.endpointCapabilities);
            writeInt32(MQTTotConstants.MQTTotConnectionClientInfo.PUBLISH_FORMAT, mqtToTConnectionData.clientInfo.publishFormat);
            writeBoolean(MQTTotConstants.MQTTotConnectionClientInfo.NO_AUTOMATIC_FOREGROUND, mqtToTConnectionData.clientInfo.noAutomaticForeground);
            writeBoolean(MQTTotConstants.MQTTotConnectionClientInfo.MAKE_USER_AVAILABLE_IN_FOREGROUND, mqtToTConnectionData.clientInfo.makeUserAvailableInForeground);
            writeString(MQTTotConstants.MQTTotConnectionClientInfo.DEVICE_ID, mqtToTConnectionData.clientInfo.deviceId);
            writeBoolean(MQTTotConstants.MQTTotConnectionClientInfo.IS_INITIALLY_FOREGROUND, mqtToTConnectionData.clientInfo.isInitiallyForeground);
            writeInt32(MQTTotConstants.MQTTotConnectionClientInfo.NETWORK_TYPE, mqtToTConnectionData.clientInfo.networkType);
            writeInt32(MQTTotConstants.MQTTotConnectionClientInfo.NETWORK_SUBTYPE, mqtToTConnectionData.clientInfo.networkSubtype);

            writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.CLIENT_MQTT_SESSION_ID, mqtToTConnectionData.clientInfo.clientMqttSessionId);
            writeListInt32(MQTTotConstants.MQTTotConnectionClientInfo.SUBSCRIBE_TOPICS, mqtToTConnectionData.clientInfo.subscribeTopics);
            writeString(MQTTotConstants.MQTTotConnectionClientInfo.CLIENT_TYPE, mqtToTConnectionData.clientInfo.clientType);
            writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.APP_ID, mqtToTConnectionData.clientInfo.appId);
            writeString(MQTTotConstants.MQTTotConnectionClientInfo.DEVICE_SECRET, mqtToTConnectionData.clientInfo.deviceSecret);
            writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.ANOTHER_UNKNOWN, mqtToTConnectionData.clientInfo.anotherUnknown);
            writByte(MQTTotConstants.MQTTotConnectionClientInfo.CLIENT_STACK, mqtToTConnectionData.clientInfo.clientStack);
            writeFieldStop();
            writeStructEnd();

            writeString(MQTTotConstants.MQTToTConnectionData.PASSWORD, mqtToTConnectionData.password);
            writeMap(MQTTotConstants.MQTToTConnectionData.APP_SPECIAL_INFO,mqtToTConnectionData.appSpecificInfo);

            writeFieldStop();
            return tMemoryInputTransport.getArray();

        }


    private static void writeString(short id, String str) throws TException {
        if (str == null) str = "";
        thrift.writeFieldBegin(new TField(null, TType.STRING, id));
        thrift.writeString(str);
        thrift.writeFieldEnd();
    }

    private static void writeStructBegin(short id) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.STRUCT, id));
        thrift.writeStructBegin(new TStruct());
    }

    private static void writeStructEnd() throws TException {
        thrift.writeStructEnd();
        thrift.writeFieldEnd();
    }

    private static void writeInt64(short id, long value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.I64, id));
        thrift.writeI64(value);
        thrift.writeFieldEnd();
    }

    private static void writeInt32(short id, int value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.I32, id));
        thrift.writeI32(value);
        thrift.writeFieldEnd();
    }

    private static void writByte(short id, byte value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.BYTE, id));
        thrift.writeByte(value);
        thrift.writeFieldEnd();
    }

    private static void writeBoolean(short id, boolean value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.BOOL, id));
        thrift.writeBool(value);
        thrift.writeFieldEnd();
    }

    private static void writeListInt32(short id, int[] value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.LIST, id));
        thrift.writeListBegin(new TList(TType.I32, value.length));
        for (int i = 0; i < value.length; i++) {
            thrift.writeI32(value[i]);
        }
        thrift.writeFieldEnd();
    }

    private static void writeListBinary(short id, String[] value) throws TException {
        if(value == null){
            value = new String[]{};
        }
        thrift.writeFieldBegin(new TField(null, TType.LIST, id));
        thrift.writeListBegin(new TList(TType.STRING, value.length));
        for (int i = 0; i < value.length; i++) {
            thrift.writeString(value[i]);
        }
        thrift.writeFieldEnd();
    }

    private static void writeMap(short id,Map<String,String> map) throws TException {
        thrift.writeFieldBegin(new TField(null,TType.MAP,id));
        thrift.writeMapBegin(new TMap(TType.STRING,TType.STRING,map.size()));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            thrift.writeString(entry.getKey());
            thrift.writeString(entry.getValue());
        }
        thrift.writeMapEnd();
        thrift.writeFieldEnd();
    }
    private static void writeFieldStop() throws TException {
        thrift.writeFieldStop();
    }

}
