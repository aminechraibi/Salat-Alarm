package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.AlarmRuleEntity
import com.example.data.entity.OccurrenceStatus
import com.example.data.entity.OccurrenceType
import com.example.data.entity.PrayerSettingsEntity
import com.example.data.entity.ScheduledOccurrenceEntity
import com.example.model.PrayerType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Salat Alarm", appName)
    }

    @Test
    fun testPrayerSettingsDatabasePersistence() = runBlocking {
        val initial = db.prayerSettingsDao().getSettings()
        // Insert custom settings
        val custom = PrayerSettingsEntity(
            cityNameArabic = "الرباط",
            cityNameEnglish = "Rabat",
            latitude = 34.0209,
            longitude = -6.8416
        )
        db.prayerSettingsDao().insertOrUpdate(custom)

        val retrieved = db.prayerSettingsDao().getSettings()
        assertNotNull(retrieved)
        assertEquals("الرباط", retrieved?.cityNameArabic)
        assertEquals(34.0209, retrieved?.latitude ?: 0.0, 0.0001)
    }

    @Test
    fun testAlarmRulesDatabaseCRUD() = runBlocking {
        val rule = AlarmRuleEntity(
            ruleId = "test_fajr_10",
            targetPrayer = PrayerType.FAJR,
            offsetMinutes = -10,
            audioAssetId = "takbir_short",
            label = "Early Fajr"
        )
        db.alarmRuleDao().insertOrUpdate(rule)

        val retrieved = db.alarmRuleDao().getRuleById("test_fajr_10")
        assertNotNull(retrieved)
        assertEquals(-10, retrieved?.offsetMinutes)
        assertEquals(PrayerType.FAJR, retrieved?.targetPrayer)

        // Toggle rule
        db.alarmRuleDao().setRuleEnabled("test_fajr_10", false)
        val updated = db.alarmRuleDao().getRuleById("test_fajr_10")
        assertEquals(false, updated?.isEnabled)

        // Delete rule
        db.alarmRuleDao().deleteById("test_fajr_10")
        val deleted = db.alarmRuleDao().getRuleById("test_fajr_10")
        assertEquals(null, deleted)
    }

    @Test
    fun testScheduledOccurrenceLifecycle() = runBlocking {
        val occurrence = ScheduledOccurrenceEntity(
            occurrenceId = "occ_123",
            ruleId = "rule_abc",
            prayerType = PrayerType.DHUHR,
            targetDate = "2026-10-18",
            scheduledTimestamp = System.currentTimeMillis() + 3600_000,
            occurrenceType = OccurrenceType.NORMAL,
            status = OccurrenceStatus.SCHEDULED
        )
        db.occurrenceDao().insertOrUpdate(occurrence)

        val found = db.occurrenceDao().getOccurrenceById("occ_123")
        assertNotNull(found)
        assertEquals(OccurrenceStatus.SCHEDULED, found?.status)

        // Status transition to FIRING then SNOOZED
        db.occurrenceDao().updateStatus("occ_123", OccurrenceStatus.FIRING)
        assertEquals(OccurrenceStatus.FIRING, db.occurrenceDao().getOccurrenceById("occ_123")?.status)

        db.occurrenceDao().updateStatus("occ_123", OccurrenceStatus.SNOOZED)
        assertEquals(OccurrenceStatus.SNOOZED, db.occurrenceDao().getOccurrenceById("occ_123")?.status)
    }

    @Test
    fun testBundledAudioResourcePresent() {
        // Verify that bundled raw resource takbir_short exists and is decodable
        val resId = context.resources.getIdentifier("takbir_short", "raw", context.packageName)
        assertTrue("takbir_short must exist in res/raw", resId != 0)

        val afd = context.resources.openRawResourceFd(resId)
        assertNotNull("Raw audio file descriptor must be non-null", afd)
        assertTrue("Audio file must have content length > 0", afd.length > 1000)
        afd.close()
    }
}
