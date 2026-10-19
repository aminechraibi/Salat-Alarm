package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.dao.AudioAssetDao
import com.example.data.entity.AudioAssetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AudioAssetRepository(
    private val context: Context,
    private val dao: AudioAssetDao
) {
    val allAssetsFlow: Flow<List<AudioAssetEntity>> = dao.getAllAssetsFlow()

    suspend fun getAllAssets(): List<AudioAssetEntity> = dao.getAllAssets()

    suspend fun getAssetById(id: String): AudioAssetEntity? = dao.getAssetById(id)

    suspend fun importAudio(uri: Uri, displayName: String): AudioAssetEntity? = withContext(Dispatchers.IO) {
        try {
            val audioDir = File(context.filesDir, "custom_audio").apply { if (!exists()) mkdirs() }
            val newId = "imported_${UUID.randomUUID()}"
            val destFile = File(audioDir, "$newId.mp3")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            val entity = AudioAssetEntity(
                id = newId,
                displayNameArabic = displayName,
                displayNameEnglish = displayName,
                resourceName = null,
                filePath = destFile.absolutePath,
                durationSeconds = 5.0,
                isBundled = false,
                sourceDescription = "مستورد من الجهاز",
                licenseDescription = "ملف شخصي خاص بالمستخدم"
            )
            dao.insertOrUpdate(entity)
            entity
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteCustomAudio(asset: AudioAssetEntity) = withContext(Dispatchers.IO) {
        if (!asset.isBundled && asset.filePath != null) {
            val file = File(asset.filePath)
            if (file.exists()) file.delete()
        }
        dao.delete(asset)
    }

    suspend fun initializeBundledAssetsIfEmpty() = withContext(Dispatchers.IO) {
        val list = mutableListOf<AudioAssetEntity>()
        try {
            val jsonString = context.assets.open("audio_credits.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AudioAssetEntity(
                        id = obj.getString("id"),
                        displayNameArabic = obj.getString("title_arabic"),
                        displayNameEnglish = obj.getString("title_english"),
                        resourceName = obj.getString("resource_name"),
                        filePath = null,
                        durationSeconds = obj.optDouble("duration_seconds", 3.0),
                        isBundled = true,
                        sourceDescription = obj.optString("source", "Bundled"),
                        licenseDescription = obj.optString("license", "Open")
                    )
                )
            }
        } catch (e: Exception) {
            list.addAll(
                listOf(
                    AudioAssetEntity("takbir_makkah", "تكبير الحرم المكي (الشيخ علي ملا)", "Makkah Grand Mosque", "takbir_makkah", null, 7.24, true, "AlAdhan Authentic Audio", "Open"),
                    AudioAssetEntity("takbir_madinah", "تكبير المسجد النبوي (الشيخ عصام بخاري)", "Madinah Prophet Mosque", "takbir_madinah", null, 7.63, true, "AlAdhan Authentic Audio", "Open"),
                    AudioAssetEntity("takbir_alafasy", "تكبير بصوت مشاري العفاسي", "Mishary Alafasy", "takbir_alafasy", null, 7.44, true, "AlAdhan Audio", "Open"),
                    AudioAssetEntity("takbir_alaqsa", "تكبير المسجد الأقصى (القدس)", "Al-Aqsa Mosque", "takbir_alaqsa", null, 7.84, true, "AlAdhan Audio", "Open"),
                    AudioAssetEntity("takbir_abdulbasit", "تكبير الشيخ عبد الباسط عبد الصمد", "Sheikh Abdul Basit", "takbir_abdulbasit", null, 8.33, true, "AlAdhan Audio", "Open"),
                    AudioAssetEntity("reminder_fajr", "حان الآن وقت صلاة الفجر", "Fajr announcement", "reminder_fajr", null, 3.24, true, "Verified spoken reminder", "Open"),
                    AudioAssetEntity("reminder_chorouk", "حان الآن وقت شروق الشمس", "Sunrise announcement", "reminder_chorouk", null, 3.24, true, "Verified spoken reminder", "Open"),
                    AudioAssetEntity("reminder_dhuhr", "حان الآن وقت صلاة الظهر", "Dhuhr announcement", "reminder_dhuhr", null, 3.14, true, "Verified spoken reminder", "Open"),
                    AudioAssetEntity("reminder_asr", "حان الآن وقت صلاة العصر", "Asr announcement", "reminder_asr", null, 3.22, true, "Verified spoken reminder", "Open"),
                    AudioAssetEntity("reminder_maghrib", "حان الآن وقت صلاة المغرب", "Maghrib announcement", "reminder_maghrib", null, 3.24, true, "Verified spoken reminder", "Open"),
                    AudioAssetEntity("reminder_isha", "حان الآن وقت صلاة العشاء", "Isha announcement", "reminder_isha", null, 3.34, true, "Verified spoken reminder", "Open"),
                    AudioAssetEntity("reminder_generic", "تذكير: حان وقت الصلاة", "Generic prayer reminder", "reminder_generic", null, 3.43, true, "Verified spoken reminder", "Open"),
                    AudioAssetEntity("reminder_5min_generic", "تذكير: بقيت خمس دقائق على الصلاة", "5-min reminder", "reminder_5min_generic", null, 4.70, true, "Verified spoken reminder", "Open"),
                    AudioAssetEntity("reminder_iqama", "الصلاة خير من النوم (أذان الفجر)", "Fajr special reminder", "reminder_iqama", null, 2.57, true, "Verified spoken reminder", "Open"),
                    AudioAssetEntity("takbir_short", "الله أكبر، الله أكبر (تكبير هادئ)", "Takbir (Calm)", "takbir_short", null, 3.50, true, "Verified spoken reminder", "Open")
                )
            )
        }
        dao.insertAll(list)
    }
}
