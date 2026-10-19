package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.alarm.ScheduleReconciliation
import com.example.data.AppDatabase
import com.example.data.entity.OccurrenceStatus
import com.example.service.AlarmPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: return
        Log.d(TAG, "AlarmReceiver triggered for occurrenceId: $occurrenceId")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val occurrence = db.occurrenceDao().getOccurrenceById(occurrenceId)

                if (occurrence == null) {
                    Log.w(TAG, "Occurrence $occurrenceId not found in database")
                    return@launch
                }

                val now = System.currentTimeMillis()
                val diffMillis = now - occurrence.scheduledTimestamp

                // Expired check: If alarm fired more than 10 minutes late (e.g. device was off for hours), skip according to policy
                if (diffMillis > 10 * 60_000L) {
                    Log.w(TAG, "Occurrence $occurrenceId expired (delayed by ${diffMillis / 60_000} mins). Marking EXPIRED.")
                    db.occurrenceDao().updateStatus(occurrenceId, OccurrenceStatus.EXPIRED)
                    ScheduleReconciliation.reconcile(context)
                    return@launch
                }

                // Mark as FIRING
                db.occurrenceDao().updateStatus(occurrenceId, OccurrenceStatus.FIRING)

                // Start Foreground Playback Service
                val serviceIntent = Intent(context, AlarmPlaybackService::class.java).apply {
                    action = AlarmPlaybackService.ACTION_PLAY
                    putExtra(AlarmPlaybackService.EXTRA_OCCURRENCE_ID, occurrenceId)
                    putExtra(AlarmPlaybackService.EXTRA_RULE_ID, occurrence.ruleId)
                }
                ContextCompat.startForegroundService(context, serviceIntent)

            } catch (e: Exception) {
                Log.e(TAG, "Error in AlarmReceiver onReceive", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val TAG = "AlarmReceiver"
        const val ACTION_FIRE_ALARM = "com.example.ACTION_FIRE_ALARM"
        const val EXTRA_OCCURRENCE_ID = "extra_occurrence_id"
    }
}
