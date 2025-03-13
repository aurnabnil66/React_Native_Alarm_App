package com.alarm

import android.content.Intent
import com.facebook.react.bridge.*

class Module(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val reactContext: ReactApplicationContext = reactContext

    init {
        Helper.createNotificationChannel(reactContext)
    }

    override fun getName(): String {
        return "AlarmModule"
    }

    @ReactMethod
    fun getState(promise: Promise) {
        promise.resolve(Manager.getActiveAlarm())
    }

    @ReactMethod
    fun set(details: ReadableMap, promise: Promise) {
        val alarm = parseAlarmObject(details)
        Manager.schedule(reactContext, alarm)
        promise.resolve(null)
    }

    @ReactMethod
    fun update(details: ReadableMap, promise: Promise) {
        val alarm = parseAlarmObject(details)
        Manager.update(reactContext, alarm)
        promise.resolve(null)
    }

    @ReactMethod
    fun remove(alarmUid: String, promise: Promise) {
        Manager.remove(reactContext, alarmUid)
        promise.resolve(null)
    }

    @ReactMethod
    fun removeAll(promise: Promise) {
        Manager.removeAll(reactContext)
        promise.resolve(null)
    }

    @ReactMethod
    fun enable(alarmUid: String, promise: Promise) {
        Manager.enable(reactContext, alarmUid)
        promise.resolve(null)
    }

    @ReactMethod
    fun disable(alarmUid: String, promise: Promise) {
        Manager.disable(reactContext, alarmUid)
        promise.resolve(null)
    }

    @ReactMethod
    fun stop(promise: Promise) {
        Manager.stop(reactContext)
        val serviceIntent = Intent(reactContext, AlarmService::class.java)
        reactContext.stopService(serviceIntent)
        promise.resolve(null)
    }

    @ReactMethod
    fun snooze(promise: Promise) {
        Manager.snooze(reactContext)
        val serviceIntent = Intent(reactContext, AlarmService::class.java)
        reactContext.stopService(serviceIntent)
        promise.resolve(null)
    }

    @ReactMethod
    fun get(alarmUid: String, promise: Promise) {
        try {
            val alarm = Storage.getAlarm(reactContext, alarmUid)
            promise.resolve(serializeAlarmObject(alarm))
        } catch (e: Exception) {
            e.printStackTrace()
            promise.reject(e.message, e)
        }
    }

    @ReactMethod
    fun getAll(promise: Promise) {
        try {
            val alarms = Storage.getAllAlarms(reactContext)
            promise.resolve(serializeArray(alarms))
        } catch (e: Exception) {
            e.printStackTrace()
            promise.reject(e.message, e)
        }
    }

    private fun parseAlarmObject(alarm: ReadableMap): Alarm {
        val uid = alarm.getString("uid") ?: ""
        val title = alarm.getString("title") ?: ""
        val description = alarm.getString("description") ?: ""
        val hour = alarm.getInt("hour")
        val minutes = alarm.getInt("minutes")
        val snoozeInterval = alarm.getInt("snoozeInterval")
        val repeating = alarm.getBoolean("repeating")
        val active = alarm.getBoolean("active")
        val days = mutableListOf<Int>()
        
        if (!alarm.isNull("days")) {
            val rawDays = alarm.getArray("days")
            for (i in 0 until (rawDays?.size() ?: 0)) {
                rawDays?.getInt(i)?.let { days.add(it) }
            }
        }
        return Alarm(uid, days, hour, minutes, snoozeInterval, title, description, repeating, active)
    }

    private fun serializeAlarmObject(alarm: Alarm): WritableMap {
        return WritableNativeMap().apply {
            putString("uid", alarm.uid)
            putString("title", alarm.title)
            putString("description", alarm.description)
            putInt("hour", alarm.hour)
            putInt("minutes", alarm.minutes)
            putInt("snoozeInterval", alarm.snoozeInterval)
            putArray("days", serializeArray(alarm.days))
            putBoolean("repeating", alarm.repeating)
            putBoolean("active", alarm.active)
        }
    }

    private fun serializeArray(a: List<Int>): WritableNativeArray {
        return WritableNativeArray().apply {
            a.forEach { pushInt(it) }
        }
    }

    private fun serializeArray(a: Array<Alarm>): WritableNativeArray {
        return WritableNativeArray().apply {
            a.forEach { pushMap(serializeAlarmObject(it)) }
        }
    }
}
