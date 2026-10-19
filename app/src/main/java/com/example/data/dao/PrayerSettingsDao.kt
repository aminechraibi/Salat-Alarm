package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PrayerSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerSettingsDao {
    @Query("SELECT * FROM prayer_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<PrayerSettingsEntity?>

    @Query("SELECT * FROM prayer_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): PrayerSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: PrayerSettingsEntity)

    @Update
    suspend fun update(settings: PrayerSettingsEntity)
}
