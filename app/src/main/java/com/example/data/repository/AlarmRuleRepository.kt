package com.example.data.repository

import com.example.data.dao.AlarmRuleDao
import com.example.data.entity.AlarmRuleEntity
import com.example.model.PrayerType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AlarmRuleRepository(private val dao: AlarmRuleDao) {
    val allRulesFlow: Flow<List<AlarmRuleEntity>> = dao.getAllRulesFlow()

    suspend fun getAllRules(): List<AlarmRuleEntity> = dao.getAllRules()

    suspend fun getEnabledRules(): List<AlarmRuleEntity> = dao.getEnabledRules()

    suspend fun getRuleById(id: String): AlarmRuleEntity? = dao.getRuleById(id)

    fun getRulesForPrayerFlow(prayer: PrayerType): Flow<List<AlarmRuleEntity>> = dao.getRulesForPrayerFlow(prayer)

    suspend fun saveRule(rule: AlarmRuleEntity) = dao.insertOrUpdate(rule)

    suspend fun saveAll(rules: List<AlarmRuleEntity>) = dao.insertAll(rules)

    suspend fun setRuleEnabled(ruleId: String, enabled: Boolean) = dao.setRuleEnabled(ruleId, enabled)

    suspend fun duplicateRule(rule: AlarmRuleEntity): AlarmRuleEntity {
        val newRule = rule.copy(
            ruleId = UUID.randomUUID().toString(),
            label = if (rule.label.isNotBlank()) "${rule.label} (نسخة)" else "نسخة"
        )
        dao.insertOrUpdate(newRule)
        return newRule
    }

    suspend fun deleteRule(rule: AlarmRuleEntity) = dao.delete(rule)

    suspend fun deleteRuleById(ruleId: String) = dao.deleteById(ruleId)

    suspend fun initializeDefaultRulesIfEmpty() {
        val existing = dao.getAllRules()
        if (existing.isEmpty()) {
            val defaults = listOf(
                AlarmRuleEntity(
                    targetPrayer = PrayerType.FAJR,
                    offsetMinutes = -10,
                    audioAssetId = "takbir_short",
                    label = "تنبيه مبكر لصلاة الفجر"
                ),
                AlarmRuleEntity(
                    targetPrayer = PrayerType.FAJR,
                    offsetMinutes = 0,
                    audioAssetId = "reminder_fajr",
                    label = "وقت صلاة الفجر"
                ),
                AlarmRuleEntity(
                    targetPrayer = PrayerType.CHOROUK,
                    offsetMinutes = 0,
                    audioAssetId = "reminder_chorouk",
                    label = "وقت الشروق"
                ),
                AlarmRuleEntity(
                    targetPrayer = PrayerType.DHUHR,
                    offsetMinutes = 0,
                    audioAssetId = "reminder_dhuhr",
                    label = "وقت صلاة الظهر"
                ),
                AlarmRuleEntity(
                    targetPrayer = PrayerType.ASR,
                    offsetMinutes = 0,
                    audioAssetId = "reminder_asr",
                    label = "وقت صلاة العصر"
                ),
                AlarmRuleEntity(
                    targetPrayer = PrayerType.MAGHRIB,
                    offsetMinutes = 0,
                    audioAssetId = "reminder_maghrib",
                    label = "وقت صلاة المغرب"
                ),
                AlarmRuleEntity(
                    targetPrayer = PrayerType.ISHA,
                    offsetMinutes = 0,
                    audioAssetId = "reminder_isha",
                    label = "وقت صلاة العشاء"
                )
            )
            dao.insertAll(defaults)
        }
    }
}
