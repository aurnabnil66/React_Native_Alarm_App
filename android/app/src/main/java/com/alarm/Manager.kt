package com.alarm

import android.content.Context
import android.util.Log
import java.util.Date

object Manager {
    private const val TAG = "AlarmManager"
    private var sound: Sound? = null
    private var activeAlarmUid: String? = null

    fun getActiveAlarm(): String? = activeAlarmUid

    fun schedule(context: Context, alarm: Alarm) {
        val dates = alarm.getAlarmDates()
        for (date in dates.dates) {
            Helper.scheduleAlarm(context, alarm.uid, date.time, dates.getNotificationId(date))
        }
        Storage.saveAlarm(context, alarm)
        Storage.saveDates(context, dates)
    }

    fun reschedule(context: Context) {
        val alarms = Storage.getAllAlarms(context)
        for (alarm in alarms) {
            Storage.removeDates(context, alarm.uid)
            val dates = alarm.getAlarmDates()
            Storage.saveDates(context, dates)
            for (date in dates.dates) {
                Helper.scheduleAlarm(context, alarm.uid, date.time, dates.getNotificationId(date))
                Log.d(TAG, "Rescheduling alarm: ${alarm.uid}")
            }
        }
    }

    fun update(context: Context, alarm: Alarm) {
        val prevDates = Storage.getDates(context, alarm.uid)
        val dates = alarm.getAlarmDates()
        for (date in dates.dates) {
            Helper.scheduleAlarm(context, alarm.uid, date.time, dates.getNotificationId(date))
        }
        Storage.saveAlarm(context, alarm)
        Storage.saveDates(context, dates)
        prevDates?.dates?.forEach { date ->
            Helper.cancelAlarm(context, dates.getNotificationId(date))
        }
    }

    fun removeAll(context: Context) {
        val alarms = Storage.getAllAlarms(context)
        for (alarm in alarms) {
            remove(context, alarm.uid)
        }
    }

    fun remove(context: Context, alarmUid: String) {
        sound?.stop()
        val alarm = Storage.getAlarm(context, alarmUid)
        val dates = Storage.getDates(context, alarm.uid)
        Storage.removeAlarm(context, alarm.uid)
        Storage.removeDates(context, alarm.uid)
        dates?.dates?.forEach { date ->
            Helper.cancelAlarm(context, dates.getNotificationId(date))
        }
    }

    fun enable(context: Context, alarmUid: String) {
        val alarm = Storage.getAlarm(context, alarmUid)
        if (!alarm.active) {
            alarm.active = true
            Storage.saveAlarm(context, alarm)
        } else {
            Log.d(TAG, "Alarm already active - exiting job")
            return
        }
        val dates = alarm.getAlarmDates()
        Storage.saveDates(context, dates)
        for (date in dates.dates) {
            Helper.scheduleAlarm(context, alarmUid, date.time, dates.getNotificationId(date))
        }
    }

    fun disable(context: Context, alarmUid: String) {
        val alarm = Storage.getAlarm(context, alarmUid)
        if (alarm.active) {
            alarm.active = false
            Storage.saveAlarm(context, alarm)
        } else {
            Log.d(TAG, "Alarm already inactive - exiting job")
            return
        }
        val dates = Storage.getDates(context, alarmUid)
        dates?.dates?.forEach { date ->
            Helper.cancelAlarm(context, dates.getNotificationId(date))
        }
    }

    fun start(context: Context, alarmUid: String) {
        activeAlarmUid = alarmUid
        sound = Sound(context).apply { play("default") }
        Log.d(TAG, "Starting $activeAlarmUid")
    }

    fun stop(context: Context) {
        Log.d(TAG, "Stopping $activeAlarmUid")
        sound?.stop()
        val alarm = Storage.getAlarm(context, activeAlarmUid!!)
        val dates = Storage.getDates(context, activeAlarmUid!!)
        if (alarm.repeating) {
            val current = dates!!.currentDate
            val updated = AlarmDates.setNextWeek(current)
            dates.update(current, updated)
            Storage.saveDates(context, dates)
            Helper.scheduleAlarm(context, dates.alarmUid, updated.time, dates.currentNotificationId)
        } else {
            alarm.active = false
            Storage.saveAlarm(context, alarm)
            Storage.removeDates(context, activeAlarmUid!!)
        }
        activeAlarmUid = null
    }

    fun snooze(context: Context) {
        Log.d(TAG, "Snoozing $activeAlarmUid")
        sound?.stop()
        val alarm = Storage.getAlarm(context, activeAlarmUid!!)
        val dates = Storage.getDates(context, activeAlarmUid!!)
        val updated = AlarmDates.snooze(Date(), alarm.snoozeInterval)
        dates!!.update(dates.currentDate, updated)
        Storage.saveDates(context, dates)
        Helper.scheduleAlarm(context, dates.alarmUid, updated.time, dates.currentNotificationId)
        activeAlarmUid = null
    }
}
