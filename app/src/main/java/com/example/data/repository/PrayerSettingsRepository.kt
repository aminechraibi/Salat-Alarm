package com.example.data.repository

import com.example.data.dao.PrayerSettingsDao
import com.example.data.entity.PrayerSettingsEntity
import kotlinx.coroutines.flow.Flow

class PrayerSettingsRepository(private val dao: PrayerSettingsDao) {
    val settingsFlow: Flow<PrayerSettingsEntity?> = dao.getSettingsFlow()

    suspend fun getSettings(): PrayerSettingsEntity {
        return dao.getSettings() ?: run {
            val defaultSettings = PrayerSettingsEntity()
            dao.insertOrUpdate(defaultSettings)
            defaultSettings
        }
    }

    suspend fun updateSettings(settings: PrayerSettingsEntity) {
        dao.insertOrUpdate(settings)
    }
}
