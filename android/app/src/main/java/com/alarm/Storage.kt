package com.alarm

import android.content.Context
import android.content.SharedPreferences
import com.react_native_alarm_app.R

class Storage {

    companion object {
        fun saveAlarm(context: Context, alarm: Alarm) {
            getEditor(context).putString(alarm.uid, Alarm.toJson(alarm)).apply()
        }

        fun saveDates(context: Context, dates: AlarmDates) {
            getEditor(context).putString(dates.uid, AlarmDates.toJson(dates)).apply()
        }

        fun getAllAlarms(context: Context): Array<Alarm> {
            val alarms = mutableListOf<Alarm>()
            val preferences = getSharedPreferences(context)
            val keyMap = preferences.all

            for ((key, value) in keyMap) {
                if (AlarmDates.isDatesId(key)) continue
                alarms.add(Alarm.fromJson(value as String))
            }
            return alarms.toTypedArray()
        }

        fun getAlarm(context: Context, alarmUid: String): Alarm? {
            val preferences = getSharedPreferences(context)
            return preferences.getString(alarmUid, null)?.let { Alarm.fromJson(it) }
        }

        fun getDates(context: Context, alarmUid: String): AlarmDates? {
            val preferences = getSharedPreferences(context)
            val json = preferences.getString(AlarmDates.getDatesId(alarmUid), null)
            return json?.let { AlarmDates.fromJson(it) }
        }

        fun removeAlarm(context: Context, alarmUid: String) {
            remove(context, alarmUid)
        }

        fun removeDates(context: Context, alarmUid: String) {
            remove(context, AlarmDates.getDatesId(alarmUid))
        }

        private fun remove(context: Context, id: String) {
            getSharedPreferences(context).edit().remove(id).apply()
        }

        private fun getEditor(context: Context): SharedPreferences.Editor {
            return getSharedPreferences(context).edit()
        }

        private fun getSharedPreferences(context: Context): SharedPreferences {
            val fileKey = context.getString(R.string.notification_channel_id)
            return context.getSharedPreferences(fileKey, Context.MODE_PRIVATE)
        }
    }
}
