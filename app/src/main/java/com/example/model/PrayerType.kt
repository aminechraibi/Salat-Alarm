package com.example.model

enum class PrayerType(
    val arabicName: String,
    val englishName: String,
    val order: Int
) {
    FAJR("الفجر", "Fajr", 0),
    CHOROUK("الشروق", "Sunrise", 1),
    DHUHR("الظهر", "Dhuhr", 2),
    ASR("العصر", "Asr", 3),
    MAGHRIB("المغرب", "Maghrib", 4),
    ISHA("العشاء", "Isha", 5);

    companion object {
        fun fromOrder(order: Int): PrayerType = entries.firstOrNull { it.order == order } ?: FAJR
    }
}
