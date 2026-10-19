package com.example

import com.example.data.entity.AlarmRuleEntity
import com.example.model.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmRuleAndSnoozeTest {

    @Test
    fun testWeekdayBitmask() {
        // Monday = 1 (bit 0), Friday = 5 (bit 4), Sunday = 7 (bit 6)
        // Bitmask for Mon + Fri only: (1 shl 0) or (1 shl 4) = 1 + 16 = 17
        val rule = AlarmRuleEntity(
            targetPrayer = PrayerType.FAJR,
            repeatDaysMask = 17
        )

        assertTrue(rule.isDayEnabled(1)) // Monday
        assertFalse(rule.isDayEnabled(2)) // Tuesday
        assertFalse(rule.isDayEnabled(3)) // Wednesday
        assertFalse(rule.isDayEnabled(4)) // Thursday
        assertTrue(rule.isDayEnabled(5)) // Friday
        assertFalse(rule.isDayEnabled(6)) // Saturday
        assertFalse(rule.isDayEnabled(7)) // Sunday
    }

    @Test
    fun testEverydayBitmask() {
        val rule = AlarmRuleEntity(
            targetPrayer = PrayerType.DHUHR,
            repeatDaysMask = 127
        )

        for (i in 1..7) {
            assertTrue("Day $i should be enabled", rule.isDayEnabled(i))
        }
    }

    @Test
    fun testOffsetSummaryText() {
        val ruleAt = AlarmRuleEntity(targetPrayer = PrayerType.FAJR, offsetMinutes = 0)
        assertEquals("في وقت الأذان (0 د)", ruleAt.getOffsetSummary())

        val ruleBefore = AlarmRuleEntity(targetPrayer = PrayerType.FAJR, offsetMinutes = -10)
        assertEquals("قبل الأذان بـ 10 دقيقة", ruleBefore.getOffsetSummary())

        val ruleAfter = AlarmRuleEntity(targetPrayer = PrayerType.DHUHR, offsetMinutes = 15)
        assertEquals("بعد الأذان بـ 15 دقيقة", ruleAfter.getOffsetSummary())
    }
}
