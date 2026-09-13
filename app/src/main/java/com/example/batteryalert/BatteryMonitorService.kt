package com.example.batteryalert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import java.util.Locale

class BatteryMonitorService : Service() {

    companion object {
        private const val CHANNEL_ID = "battery_alert"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val ALERT_NOTIFICATION_ID = 2
        private const val EXTRA_THRESHOLD = "threshold"

        fun start(context: Context, threshold: Int) {
            val intent = Intent(context, BatteryMonitorService::class.java)
                .putExtra(EXTRA_THRESHOLD, threshold)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BatteryMonitorService::class.java))
        }

        fun postTestNotification(context: Context) {
            ensureChannel(context)
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.notify(
                ALERT_NOTIFICATION_ID,
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("バッテリー通知（テスト）")
                    .setContentText("通知が正しく表示されています")
                    .setAutoCancel(true)
                    .build()
            )
        }

        private fun ensureChannel(context: Context) {
            val nm = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID, "バッテリー通知", NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "バッテリー残量が指定値に達したときのお知らせ"
            nm.createNotificationChannel(channel)
        }
    }

    private var threshold = 20
    private var lastAbove: Boolean? = null
    private var tts: TextToSpeech? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            if (scale <= 0) return
            val percent = level * 100 / scale

            val above = percent >= threshold
            if (lastAbove != null && lastAbove != above) {
                notifyThresholdReached(percent)
            }
            lastAbove = above
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        threshold = intent?.getIntExtra(EXTRA_THRESHOLD, 20) ?: 20
        lastAbove = null
        ensureChannel(this)
        initTts()
        startForegroundCompat(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification(threshold))
        registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun initTts() {
        if (tts == null) {
            tts = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.JAPANESE
                }
            }
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "battery_alert")
    }

    private fun startForegroundCompat(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(id, notification)
        }
    }

    private fun buildForegroundNotification(threshold: Int): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("バッテリー監視中")
            .setContentText("残量${threshold}%で通知します")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build()
    }

    private fun notifyThresholdReached(percent: Int) {
        val nm = getSystemService(NotificationManager::class.java)
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        nm.notify(
            ALERT_NOTIFICATION_ID,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("バッテリー残量")
                .setContentText("バッテリーが ${percent}% になりました")
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()
        )
        speak("バッテリーが${percent}%になりました")
    }
}
