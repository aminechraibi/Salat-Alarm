package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.PrayerType

enum class OccurrenceType {
    NORMAL,
    SNOOZE
}

enum class OccurrenceStatus {
    SCHEDULED,
    FIRING,
    COMPLETED,
    SNOOZED,
    DISMISSED,
    EXPIRED
}

@Entity(
    tableName = "scheduled_occurrences",
    indices = [
        Index(value = ["ruleId", "targetDate", "occurrenceType"], unique = false),
        Index(value = ["scheduledTimestamp"]),
        Index(value = ["status"])
    ]
)
data class ScheduledOccurrenceEntity(
    @PrimaryKey val occurrenceId: String,
    val ruleId: String,
    val prayerType: PrayerType,
    val targetDate: String, // YYYY-MM-DD
    val scheduledTimestamp: Long, // Epoch millis
    val occurrenceType: OccurrenceType = OccurrenceType.NORMAL,
    val snoozeCount: Int = 0,
    val status: OccurrenceStatus = OccurrenceStatus.SCHEDULED,
    val lastUpdated: Long = System.currentTimeMillis()
)
