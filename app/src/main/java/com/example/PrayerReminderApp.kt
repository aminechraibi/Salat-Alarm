package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.example.alarm.ScheduleReconciliation
import com.example.data.AppDatabase
import com.example.data.repository.AlarmRuleRepository
import com.example.data.repository.AudioAssetRepository
import com.example.data.repository.PrayerSettingsRepository
import com.example.service.AlarmPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrayerReminderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val audioRepo = AudioAssetRepository(applicationContext, db.audioAssetDao())
                val rulesRepo = AlarmRuleRepository(db.alarmRuleDao())
                val settingsRepo = PrayerSettingsRepository(db.prayerSettingsDao())

                // Ensure default settings exist
                settingsRepo.getSettings()

                // Ensure default bundled short Arabic audio assets exist
                audioRepo.initializeBundledAssetsIfEmpty()

                // Ensure default prayer alarm rules exist
                rulesRepo.initializeDefaultRulesIfEmpty()

                // Initial schedule reconciliation
                ScheduleReconciliation.reconcile(applicationContext)
                Log.i("PrayerReminderApp", "Initialization completed successfully")
            } catch (e: Exception) {
                Log.e("PrayerReminderApp", "Error during app initialization", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AlarmPlaybackService.CHANNEL_ID,
                getString(R.string.notif_channel_alarms),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notif_channel_alarms_desc)
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
