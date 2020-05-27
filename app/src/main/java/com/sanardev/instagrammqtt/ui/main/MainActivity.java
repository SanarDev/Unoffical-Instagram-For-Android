package com.sanardev.instagrammqtt.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sanardev.instagrammqtt.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static String HOSTNAME = "edge-mqtt.facebook.com:443";
    private static String USER_AGENT = "[FBAN/MQTT;FBAV/130.0.0.31.121;FBBV/200396014;FBDM/{density=3.0,width=1080,height=1920};FBLC/en_GB;FBCR/;FBMF/Xiaomi;FBBD/Xiaomi;FBPN/com.instagram.android;FBDV/Mi 8;FBSV/10;FBLR/0;FBBK/1;FBCA/arm64-v8a:;]";

    private static int PORT = 443;
    private static int MAX_EXECUTION_TIME = 8;
    private String broker;
    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            connect("ssl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect(String protogle) throws Exception {

        this.broker = protogle + "://" + HOSTNAME;
        this.mqttClient = new MqttClient(broker, "567310203415052", new MemoryPersistence());

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setKeepAliveInterval(100);
        connOpts.setCleanSession(true);
        Log.i("TEST_APP_1","Connecting to broker: " + broker);
        Log.i("TEST_APP_1","isConnected:" + mqttClient.isConnected());
        try {
            IMqttToken cn = mqttClient.connectWithResult(connOpts);
            Log.i("TEST_APP_1","connected");
        } catch (MqttException me) {
            Log.i("TEST_APP_1","reason " + me.getReasonCode());
            Log.i("TEST_APP_1","msg " + me.getMessage());
            Log.i("TEST_APP_1","loc " + me.getLocalizedMessage());
            Log.i("TEST_APP_1","cause " + me.getCause());
            Log.i("TEST_APP_1","excep " + me);
            return;
        }


        this.mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable me) {
                Log.i("TEST_APP_1","Connection lost");
                Log.i("TEST_APP_1","msg " + me.getMessage());
                Log.i("TEST_APP_1","loc " + me.getLocalizedMessage());
                Log.i("TEST_APP_1","cause " + me.getCause());
                Log.i("TEST_APP_1","excep " + me);
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Log.i("TEST_APP_1","message Arrived");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.i("TEST_APP_1","deliverd--------");
                try {
                    MqttDeliveryToken token = (MqttDeliveryToken) iMqttDeliveryToken;
                    String h = token.getMessage().toString();
                    Log.i("TEST_APP_1","deliverd message :" + h);
                } catch (MqttException me) {
                    Log.i("TEST_APP_1","reason " + me.getReasonCode());
                    Log.i("TEST_APP_1","msg " + me.getMessage());
                    Log.i("TEST_APP_1","loc " + me.getLocalizedMessage());
                    Log.i("TEST_APP_1","cause " + me.getCause());
                    Log.i("TEST_APP_1","excep " + me);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}