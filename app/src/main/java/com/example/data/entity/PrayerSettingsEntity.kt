package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AsrMadhhab
import com.example.model.CalculationMethod
import com.example.model.HighLatitudeRule

@Entity(tableName = "prayer_settings")
data class PrayerSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val cityNameArabic: String = "الدار البيضاء",
    val cityNameEnglish: String = "Casablanca",
    val countryArabic: String = "المغرب",
    val countryEnglish: String = "Morocco",
    val latitude: Double = 33.5731,
    val longitude: Double = -7.5898,
    val altitudeMeters: Double = 27.0,
    val timeZoneId: String = "Africa/Casablanca",
    val method: CalculationMethod = CalculationMethod.MOROCCO,
    val customFajrAngle: Double = 19.0,
    val customIshaAngle: Double = 17.0,
    val asrMadhhab: AsrMadhhab = AsrMadhhab.STANDARD,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDNIGHT,
    val fajrAdjustment: Int = 0,
    val sunriseAdjustment: Int = 0,
    val dhuhrAdjustment: Int = 0,
    val asrAdjustment: Int = 0,
    val maghribAdjustment: Int = 0,
    val ishaAdjustment: Int = 0,
    val is24HourFormat: Boolean = true,
    val isAutoLocation: Boolean = false,
    val configVersion: Int = 1
)
