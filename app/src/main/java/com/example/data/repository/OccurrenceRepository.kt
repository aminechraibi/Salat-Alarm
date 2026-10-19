package com.example.data.repository

import com.example.data.dao.OccurrenceDao
import com.example.data.entity.OccurrenceStatus
import com.example.data.entity.ScheduledOccurrenceEntity
import kotlinx.coroutines.flow.Flow

class OccurrenceRepository(private val dao: OccurrenceDao) {
    fun getUpcomingOccurrencesFlow(minTimestamp: Long = System.currentTimeMillis() - 60_000): Flow<List<ScheduledOccurrenceEntity>> =
        dao.getUpcomingOccurrencesFlow(minTimestamp)

    suspend fun getUpcomingOccurrences(minTimestamp: Long = System.currentTimeMillis() - 60_000): List<ScheduledOccurrenceEntity> =
        dao.getUpcomingOccurrences(minTimestamp)

    suspend fun getOccurrenceById(id: String): ScheduledOccurrenceEntity? = dao.getOccurrenceById(id)

    suspend fun getOccurrenceByRuleAndDate(ruleId: String, date: String): ScheduledOccurrenceEntity? =
        dao.getOccurrenceByRuleAndDate(ruleId, date)

    suspend fun saveOccurrence(occurrence: ScheduledOccurrenceEntity) = dao.insertOrUpdate(occurrence)

    suspend fun saveAll(occurrences: List<ScheduledOccurrenceEntity>) = dao.insertAll(occurrences)

    suspend fun updateStatus(occurrenceId: String, status: OccurrenceStatus) = dao.updateStatus(occurrenceId, status)

    suspend fun deleteFutureScheduledByRuleId(ruleId: String) = dao.deleteFutureScheduledByRuleId(ruleId)

    suspend fun cleanOldOccurrences() {
        val oneDayAgo = System.currentTimeMillis() - (24 * 3600 * 1000L)
        dao.deleteOldOccurrences(oneDayAgo)
    }
}
