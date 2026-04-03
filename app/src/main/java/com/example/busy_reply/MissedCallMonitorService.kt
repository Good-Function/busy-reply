package com.example.busy_reply

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/**
 * Foreground service so call-state listening survives process pressure after the user
 * dismisses the task. [MissedCallReplyTracker] receives state updates here.
 */
class MissedCallMonitorService : Service() {

    private var callStateListening = false
    private var telephonyCallback: TelephonyCallback? = null
    private var legacyPhoneStateListener: android.telephony.PhoneStateListener? = null

    override fun onCreate() {
        super.onCreate()
        MissedCallReplyTracker.init(this)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.missed_call_monitor_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.missed_call_monitor_channel_description)
            setShowBadge(false)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        if (!callStateListening) {
            callStateListening = true
            registerCallStateListener()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterCallStateListener()
        callStateListening = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.missed_call_monitor_notification_title))
            .setContentText(getString(R.string.missed_call_monitor_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun registerCallStateListener() {
        val tm = getSystemService(TelephonyManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerTelephonyCallbackApi31(tm)
        } else {
            @Suppress("DEPRECATION")
            legacyPhoneStateListener = object : android.telephony.PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    MissedCallReplyTracker.onCallStateChanged(state)
                }
            }.also {
                @Suppress("DEPRECATION")
                tm.listen(it, android.telephony.PhoneStateListener.LISTEN_CALL_STATE)
            }
        }
    }

    private fun unregisterCallStateListener() {
        val tm = getSystemService(TelephonyManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let { tm.unregisterTelephonyCallback(it) }
            telephonyCallback = null
        } else {
            @Suppress("DEPRECATION")
            legacyPhoneStateListener?.let {
                @Suppress("DEPRECATION")
                tm.listen(it, android.telephony.PhoneStateListener.LISTEN_NONE)
            }
            legacyPhoneStateListener = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun registerTelephonyCallbackApi31(telephonyManager: TelephonyManager) {
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                MissedCallReplyTracker.onCallStateChanged(state)
            }
        }
        telephonyManager.registerTelephonyCallback(executor, callback)
        telephonyCallback = callback
    }

    companion object {
        private const val CHANNEL_ID = "missed_call_monitor"
        private const val NOTIFICATION_ID = 1001
    }
}
