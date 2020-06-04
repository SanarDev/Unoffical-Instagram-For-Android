package com.sanardev.instagrammqtt.mqtt.network;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.sanardev.instagrammqtt.mqtt.packethelper.MQTToTConnectionData;
import com.sanardev.instagrammqtt.mqtt.packethelper.MQTTotConstants;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

            byte[] input = "asdf".getBytes();

            Deflater compressor = new Deflater();
            compressor.setLevel(Deflater.BEST_COMPRESSION);

            compressor.setInput(input);
            compressor.finish();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
            bos.close();
            byte[] compressedData = bos.toByteArray();
            return Unpooled.copiedBuffer(compressedData);
        } catch (TException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
            try {
                zipOutputStream.write(payloadData);
            } finally {
                zipOutputStream.close();
            }
            byte[] data = byteArrayOutputStream.toByteArray();

     */

    private static byte[] toThrift(MQTToTConnectionData mqtToTConnectionData) throws TException {
        writeString(MQTTotConstants.MQTToTConnectionData.CLIENT_IDENTIFIER, mqtToTConnectionData.clientIdentifier);

        //write struct client info
        writeStructBegin(MQTTotConstants.MQTToTConnectionData.CLIENT_INFO);
        writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.USER_ID, mqtToTConnectionData.clientInfo.userId);
        writeString(MQTTotConstants.MQTTotConnectionClientInfo.USER_AGENT, mqtToTConnectionData.clientInfo.userAgent);
        writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.CLIENT_CAPABILITIES, mqtToTConnectionData.clientInfo.clientCapabilities);
        writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.END_POINT_CAPABILITIES, mqtToTConnectionData.clientInfo.endpointCapabilities);
        writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.PUBLISH_FORMAT, mqtToTConnectionData.clientInfo.publishFormat);
        writeBoolean(MQTTotConstants.MQTTotConnectionClientInfo.NO_AUTOMATIC_FOREGROUND, mqtToTConnectionData.clientInfo.noAutomaticForeground);
        writeBoolean(MQTTotConstants.MQTTotConnectionClientInfo.MAKE_USER_AVAILABLE_IN_FOREGROUND, mqtToTConnectionData.clientInfo.makeUserAvailableInForeground);
        writeString(MQTTotConstants.MQTTotConnectionClientInfo.DEVICE_ID, mqtToTConnectionData.clientInfo.deviceId);
        writeBoolean(MQTTotConstants.MQTTotConnectionClientInfo.IS_INITIALLY_FOREGROUND, mqtToTConnectionData.clientInfo.isInitiallyForeground);
        writeInt32(MQTTotConstants.MQTTotConnectionClientInfo.NETWORK_TYPE, mqtToTConnectionData.clientInfo.networkType);
        writeInt32(MQTTotConstants.MQTTotConnectionClientInfo.NETWORK_SUBTYPE, mqtToTConnectionData.clientInfo.networkSubtype);
        writeInt32(MQTTotConstants.MQTTotConnectionClientInfo.NETWORK_SUBTYPE, mqtToTConnectionData.clientInfo.networkSubtype);
        writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.CLIENT_MQTT_SESSION_ID, mqtToTConnectionData.clientInfo.clientMqttSessionId);
        writeListInt32(MQTTotConstants.MQTTotConnectionClientInfo.SUBSCRIBE_TOPICS, mqtToTConnectionData.clientInfo.subscribeTopics);
        writeString(MQTTotConstants.MQTTotConnectionClientInfo.CLIENT_TYPE, mqtToTConnectionData.clientInfo.clientType);
        writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.APP_ID, mqtToTConnectionData.clientInfo.appId);
        writeString(MQTTotConstants.MQTTotConnectionClientInfo.DEVICE_SECRET, mqtToTConnectionData.clientInfo.deviceSecret);
        writeInt64(MQTTotConstants.MQTTotConnectionClientInfo.ANOTHER_UNKNOWN, mqtToTConnectionData.clientInfo.anotherUnknown);
        writByte(MQTTotConstants.MQTTotConnectionClientInfo.CLIENT_STACK, mqtToTConnectionData.clientInfo.clientStack);

        writeString(MQTTotConstants.MQTToTConnectionData.PASSWORD, mqtToTConnectionData.password);
        writeFieldStop();
        return tMemoryInputTransport.getArray();
    }

    private static void writeString(short id, String str) throws TException {
        if (str == null) str = "";
        thrift.writeFieldBegin(new TField(null, TType.STRING, id));
        thrift.writeString(str);
    }

    private static void writeStructBegin(short id) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.STRUCT, id));
    }

    private static void writeStructEnd() throws TException {
        thrift.writeStructEnd();
    }

    private static void writeInt64(short id, long value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.I64, id));
        thrift.writeI64(value);
    }

    private static void writeInt32(short id, int value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.I32, id));
        thrift.writeI32(value);
    }

    private static void writByte(short id, byte value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.BYTE, id));
        thrift.writeByte(value);
    }

    private static void writeBoolean(short id, boolean value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.BOOL, id));
        thrift.writeBool(value);
    }

    private static void writeListInt32(short id, int[] value) throws TException {
        thrift.writeFieldBegin(new TField(null, TType.LIST, id));
        thrift.writeListBegin(new TList(TType.I32, value.length));
        for (int i = 0; i < value.length; i++) {
            thrift.writeI32(value[i]);
        }
    }

    private static void writeFieldStop() throws TException {
        thrift.writeFieldStop();
    }


}
