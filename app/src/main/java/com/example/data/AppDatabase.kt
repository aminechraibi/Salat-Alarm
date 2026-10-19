package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AlarmRuleDao
import com.example.data.dao.AudioAssetDao
import com.example.data.dao.OccurrenceDao
import com.example.data.dao.PrayerSettingsDao
import com.example.data.entity.AlarmRuleEntity
import com.example.data.entity.AudioAssetEntity
import com.example.data.entity.PrayerSettingsEntity
import com.example.data.entity.ScheduledOccurrenceEntity

@Database(
    entities = [
        PrayerSettingsEntity::class,
        AlarmRuleEntity::class,
        AudioAssetEntity::class,
        ScheduledOccurrenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerSettingsDao(): PrayerSettingsDao
    abstract fun alarmRuleDao(): AlarmRuleDao
    abstract fun audioAssetDao(): AudioAssetDao
    abstract fun occurrenceDao(): OccurrenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prayer_reminder.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
