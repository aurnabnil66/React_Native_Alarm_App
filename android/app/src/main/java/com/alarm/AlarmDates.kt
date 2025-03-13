package com.alarm
import com.google.gson.Gson
import java.util.*

class Alarm(
    var uid: String,
    var days: ArrayList<Int>,
    var hour: Int,
    var minutes: Int,
    var snoozeInterval: Int,
    var title: String,
    var description: String,
    var repeating: Boolean,
    var active: Boolean
) : Cloneable {

    fun getDates(): Array<Date> {
        return days.map { day ->
            Helper.getDate(day, hour, minutes).time
        }.toTypedArray()
    }

    fun getAlarmDates(): AlarmDates {
        return AlarmDates(uid, getDates(),)
    }

    override fun clone(): Alarm {
        return super.clone() as Alarm
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Alarm) return false

        return hour == other.hour &&
                minutes == other.minutes &&
                snoozeInterval == other.snoozeInterval &&
                uid == other.uid &&
                days == other.days &&
                title == other.title &&
                description == other.description
    }

    companion object {
        fun fromJson(json: String): Alarm {
            return Gson().fromJson(json, Alarm::class.java)
        }

        fun toJson(alarm: Alarm): String {
            return Gson().toJson(alarm)
        }
    }
}

class AlarmDates(
    var alarmUid: String,
    var dates: Array<Date>,
    val currentDate: Date,
    val currentNotificationId: Int
) {
    var uid: String = "${alarmUid}_DATES"

    var notificationIds: IntArray = IntArray(dates.size) { randomId() }

    fun getNotificationId(date: Date): Int {
        return dates.indexOf(date).takeIf { it >= 0 }?.let { notificationIds[it] } ?: -1
    }

    fun getCurrentNotificationId(): Int {
        val current = getCurrentDate() ?: return -1
        return getNotificationId(current)
    }

    fun getCurrentDate(): Date? {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        return dates.find {
            Calendar.getInstance().apply { time = it }.get(Calendar.DAY_OF_WEEK) == currentDay
        }
    }

    fun getDatesList(): ArrayList<Date> {
        return ArrayList(dates.asList())
    }

    fun update(old: Date, updated: Date) {
        dates.indexOf(old).takeIf { it >= 0 }?.let { dates[it] = updated }
    }


    companion object {
        private const val POSTFIX = "_DATES"

        fun getDatesId(alarmUid: String): String {
            return "$alarmUid$POSTFIX"
        }

        fun fromJson(json: String): AlarmDates {
            return Gson().fromJson(json, AlarmDates::class.java)
        }

        fun toJson(dates: AlarmDates): String {
            return Gson().toJson(dates)
        }

        fun isDatesId(id: String): Boolean {
            return id.contains(POSTFIX)
        }

        private fun randomId(): Int {
            return (Math.random() * 10000000).toInt()
        }
    }
}

// package com.alarm.receivers

// import android.content.BroadcastReceiver
// import android.content.Context
// import android.content.Intent
// import android.util.Log

// import com.alarm.Manager

// class BootReceiver : BroadcastReceiver() {

//     companion object {
//         private const val TAG = "AlarmBootReceiver"
//     }

//     override fun onReceive(context: Context, intent: Intent) {
//         val action = intent.action
//         if (action == "android.intent.action.BOOT_COMPLETED") {
//             Log.d(TAG, "received on boot intent: $action")
//             Manager.reschedule(context)
//         }
//     }
// }

// class DismissReceiver : BroadcastReceiver() {

//     companion object {
//         private const val TAG = "AlarmDismissReceiver"
//     }

//     override fun onReceive(context: Context, intent: Intent) {
//         // TODO: snooze alarm and send event to JS
//         Log.d(TAG, "dismissed alarm notification for: ${intent.getStringExtra("ALARM_UID")}")
//     }
// }
