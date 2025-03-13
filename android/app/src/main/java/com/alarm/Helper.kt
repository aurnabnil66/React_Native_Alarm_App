package com.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.alarm.receivers.AlarmReceiver
import com.alarm.receivers.DismissReceiver
import com.app.R
import java.util.*

class Helper {

    companion object {
        private const val TAG = "AlarmHelper"

        fun scheduleAlarm(context: Context, alarmUid: String, triggerAtMillis: Long, notificationID: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("ALARM_UID", alarmUid)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, notificationID, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ->
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                else ->
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            Log.d(TAG, "SDK version: ${Build.VERSION.SDK_INT}")
            Log.d(TAG, "Scheduling alarm with notification id: $notificationID")
            Log.d(TAG, "Alarm scheduled to fire in ${(triggerAtMillis - System.currentTimeMillis()) / (1000 * 60)} min")
        }

        fun cancelAlarm(context: Context, notificationID: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, notificationID, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Canceling alarm with notification id: $notificationID")
        }

        fun sendNotification(context: Context, alarm: Alarm, notificationID: Int) {
            try {
                val notification = getAlarmNotification(context, alarm, notificationID)
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationID, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getAlarmNotification(context: Context, alarm: Alarm, notificationID: Int): Notification {
            return getNotification(context, notificationID, alarm.uid, alarm.title, alarm.description)
        }

        fun cancelNotification(context: Context, notificationId: Int) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
            manager.cancelAll()
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val id = context.getString(R.string.notification_channel_id)
                val name = context.getString(R.string.notification_channel_name)
                val description = context.getString(R.string.notification_channel_desc)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(id, name, importance).apply {
                    this.description = description
                    enableLights(true)
                    lightColor = Color.RED
                    enableVibration(true)
                    vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                }
                val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
                Log.d(TAG, "Created a notification channel: $channel")
            } else {
                Log.d(TAG, "Didn't need to create a notification channel")
            }
        }

        private fun getNotification(context: Context, id: Int, alarmUid: String, title: String, description: String): Notification {
            val res = context.resources
            val packageName = context.packageName
            val smallIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName)
            val channelId = context.getString(R.string.notification_channel_id)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(smallIconResId)
                .setContentTitle(title)
                .setContentText(description)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setSound(null)
                .setVibrate(null)
                .setContentIntent(createOnClickedIntent(context, alarmUid, id))
                .setDeleteIntent(createOnDismissedIntent(context, alarmUid, id))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName)
                val largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId)
                if (largeIconResId != 0) builder.setLargeIcon(largeIconBitmap)
                builder.setCategory(NotificationCompat.CATEGORY_CALL)
                builder.color = Color.BLUE
            }
            return builder.build()
        }

        private fun createOnClickedIntent(context: Context, alarmUid: String, notificationID: Int): PendingIntent {
            val resultIntent = Intent(context, getMainActivityClass(context)).apply {
                putExtra("ALARM_UID", alarmUid)
            }
            return PendingIntent.getActivity(context, notificationID, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun createOnDismissedIntent(context: Context, alarmUid: String, notificationId: Int): PendingIntent {
            val intent = Intent(context, DismissReceiver::class.java).apply {
                putExtra("NOTIFICATION_ID", notificationId)
                putExtra("ALARM_UID", alarmUid)
            }
            return PendingIntent.getBroadcast(context, notificationId, intent, 0)
        }

        fun getDate(day: Int, hour: Int, minute: Int): Calendar {
            return Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 7)
                }
            }
        }

        fun getMainActivityClass(context: Context): Class<*>? {
            val packageName = context.packageName
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            return try {
                Class.forName(launchIntent?.component?.className ?: return null)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                null
            }
        }
    }
}
