package com.example.alarm

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.entity.OccurrenceStatus
import com.example.data.entity.OccurrenceType
import com.example.data.entity.ScheduledOccurrenceEntity
import com.example.service.AlarmPlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SnoozeManager {
    private const val TAG = "SnoozeManager"

    suspend fun snooze(context: Context, occurrenceId: String, scheduler: IAlarmScheduler? = null): Boolean = withContext(Dispatchers.IO) {
        // Stop current audio immediately
        val stopIntent = Intent(context, AlarmPlaybackService::class.java).apply {
            action = AlarmPlaybackService.ACTION_STOP
        }
        context.startService(stopIntent)

        val db = AppDatabase.getInstance(context)
        val actualScheduler = scheduler ?: AndroidAlarmScheduler(context)

        val occurrence = db.occurrenceDao().getOccurrenceById(occurrenceId) ?: run {
            Log.e(TAG, "Cannot snooze: occurrence $occurrenceId not found")
            return@withContext false
        }

        val rule = db.alarmRuleDao().getRuleById(occurrence.ruleId) ?: run {
            Log.e(TAG, "Cannot snooze: rule ${occurrence.ruleId} not found")
            return@withContext false
        }

        if (!rule.snoozeEnabled) {
            Log.w(TAG, "Snooze disabled for rule ${rule.ruleId}")
            return@withContext false
        }

        if (occurrence.snoozeCount >= rule.maxSnoozeCount) {
            Log.w(TAG, "Max snooze count (${rule.maxSnoozeCount}) reached for occurrence $occurrenceId")
            db.occurrenceDao().updateStatus(occurrenceId, OccurrenceStatus.DISMISSED)
            return@withContext false
        }

        val now = System.currentTimeMillis()
        val nextTrigger = now + (rule.snoozeDurationMinutes * 60_000L)
        val newSnoozeCount = occurrence.snoozeCount + 1
        val newOccurrenceId = "${occurrence.ruleId}_${occurrence.targetDate}_snz$newSnoozeCount"

        // Mark current occurrence as snoozed
        db.occurrenceDao().updateStatus(occurrenceId, OccurrenceStatus.SNOOZED, now)

        // Create new snooze occurrence
        val snoozeOccurrence = ScheduledOccurrenceEntity(
            occurrenceId = newOccurrenceId,
            ruleId = rule.ruleId,
            prayerType = occurrence.prayerType,
            targetDate = occurrence.targetDate,
            scheduledTimestamp = nextTrigger,
            occurrenceType = OccurrenceType.SNOOZE,
            snoozeCount = newSnoozeCount,
            status = OccurrenceStatus.SCHEDULED,
            lastUpdated = now
        )
        db.occurrenceDao().insertOrUpdate(snoozeOccurrence)
        actualScheduler.scheduleAlarm(newOccurrenceId, nextTrigger)
        Log.d(TAG, "Scheduled snooze #$newSnoozeCount for $newOccurrenceId in ${rule.snoozeDurationMinutes} mins")

        true
    }

    suspend fun dismiss(context: Context, occurrenceId: String) = withContext(Dispatchers.IO) {
        val stopIntent = Intent(context, AlarmPlaybackService::class.java).apply {
            action = AlarmPlaybackService.ACTION_STOP
        }
        context.startService(stopIntent)

        val db = AppDatabase.getInstance(context)
        db.occurrenceDao().updateStatus(occurrenceId, OccurrenceStatus.DISMISSED)
        Log.d(TAG, "Dismissed occurrence $occurrenceId")
    }
}
