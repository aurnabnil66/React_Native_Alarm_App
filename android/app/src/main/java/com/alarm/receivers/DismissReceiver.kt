package com.alarm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.alarm.Manager

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

class DismissReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmDismissReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: snooze alarm and send event to JS
        Log.d(TAG, "dismissed alarm notification for: ${intent.getStringExtra("ALARM_UID")}")
    }
}
