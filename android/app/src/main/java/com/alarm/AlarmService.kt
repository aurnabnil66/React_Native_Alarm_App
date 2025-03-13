package com.alarm

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class AlarmService : Service() {

    companion object {
        private const val TAG = "AlarmService"
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "On bind " + intent?.extras)
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating service")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Stopping service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "On start command")

        val alarmUid = intent?.getStringExtra("ALARM_UID")
        val alarm = alarmUid?.let { Storage.getAlarm(applicationContext, it) }
        val notification = alarm?.let { Helper.getAlarmNotification(this, it, 1) }
        alarmUid?.let { Manager.start(applicationContext, it) }
        notification?.let { startForeground(1, it) }

        return START_STICKY
    }
}
