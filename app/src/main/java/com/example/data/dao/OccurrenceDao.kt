package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.OccurrenceStatus
import com.example.data.entity.ScheduledOccurrenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OccurrenceDao {
    @Query("SELECT * FROM scheduled_occurrences WHERE status = 'SCHEDULED' AND scheduledTimestamp >= :minTimestamp ORDER BY scheduledTimestamp ASC")
    fun getUpcomingOccurrencesFlow(minTimestamp: Long): Flow<List<ScheduledOccurrenceEntity>>

    @Query("SELECT * FROM scheduled_occurrences WHERE status = 'SCHEDULED' AND scheduledTimestamp >= :minTimestamp ORDER BY scheduledTimestamp ASC")
    suspend fun getUpcomingOccurrences(minTimestamp: Long): List<ScheduledOccurrenceEntity>

    @Query("SELECT * FROM scheduled_occurrences WHERE occurrenceId = :occurrenceId LIMIT 1")
    suspend fun getOccurrenceById(occurrenceId: String): ScheduledOccurrenceEntity?

    @Query("SELECT * FROM scheduled_occurrences WHERE ruleId = :ruleId AND targetDate = :date LIMIT 1")
    suspend fun getOccurrenceByRuleAndDate(ruleId: String, date: String): ScheduledOccurrenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(occurrence: ScheduledOccurrenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(occurrences: List<ScheduledOccurrenceEntity>)

    @Update
    suspend fun update(occurrence: ScheduledOccurrenceEntity)

    @Query("UPDATE scheduled_occurrences SET status = :status, lastUpdated = :now WHERE occurrenceId = :occurrenceId")
    suspend fun updateStatus(occurrenceId: String, status: OccurrenceStatus, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM scheduled_occurrences WHERE ruleId = :ruleId AND status = 'SCHEDULED'")
    suspend fun deleteFutureScheduledByRuleId(ruleId: String)

    @Query("DELETE FROM scheduled_occurrences WHERE scheduledTimestamp < :cutoffTimestamp")
    suspend fun deleteOldOccurrences(cutoffTimestamp: Long)
}
