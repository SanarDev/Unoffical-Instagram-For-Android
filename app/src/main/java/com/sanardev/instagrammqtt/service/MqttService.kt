package com.sanardev.instagrammqtt.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.android.service.MqttTraceHandler
import org.eclipse.paho.client.mqttv3.*
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

internal class MqttService : Service(), MqttCallback, MqttTraceHandler {
    protected var mqttAndroidClient: MqttAndroidClient? = null
    override fun onCreate() {
        super.onCreate()
        mqttAndroidClient =
            createMqttClient(SERVER_URL_WITH_SSL)
        val sslSocketFactory = createSSLSocketFactory()
        connectToMqttBroker(mqttAndroidClient, sslSocketFactory)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun publish(
        topic: String,
        mqttMessage: MqttMessage?,
        context: Any?
    ): IMqttDeliveryToken? {
        return try {
            mqttAndroidClient!!.publish(topic, mqttMessage, context, null)
        } catch (e: MqttException) {
            Log.i(
                "TEST_APPLICATION",
                "An error occurred during publishing an MqttMessage to topic: $topic",
                e
            )
            null
        }
    }

    protected fun subscribeToTopic(topic: String?) {
        //            this.mqttAndroidClient.subscribe(topic, 1, null, new LoggingMqttListener());
    }

    protected fun subscribeToTopic(topic: String?, reconnect: Boolean) {
        if (reconnect) subscribeToTopic(topic)
    }

    private fun connectToMqttBroker(
        mqttClient: MqttAndroidClient?,
        sslSocketFactory: SSLSocketFactory?
    ) {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.userName = USER
        mqttConnectOptions.password = PASSWORD.toCharArray()
        mqttConnectOptions.socketFactory = sslSocketFactory
        try {
            mqttClient!!.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    val disconnectedBufferOptions =
                        DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient!!.setBufferOpts(disconnectedBufferOptions)

//                    subscribeToTopic(topic);
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    Log.i(
                        "TEST_APPLICATION",
                        "Failed to connectToMqttBroker to: $SERVER_URL_WITH_SSL"
                    )
                }
            })
        } catch (ex: MqttException) {
            Log.i("TEST_APPLICATION", "Exception while connecting to mqtt broker", ex)
        }
    }

    private fun createMqttClient(serverUrl: String): MqttAndroidClient {
        val mqttAndroidClient =
            MqttAndroidClient(applicationContext, serverUrl, "Behzzd-dn8Ty0")
        mqttAndroidClient.setCallback(this)
        mqttAndroidClient.setTraceEnabled(true)
        mqttAndroidClient.setTraceCallback(this)
        return mqttAndroidClient
    }

    private fun createSSLSocketFactory(): SSLSocketFactory? {
        return try {
            val caCF =
                CertificateFactory.getInstance("X.509")
            val ca = caCF.generateCertificate(
                this.assets.open("ca.crt")
            ) as X509Certificate
            val caKeyStore =
                KeyStore.getInstance(KeyStore.getDefaultType())
            caKeyStore.load(null, null)
            caKeyStore.setCertificateEntry(ca.subjectX500Principal.name, ca)
            val tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(caKeyStore)
            val context = SSLContext.getInstance("TLS")
            context.init(null, tmf.trustManagers, null)
            context.socketFactory
        } catch (e: IOException) {
            Log.i("TEST_APPLICATION", "Creating ssl socket factory failed")
            null
        } catch (e: KeyStoreException) {
            Log.i("TEST_APPLICATION", "Creating ssl socket factory failed")
            null
        } catch (e: KeyManagementException) {
            Log.i("TEST_APPLICATION", "Creating ssl socket factory failed")
            null
        } catch (e: NoSuchAlgorithmException) {
            Log.i("TEST_APPLICATION", "Creating ssl socket factory failed")
            null
        } catch (e: CertificateException) {
            Log.i("TEST_APPLICATION", "Creating ssl socket factory failed")
            null
        }
    }

    override fun connectionLost(cause: Throwable) {
        Log.i("TEST_APPLICATION", "Connection lost")
    }

    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        Log.i("TEST_APPLICATION", "message Arrived")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        Log.i("TEST_APPLICATION", "delivery compelete")
    }

    override fun traceDebug(tag: String, message: String) {
        Log.i("TEST_APPLICATION", "$tag | $message")
    }

    override fun traceError(tag: String, message: String) {
        Log.i("TEST_APPLICATION", "$tag || $message")
    }

    override fun traceException(
        tag: String,
        message: String,
        e: Exception
    ) {
        Log.i("TEST_APPLICATION", "$tag ||| $message")
    }

    companion object {
        private const val USER = ""
        private const val PASSWORD = ""
        private const val SERVER_URL_WITH_SSL = "ssl://mqtt-mini.facebook.com:443"
    }
}