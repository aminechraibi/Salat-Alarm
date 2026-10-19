package com.example

import com.example.calc.PrayerTimeCalculator
import com.example.model.AsrMadhhab
import com.example.model.CalculationMethod
import com.example.model.HighLatitudeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PrayerCalculationTest {

    @Test
    fun testPrayerTimesChronologicalOrder() {
        val date = LocalDate.of(2026, 5, 15)
        val zone = ZoneId.of("Africa/Casablanca")
        val params = PrayerTimeCalculator.CalculationParameters(
            latitude = 33.5731,
            longitude = -7.5898,
            altitudeMeters = 27.0,
            zoneId = zone,
            method = CalculationMethod.MOROCCO,
            asrMadhhab = AsrMadhhab.STANDARD,
            highLatitudeRule = HighLatitudeRule.MIDNIGHT
        )

        val times = PrayerTimeCalculator.calculate(date, params)

        // Verification of chronological order
        assertTrue("Fajr must be before Chorouk", times.fajr.isBefore(times.chorouk))
        assertTrue("Chorouk must be before Dhuhr", times.chorouk.isBefore(times.dhuhr))
        assertTrue("Dhuhr must be before Asr", times.dhuhr.isBefore(times.asr))
        assertTrue("Asr must be before Maghrib", times.asr.isBefore(times.maghrib))
        assertTrue("Maghrib must be before Isha", times.maghrib.isBefore(times.isha))
    }

    @Test
    fun testHanafiAsrIsLaterThanStandardAsr() {
        val date = LocalDate.of(2026, 3, 21) // Equinox
        val zone = ZoneId.of("Asia/Riyadh")
        val baseParams = PrayerTimeCalculator.CalculationParameters(
            latitude = 24.7136,
            longitude = 46.6753,
            altitudeMeters = 612.0,
            zoneId = zone,
            method = CalculationMethod.SAUDI_ARABIA,
            asrMadhhab = AsrMadhhab.STANDARD
        )

        val standardTimes = PrayerTimeCalculator.calculate(date, baseParams)
        val hanafiTimes = PrayerTimeCalculator.calculate(date, baseParams.copy(asrMadhhab = AsrMadhhab.HANAFI))

        // Hanafi Asr (shadow factor 2) must be strictly after Standard Asr (shadow factor 1)
        assertTrue(
            "Hanafi Asr must be strictly later than Standard Asr",
            hanafiTimes.asr.isAfter(standardTimes.asr)
        )
    }

    @Test
    fun testMinuteAdjustments() {
        val date = LocalDate.of(2026, 6, 21)
        val zone = ZoneId.of("Africa/Cairo")
        val paramsWithoutAdj = PrayerTimeCalculator.CalculationParameters(
            latitude = 30.0444,
            longitude = 31.2357,
            altitudeMeters = 23.0,
            zoneId = zone,
            method = CalculationMethod.EGYPT,
            fajrAdjustmentMinutes = 0
        )
        val paramsWithAdj = paramsWithoutAdj.copy(fajrAdjustmentMinutes = 5)

        val timesWithout = PrayerTimeCalculator.calculate(date, paramsWithoutAdj)
        val timesWith = PrayerTimeCalculator.calculate(date, paramsWithAdj)

        val diffMinutes = java.time.Duration.between(timesWithout.fajr, timesWith.fajr).toMinutes()
        assertEquals(5L, diffMinutes)
    }

    @Test
    fun testUmmAlQuraIshaFixedOffset() {
        val date = LocalDate.of(2026, 1, 15)
        val zone = ZoneId.of("Asia/Riyadh")
        val params = PrayerTimeCalculator.CalculationParameters(
            latitude = 21.4225,
            longitude = 39.8262,
            altitudeMeters = 277.0,
            zoneId = zone,
            method = CalculationMethod.SAUDI_ARABIA
        )

        val times = PrayerTimeCalculator.calculate(date, params)
        val maghribToIshaMinutes = java.time.Duration.between(times.maghrib, times.isha).toMinutes()

        assertEquals(90L, maghribToIshaMinutes)
    }
}
