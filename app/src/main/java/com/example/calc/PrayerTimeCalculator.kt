package com.example.calc

import com.example.model.AsrMadhhab
import com.example.model.CalculationMethod
import com.example.model.HighLatitudeRule
import com.example.model.PrayerTimesDay
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object PrayerTimeCalculator {

    data class CalculationParameters(
        val latitude: Double,
        val longitude: Double,
        val altitudeMeters: Double = 0.0,
        val zoneId: ZoneId,
        val method: CalculationMethod = CalculationMethod.MOROCCO,
        val customFajrAngle: Double = 18.0,
        val customIshaAngle: Double = 17.0,
        val asrMadhhab: AsrMadhhab = AsrMadhhab.STANDARD,
        val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDNIGHT,
        val fajrAdjustmentMinutes: Int = 0,
        val sunriseAdjustmentMinutes: Int = 0,
        val dhuhrAdjustmentMinutes: Int = 0,
        val asrAdjustmentMinutes: Int = 0,
        val maghribAdjustmentMinutes: Int = 0,
        val ishaAdjustmentMinutes: Int = 0
    )

    fun calculate(date: LocalDate, params: CalculationParameters): PrayerTimesDay {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        val jd = julianDay(year, month, day) - (params.longitude / (15.0 * 24.0))

        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(atan2Deg(cosDeg(e) * sinDeg(l), cosDeg(l))) / 15.0

        val declination = asinDeg(sinDeg(e) * sinDeg(l))
        val eqt = (q / 15.0) - ra

        // Solar transit (Midday / Dhuhr base) in local standard solar hours
        val dhuhrBase = 12.0 - eqt

        // Sun disc adjustment for sunrise/sunset (accounting for atmospheric refraction and elevation)
        val sunriseAngle = 0.8333 + (0.0347 * sqrt(params.altitudeMeters.coerceAtLeast(0.0)))

        // Sunrise and Sunset hour angles
        val sunHourAngle = hourAngle(-sunriseAngle, params.latitude, declination)
        val sunriseBase = if (sunHourAngle != null) dhuhrBase - sunHourAngle else dhuhrBase - 6.0
        val sunsetBase = if (sunHourAngle != null) dhuhrBase + sunHourAngle else dhuhrBase + 6.0

        // Fajr angle
        val fajrAngle = if (params.method == CalculationMethod.CUSTOM) params.customFajrAngle else params.method.fajrAngle
        var fajrHourAngle = hourAngle(-fajrAngle, params.latitude, declination)
        var fajrBase = if (fajrHourAngle != null) dhuhrBase - fajrHourAngle else null

        // Isha angle / time
        val ishaAngle = if (params.method == CalculationMethod.CUSTOM) params.customIshaAngle else params.method.ishaAngle
        var ishaHourAngle = if (params.method.ishaMinutesAfterMaghrib == null) {
            hourAngle(-ishaAngle, params.latitude, declination)
        } else {
            null
        }
        var ishaBase = if (ishaHourAngle != null) {
            dhuhrBase + ishaHourAngle
        } else if (params.method.ishaMinutesAfterMaghrib != null) {
            sunsetBase + (params.method.ishaMinutesAfterMaghrib / 60.0)
        } else {
            null
        }

        // Night length between sunset and next sunrise
        val nightLengthHours = 24.0 - (sunsetBase - sunriseBase)

        // Apply high latitude rule if needed
        if (fajrBase == null || params.highLatitudeRule != HighLatitudeRule.NONE) {
            val portion = when (params.highLatitudeRule) {
                HighLatitudeRule.NONE -> 0.5
                HighLatitudeRule.MIDNIGHT -> 0.5
                HighLatitudeRule.ONE_SEVENTH -> 1.0 / 7.0
                HighLatitudeRule.ANGLE_BASED -> fajrAngle / 60.0
            }
            val maxFajrDiff = nightLengthHours * portion
            val calculatedDiff = sunriseBase - (fajrBase ?: (sunriseBase - maxFajrDiff))
            if (fajrBase == null || calculatedDiff > maxFajrDiff) {
                fajrBase = sunriseBase - maxFajrDiff
            }
        }

        if (ishaBase == null || (params.method.ishaMinutesAfterMaghrib == null && params.highLatitudeRule != HighLatitudeRule.NONE)) {
            val portion = when (params.highLatitudeRule) {
                HighLatitudeRule.NONE -> 0.5
                HighLatitudeRule.MIDNIGHT -> 0.5
                HighLatitudeRule.ONE_SEVENTH -> 1.0 / 7.0
                HighLatitudeRule.ANGLE_BASED -> ishaAngle / 60.0
            }
            val maxIshaDiff = nightLengthHours * portion
            val calculatedDiff = (ishaBase ?: (sunsetBase + maxIshaDiff)) - sunsetBase
            if (ishaBase == null || calculatedDiff > maxIshaDiff) {
                ishaBase = sunsetBase + maxIshaDiff
            }
        }

        // Asr calculation
        val shadowFactor = params.asrMadhhab.shadowFactor
        val asrAngle = -atanDeg(1.0 / (shadowFactor + tanDeg(kotlin.math.abs(params.latitude - declination))))
        val asrHourAngle = hourAngle(-asrAngle, params.latitude, declination)
        val asrBase = if (asrHourAngle != null) dhuhrBase + asrHourAngle else dhuhrBase + 3.0

        // Convert base solar times (which are relative to solar transit at Greenwich) to local clock time
        val timezoneOffsetHours = params.zoneId.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0
        val longitudeHours = params.longitude / 15.0

        fun toZonedDateTime(solarHours: Double, adjustmentMinutes: Int): ZonedDateTime {
            val localSolarHours = solarHours + timezoneOffsetHours - longitudeHours
            val normalizedHours = fixHour(localSolarHours)
            val totalSeconds = (normalizedHours * 3600.0).toLong() + (adjustmentMinutes * 60L)
            
            val safeSeconds = (totalSeconds % 86400 + 86400) % 86400
            val hours = (safeSeconds / 3600).toInt()
            val minutes = ((safeSeconds % 3600) / 60).toInt()
            val seconds = (safeSeconds % 60).toInt()

            val localTime = LocalTime.of(hours.coerceIn(0, 23), minutes.coerceIn(0, 59), seconds.coerceIn(0, 59))
            return ZonedDateTime.of(date, localTime, params.zoneId)
        }

        return PrayerTimesDay(
            date = date,
            fajr = toZonedDateTime(fajrBase, params.fajrAdjustmentMinutes),
            chorouk = toZonedDateTime(sunriseBase, params.sunriseAdjustmentMinutes),
            dhuhr = toZonedDateTime(dhuhrBase, params.dhuhrAdjustmentMinutes),
            asr = toZonedDateTime(asrBase, params.asrAdjustmentMinutes),
            maghrib = toZonedDateTime(sunsetBase, params.maghribAdjustmentMinutes),
            isha = toZonedDateTime(ishaBase, params.ishaAdjustmentMinutes)
        )
    }

    private fun hourAngle(altitude: Double, latitude: Double, declination: Double): Double? {
        val cosH = (sinDeg(altitude) - sinDeg(latitude) * sinDeg(declination)) /
                (cosDeg(latitude) * cosDeg(declination))
        return if (cosH in -1.0..1.0) {
            acosDeg(cosH) / 15.0
        } else {
            null
        }
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double = (a % 360.0 + 360.0) % 360.0
    private fun fixHour(h: Double): Double = (h % 24.0 + 24.0) % 24.0

    private fun sinDeg(deg: Double): Double = sin(Math.toRadians(deg))
    private fun cosDeg(deg: Double): Double = cos(Math.toRadians(deg))
    private fun tanDeg(deg: Double): Double = tan(Math.toRadians(deg))
    private fun asinDeg(x: Double): Double = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
    private fun acosDeg(x: Double): Double = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
    private fun atanDeg(x: Double): Double = Math.toDegrees(atan(x))
    private fun atan2Deg(y: Double, x: Double): Double = Math.toDegrees(kotlin.math.atan2(y, x))
}
