package com.example.alarm

import android.content.Context
import android.util.Log
import com.example.calc.PrayerTimeCalculator
import com.example.data.AppDatabase
import com.example.data.entity.OccurrenceStatus
import com.example.data.entity.OccurrenceType
import com.example.data.entity.ScheduledOccurrenceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

object ScheduleReconciliation {
    private const val TAG = "ScheduleReconciliation"

    suspend fun reconcile(context: Context, scheduler: IAlarmScheduler? = null) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val actualScheduler = scheduler ?: AndroidAlarmScheduler(context)

        val settings = db.prayerSettingsDao().getSettings() ?: return@withContext
        val rules = db.alarmRuleDao().getEnabledRules()
        val now = System.currentTimeMillis()

        val zoneId = try {
            ZoneId.of(settings.timeZoneId)
        } catch (e: Exception) {
            ZoneId.systemDefault()
        }

        val calcParams = PrayerTimeCalculator.CalculationParameters(
            latitude = settings.latitude,
            longitude = settings.longitude,
            altitudeMeters = settings.altitudeMeters,
            zoneId = zoneId,
            method = settings.method,
            customFajrAngle = settings.customFajrAngle,
            customIshaAngle = settings.customIshaAngle,
            asrMadhhab = settings.asrMadhhab,
            highLatitudeRule = settings.highLatitudeRule,
            fajrAdjustmentMinutes = settings.fajrAdjustment,
            sunriseAdjustmentMinutes = settings.sunriseAdjustment,
            dhuhrAdjustmentMinutes = settings.dhuhrAdjustment,
            asrAdjustmentMinutes = settings.asrAdjustment,
            maghribAdjustmentMinutes = settings.maghribAdjustment,
            ishaAdjustmentMinutes = settings.ishaAdjustment
        )

        val today = LocalDate.now(zoneId)
        val datesToSchedule = listOf(today, today.plusDays(1))

        for (date in datesToSchedule) {
            val dateStr = date.toString()
            val dayOfWeek = date.dayOfWeek.value // 1 = Monday .. 7 = Sunday
            val epochDay = date.toEpochDay()
            val prayerTimes = PrayerTimeCalculator.calculate(date, calcParams)

            for (rule in rules) {
                // Check weekday filter
                if (!rule.isDayEnabled(dayOfWeek)) continue

                // Check date boundaries
                if (rule.startDateEpochDay != null && epochDay < rule.startDateEpochDay) continue
                if (rule.endDateEpochDay != null && epochDay > rule.endDateEpochDay) continue

                val baseTime = prayerTimes.getTime(rule.targetPrayer)
                val targetZoned = baseTime.plusMinutes(rule.offsetMinutes.toLong())
                val triggerMillis = targetZoned.toInstant().toEpochMilli()

                if (triggerMillis <= now) {
                    // Occurrence already passed for this date
                    continue
                }

                val occurrenceId = "${rule.ruleId}_$dateStr"
                val existing = db.occurrenceDao().getOccurrenceById(occurrenceId)

                if (existing == null || existing.status == OccurrenceStatus.SCHEDULED) {
                    val occurrence = ScheduledOccurrenceEntity(
                        occurrenceId = occurrenceId,
                        ruleId = rule.ruleId,
                        prayerType = rule.targetPrayer,
                        targetDate = dateStr,
                        scheduledTimestamp = triggerMillis,
                        occurrenceType = OccurrenceType.NORMAL,
                        snoozeCount = 0,
                        status = OccurrenceStatus.SCHEDULED,
                        lastUpdated = now
                    )
                    db.occurrenceDao().insertOrUpdate(occurrence)
                    actualScheduler.scheduleAlarm(occurrenceId, triggerMillis)
                    Log.d(TAG, "Reconciled occurrence $occurrenceId for prayer ${rule.targetPrayer} at $targetZoned")
                }
            }
        }

        // Clean up occurrences older than 24h
        db.occurrenceDao().deleteOldOccurrences(now - (24 * 3600 * 1000L))
    }

    suspend fun cancelRuleOccurrences(context: Context, ruleId: String, scheduler: IAlarmScheduler? = null) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val actualScheduler = scheduler ?: AndroidAlarmScheduler(context)

        val occurrences = db.occurrenceDao().getUpcomingOccurrences(System.currentTimeMillis())
        for (occ in occurrences) {
            if (occ.ruleId == ruleId) {
                actualScheduler.cancelAlarm(occ.occurrenceId)
                db.occurrenceDao().updateStatus(occ.occurrenceId, OccurrenceStatus.DISMISSED)
            }
        }
    }
}
