package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AlarmRuleEntity
import com.example.model.PrayerType
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmRuleDao {
    @Query("SELECT * FROM alarm_rules ORDER BY targetPrayer, offsetMinutes ASC")
    fun getAllRulesFlow(): Flow<List<AlarmRuleEntity>>

    @Query("SELECT * FROM alarm_rules ORDER BY targetPrayer, offsetMinutes ASC")
    suspend fun getAllRules(): List<AlarmRuleEntity>

    @Query("SELECT * FROM alarm_rules WHERE isEnabled = 1")
    suspend fun getEnabledRules(): List<AlarmRuleEntity>

    @Query("SELECT * FROM alarm_rules WHERE ruleId = :ruleId LIMIT 1")
    suspend fun getRuleById(ruleId: String): AlarmRuleEntity?

    @Query("SELECT * FROM alarm_rules WHERE targetPrayer = :prayer ORDER BY offsetMinutes ASC")
    fun getRulesForPrayerFlow(prayer: PrayerType): Flow<List<AlarmRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: AlarmRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<AlarmRuleEntity>)

    @Update
    suspend fun update(rule: AlarmRuleEntity)

    @Query("UPDATE alarm_rules SET isEnabled = :enabled WHERE ruleId = :ruleId")
    suspend fun setRuleEnabled(ruleId: String, enabled: Boolean)

    @Delete
    suspend fun delete(rule: AlarmRuleEntity)

    @Query("DELETE FROM alarm_rules WHERE ruleId = :ruleId")
    suspend fun deleteById(ruleId: String)
}
