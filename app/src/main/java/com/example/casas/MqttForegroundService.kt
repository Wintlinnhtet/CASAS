package com.example.casas

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID

class MqttForegroundService : Service() {

    private val binder = LocalBinder()
    private var mqttClient: MqttClient? = null

    companion object {
        private const val CHANNEL_ID = "mqtt_channel"
        private const val NOTIF_ID = 1001

        // 🔧 set your broker here
        private const val BROKER_URI = "tcp://192.168.1.8:1883"
        private const val CLIENT_PREFIX = "AndroidPub-"

        // If you enable auth on broker later:
        private val MQTT_USERNAME: String? = null
        private val MQTT_PASSWORD: String? = null
    }

    inner class LocalBinder : Binder() {
        fun getService(): MqttForegroundService = this@MqttForegroundService
    }
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, notif("Starting…"))
        connect()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "MQTT Service",
                NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun notif(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("MQTT")
            .setContentText(text)
            .setOngoing(true)
            .build()

    private fun connect() {
        val clientId = CLIENT_PREFIX + UUID.randomUUID().toString().take(8)
        mqttClient = MqttClient(BROKER_URI, clientId, null)
        val opts = MqttConnectOptions().apply {
            isCleanSession = true
            isAutomaticReconnect = true
            connectionTimeout = 10
            keepAliveInterval = 30
            if (!MQTT_USERNAME.isNullOrEmpty() && !MQTT_PASSWORD.isNullOrEmpty()) {
                userName = MQTT_USERNAME
                password = MQTT_PASSWORD.toCharArray()
            }
        }

        try {
            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.w("MQTT", "lost: ${cause?.message}")
                    notify("Disconnected")
                }
                override fun messageArrived(topic: String?, message: MqttMessage?) {}
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })
            mqttClient?.connect(opts)
            notify("Connected to $BROKER_URI")
        } catch (e: Exception) {
            Log.e("MQTT", "connect fail", e)
            notify("Connect failed: ${e.message}")
        }
    }

    fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val c = mqttClient
            if (c?.isConnected == true) {
                val msg = MqttMessage(payload.toByteArray()).apply { this.qos = qos; isRetained = retained }
                c.publish(topic, msg)
            } else {
                Log.w("MQTT", "publish skipped: not connected")
            }
        } catch (e: Exception) {
            Log.e("MQTT", "publish error", e)
        }
    }

    private fun notify(text: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, notif(text))
    }

    override fun onDestroy() {
        try { mqttClient?.disconnect(); mqttClient?.close() } catch (_: Exception) {}
        super.onDestroy()
    }
}
