package com.sanardev.instagrammqtt.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

class MqttService extends Service implements MqttCallback, MqttTraceHandler
{
    private static final String USER = "";
    private static final String PASSWORD = "";
    private static final String SERVER_URL_WITH_SSL = "ssl://mqtt-mini.facebook.com:443";

    protected MqttAndroidClient mqttAndroidClient;

    @Override
    public void onCreate()
    {
        super.onCreate();

        this.mqttAndroidClient = this.createMqttClient(SERVER_URL_WITH_SSL);

        SSLSocketFactory sslSocketFactory = this.createSSLSocketFactory();

        this.connectToMqttBroker(this.mqttAndroidClient, sslSocketFactory);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public IMqttDeliveryToken publish(String topic, MqttMessage mqttMessage, Object context)
    {
        try
        {
            return this.mqttAndroidClient.publish(topic, mqttMessage, context, null);
        }
        catch (MqttException e)
        {
            Log.i("TEST_APPLICATION","An error occurred during publishing an MqttMessage to topic: " + topic, e);
            return null;
        }
    }

    protected void subscribeToTopic(String topic)
    {
        //            this.mqttAndroidClient.subscribe(topic, 1, null, new LoggingMqttListener());
    }

    protected void subscribeToTopic(String topic, boolean reconnect)
    {
        if (reconnect) subscribeToTopic(topic);
    }

    private void connectToMqttBroker(MqttAndroidClient mqttClient, SSLSocketFactory sslSocketFactory)
    {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(USER);
        mqttConnectOptions.setPassword(PASSWORD.toCharArray());
        mqttConnectOptions.setSocketFactory(sslSocketFactory);

        try
        {
            mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

//                    subscribeToTopic(topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    Log.i("TEST_APPLICATION","Failed to connectToMqttBroker to: " + SERVER_URL_WITH_SSL);
                }
            });
        }
        catch (MqttException ex)
        {
            Log.i("TEST_APPLICATION","Exception while connecting to mqtt broker", ex);
        }
    }

    private MqttAndroidClient createMqttClient(String serverUrl)
    {
        MqttAndroidClient mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUrl, "Behzzd-dn8Ty0");

        mqttAndroidClient.setCallback(this);
        mqttAndroidClient.setTraceEnabled(true);
        mqttAndroidClient.setTraceCallback(this);

        return mqttAndroidClient;
    }

    private SSLSocketFactory createSSLSocketFactory()
    {
        try
        {
            CertificateFactory caCF = CertificateFactory.getInstance("X.509");
            X509Certificate ca = (X509Certificate) caCF.generateCertificate(this.getAssets().open("ca.crt"));

            KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            caKeyStore.load(null, null);
            caKeyStore.setCertificateEntry(ca.getSubjectX500Principal().getName(), ca);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(caKeyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context.getSocketFactory();
        }
        catch (IOException | KeyStoreException | KeyManagementException | NoSuchAlgorithmException | CertificateException e)
        {
            Log.i("TEST_APPLICATION","Creating ssl socket factory failed");
            return null;
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.i("TEST_APPLICATION","Connection lost");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i("TEST_APPLICATION","message Arrived");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i("TEST_APPLICATION","delivery compelete");
    }

    @Override
    public void traceDebug(String tag, String message) {
        Log.i("TEST_APPLICATION",tag + " | "+message);
    }

    @Override
    public void traceError(String tag, String message) {
        Log.i("TEST_APPLICATION",tag + " || "+message);
    }

    @Override
    public void traceException(String tag, String message, Exception e) {
        Log.i("TEST_APPLICATION",tag + " ||| "+message);
    }
}