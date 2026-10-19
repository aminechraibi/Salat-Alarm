package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_assets")
data class AudioAssetEntity(
    @PrimaryKey val id: String,
    val displayNameArabic: String,
    val displayNameEnglish: String,
    val resourceName: String?,
    val filePath: String?,
    val durationSeconds: Double,
    val isBundled: Boolean,
    val sourceDescription: String = "",
    val licenseDescription: String = ""
)
