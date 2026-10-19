package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.AudioAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioAssetDao {
    @Query("SELECT * FROM audio_assets ORDER BY isBundled DESC, displayNameArabic ASC")
    fun getAllAssetsFlow(): Flow<List<AudioAssetEntity>>

    @Query("SELECT * FROM audio_assets ORDER BY isBundled DESC, displayNameArabic ASC")
    suspend fun getAllAssets(): List<AudioAssetEntity>

    @Query("SELECT * FROM audio_assets WHERE id = :id LIMIT 1")
    suspend fun getAssetById(id: String): AudioAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(asset: AudioAssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<AudioAssetEntity>)

    @Delete
    suspend fun delete(asset: AudioAssetEntity)
}
