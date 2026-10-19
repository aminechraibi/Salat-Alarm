package com.example.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class PrayerTimesDay(
    val date: LocalDate,
    val fajr: ZonedDateTime,
    val chorouk: ZonedDateTime,
    val dhuhr: ZonedDateTime,
    val asr: ZonedDateTime,
    val maghrib: ZonedDateTime,
    val isha: ZonedDateTime
) {
    fun getTime(type: PrayerType): ZonedDateTime = when (type) {
        PrayerType.FAJR -> fajr
        PrayerType.CHOROUK -> chorouk
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
    }

    fun getAllTimes(): List<Pair<PrayerType, ZonedDateTime>> = listOf(
        PrayerType.FAJR to fajr,
        PrayerType.CHOROUK to chorouk,
        PrayerType.DHUHR to dhuhr,
        PrayerType.ASR to asr,
        PrayerType.MAGHRIB to maghrib,
        PrayerType.ISHA to isha
    )
}
