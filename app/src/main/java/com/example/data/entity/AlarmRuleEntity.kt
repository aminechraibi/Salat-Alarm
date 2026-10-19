package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.PrayerType
import java.util.UUID

@Entity(tableName = "alarm_rules")
data class AlarmRuleEntity(
    @PrimaryKey val ruleId: String = UUID.randomUUID().toString(),
    val targetPrayer: PrayerType,
    val offsetMinutes: Int = 0, // negative = before, 0 = at prayer time, positive = after
    val isEnabled: Boolean = true,
    val audioAssetId: String = "reminder_generic",
    val volumePercent: Int = 100,
    val vibrationEnabled: Boolean = true,
    val repeatDaysMask: Int = 127, // 7 bits (Mon-Sun: bit 0=Mon, bit 6=Sun; 127=all days)
    val audioRepeatCount: Int = 1, // 1 to 5 repetitions
    val audioRepeatIntervalSec: Int = 5, // Delay between repetitions
    val maxAudioDurationSec: Int = 45, // Safety cut-off
    val snoozeEnabled: Boolean = true,
    val snoozeDurationMinutes: Int = 5,
    val maxSnoozeCount: Int = 3,
    val snoozeAudioId: String? = null,
    val startDateEpochDay: Long? = null,
    val endDateEpochDay: Long? = null,
    val label: String = ""
) {
    fun isDayEnabled(dayOfWeek1To7: Int): Boolean { // 1 = Mon, 7 = Sun
        val bit = 1 shl (dayOfWeek1To7 - 1)
        return (repeatDaysMask and bit) != 0
    }

    fun getOffsetSummary(): String {
        return when {
            offsetMinutes == 0 -> "في وقت الأذان (0 د)"
            offsetMinutes < 0 -> "قبل الأذان بـ ${kotlin.math.abs(offsetMinutes)} دقيقة"
            else -> "بعد الأذان بـ $offsetMinutes دقيقة"
        }
    }
}
