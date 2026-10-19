package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.receiver.AlarmReceiver

class AndroidAlarmScheduler(private val context: Context) : IAlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    override fun scheduleAlarm(occurrenceId: String, triggerTimeMillis: Long) {
        if (triggerTimeMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Skipping scheduling occurrence $occurrenceId in the past: $triggerTimeMillis")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
            putExtra(AlarmReceiver.EXTRA_OCCURRENCE_ID, occurrenceId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            occurrenceId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm for $occurrenceId at $triggerTimeMillis")
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled inexact fallback alarm for $occurrenceId at $triggerTimeMillis")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm, falling back", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed fallback alarm scheduling", fallbackEx)
            }
        }
    }

    override fun cancelAlarm(occurrenceId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
            putExtra(AlarmReceiver.EXTRA_OCCURRENCE_ID, occurrenceId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            occurrenceId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for occurrence $occurrenceId")
        }
    }

    companion object {
        private const val TAG = "AndroidAlarmScheduler"
    }
}
