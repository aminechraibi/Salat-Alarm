package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AndroidAlarmScheduler
import com.example.alarm.ScheduleReconciliation
import com.example.calc.PrayerTimeCalculator
import com.example.data.AppDatabase
import com.example.data.entity.AlarmRuleEntity
import com.example.data.entity.AudioAssetEntity
import com.example.data.entity.OccurrenceStatus
import com.example.data.entity.PrayerSettingsEntity
import com.example.data.entity.ScheduledOccurrenceEntity
import com.example.data.repository.AlarmRuleRepository
import com.example.data.repository.AudioAssetRepository
import com.example.data.repository.OccurrenceRepository
import com.example.data.repository.PrayerSettingsRepository
import com.example.model.CityPreset
import com.example.model.PrayerTimesDay
import com.example.model.PrayerType
import com.example.service.AlarmPlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class NextPrayerInfo(
    val prayerType: PrayerType,
    val prayerTime: ZonedDateTime,
    val remainingHours: Long,
    val remainingMinutes: Long,
    val remainingSeconds: Long,
    val formattedTime: String
)

data class UiState(
    val settings: PrayerSettingsEntity = PrayerSettingsEntity(),
    val todayPrayerTimes: PrayerTimesDay? = null,
    val nextPrayer: NextPrayerInfo? = null,
    val rules: List<AlarmRuleEntity> = emptyList(),
    val audioAssets: List<AudioAssetEntity> = emptyList(),
    val upcomingOccurrences: List<ScheduledOccurrenceEntity> = emptyList(),
    val previewingAssetId: String? = null,
    val isPlayingPreview: Boolean = false,
    val previewVolume: Float = 1.0f,
    val isGpsLoading: Boolean = false,
    val gpsMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val settingsRepo = PrayerSettingsRepository(db.prayerSettingsDao())
    private val rulesRepo = AlarmRuleRepository(db.alarmRuleDao())
    private val audioRepo = AudioAssetRepository(application, db.audioAssetDao())
    private val occurrenceRepo = OccurrenceRepository(db.occurrenceDao())
    private val scheduler = AndroidAlarmScheduler(application)

    private val _previewingAssetId = MutableStateFlow<String?>(null)
    private val _isPlayingPreview = MutableStateFlow(false)
    private val _previewVolume = MutableStateFlow(1.0f)
    private var previewMediaPlayer: MediaPlayer? = null

    private val _isGpsLoading = MutableStateFlow(false)
    private val _gpsMessage = MutableStateFlow<String?>(null)

    private val _currentTimeSeconds = MutableStateFlow(System.currentTimeMillis() / 1000)

    val settings: StateFlow<PrayerSettingsEntity?> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rules: StateFlow<List<AlarmRuleEntity>> = rulesRepo.allRulesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val audioAssets: StateFlow<List<AudioAssetEntity>> = audioRepo.allAssetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingOccurrences: StateFlow<List<ScheduledOccurrenceEntity>> = occurrenceRepo.getUpcomingOccurrencesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<UiState> = combine(
        settings,
        rules,
        audioAssets,
        upcomingOccurrences,
        _previewingAssetId,
        _isPlayingPreview,
        _previewVolume,
        _currentTimeSeconds,
        _isGpsLoading,
        _gpsMessage
    ) { params ->
        val currentSettings = params[0] as? PrayerSettingsEntity ?: PrayerSettingsEntity()
        @Suppress("UNCHECKED_CAST")
        val currentRules = params[1] as List<AlarmRuleEntity>
        @Suppress("UNCHECKED_CAST")
        val currentAudio = params[2] as List<AudioAssetEntity>
        @Suppress("UNCHECKED_CAST")
        val currentOccurrences = params[3] as List<ScheduledOccurrenceEntity>
        val previewId = params[4] as? String
        val isPlaying = params[5] as Boolean
        val previewVol = params[6] as Float
        val gpsLoading = params[8] as Boolean
        val gpsMsg = params[9] as? String

        val zoneId = try { ZoneId.of(currentSettings.timeZoneId) } catch (e: Exception) { ZoneId.systemDefault() }
        val today = LocalDate.now(zoneId)

        val calcParams = PrayerTimeCalculator.CalculationParameters(
            latitude = currentSettings.latitude,
            longitude = currentSettings.longitude,
            altitudeMeters = currentSettings.altitudeMeters,
            zoneId = zoneId,
            method = currentSettings.method,
            customFajrAngle = currentSettings.customFajrAngle,
            customIshaAngle = currentSettings.customIshaAngle,
            asrMadhhab = currentSettings.asrMadhhab,
            highLatitudeRule = currentSettings.highLatitudeRule,
            fajrAdjustmentMinutes = currentSettings.fajrAdjustment,
            sunriseAdjustmentMinutes = currentSettings.sunriseAdjustment,
            dhuhrAdjustmentMinutes = currentSettings.dhuhrAdjustment,
            asrAdjustmentMinutes = currentSettings.asrAdjustment,
            maghribAdjustmentMinutes = currentSettings.maghribAdjustment,
            ishaAdjustmentMinutes = currentSettings.ishaAdjustment
        )

        val todayTimes = PrayerTimeCalculator.calculate(today, calcParams)
        val tomorrowTimes = PrayerTimeCalculator.calculate(today.plusDays(1), calcParams)

        val nextPrayer = calculateNextPrayer(todayTimes, tomorrowTimes, currentSettings.is24HourFormat)

        UiState(
            settings = currentSettings,
            todayPrayerTimes = todayTimes,
            nextPrayer = nextPrayer,
            rules = currentRules,
            audioAssets = currentAudio,
            upcomingOccurrences = currentOccurrences,
            previewingAssetId = previewId,
            isPlayingPreview = isPlaying,
            previewVolume = previewVol,
            isGpsLoading = gpsLoading,
            gpsMessage = gpsMsg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    init {
        // Clock tick for countdown
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                _currentTimeSeconds.value = System.currentTimeMillis() / 1000
                delay(1000)
            }
        }
    }

    private fun calculateNextPrayer(
        today: PrayerTimesDay,
        tomorrow: PrayerTimesDay,
        is24Hour: Boolean
    ): NextPrayerInfo? {
        val now = ZonedDateTime.now(today.fajr.zone)
        val allTimes = today.getAllTimes() + tomorrow.getAllTimes()

        val next = allTimes.firstOrNull { it.second.isAfter(now) } ?: return null
        val duration = Duration.between(now, next.second)

        val hours = duration.toHours()
        val minutes = (duration.toMinutes() % 60)
        val seconds = (duration.seconds % 60)

        val formatter = if (is24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
        val formattedTime = next.second.format(formatter)

        return NextPrayerInfo(
            prayerType = next.first,
            prayerTime = next.second,
            remainingHours = hours,
            remainingMinutes = minutes,
            remainingSeconds = seconds,
            formattedTime = formattedTime
        )
    }

    fun toggleRule(ruleId: String, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            rulesRepo.setRuleEnabled(ruleId, enabled)
            if (!enabled) {
                ScheduleReconciliation.cancelRuleOccurrences(getApplication(), ruleId, scheduler)
            }
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun saveRule(rule: AlarmRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            rulesRepo.saveRule(rule)
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun addQuickAlarmForPrayer(
        prayer: PrayerType,
        offsetMinutes: Int,
        audioAssetId: String? = null,
        label: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val audioId = audioAssetId ?: when (prayer) {
                PrayerType.FAJR -> "takbir_makkah"
                PrayerType.CHOROUK -> "reminder_chorouk"
                PrayerType.DHUHR -> "takbir_madinah"
                PrayerType.ASR -> "takbir_alafasy"
                PrayerType.MAGHRIB -> "takbir_alaqsa"
                PrayerType.ISHA -> "takbir_abdulbasit"
            }
            val rule = AlarmRuleEntity(
                targetPrayer = prayer,
                offsetMinutes = offsetMinutes,
                audioAssetId = audioId,
                label = label ?: if (offsetMinutes == 0) "وقت صلاة ${prayer.arabicName}" else "${prayer.arabicName} ($offsetMinutes د)"
            )
            rulesRepo.saveRule(rule)
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun addAllPrayersAlarms(offsetMinutes: Int, audioAssetId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val rulesToAdd = PrayerType.entries.map { prayer ->
                val audioId = audioAssetId ?: when (prayer) {
                    PrayerType.FAJR -> "takbir_makkah"
                    PrayerType.CHOROUK -> "reminder_chorouk"
                    PrayerType.DHUHR -> "takbir_madinah"
                    PrayerType.ASR -> "takbir_alafasy"
                    PrayerType.MAGHRIB -> "takbir_alaqsa"
                    PrayerType.ISHA -> "takbir_abdulbasit"
                }
                AlarmRuleEntity(
                    targetPrayer = prayer,
                    offsetMinutes = offsetMinutes,
                    audioAssetId = audioId,
                    label = if (offsetMinutes == 0) "وقت ${prayer.arabicName}" else "قبل ${prayer.arabicName} بـ ${kotlin.math.abs(offsetMinutes)}د"
                )
            }
            rulesRepo.saveAll(rulesToAdd)
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun applyRuleToAllPrayers(baseRule: AlarmRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val rulesToAdd = PrayerType.entries.map { prayer ->
                baseRule.copy(
                    ruleId = java.util.UUID.randomUUID().toString(),
                    targetPrayer = prayer,
                    label = if (baseRule.label.isNotBlank()) "${baseRule.label} (${prayer.arabicName})" else prayer.arabicName
                )
            }
            rulesRepo.saveAll(rulesToAdd)
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun duplicateRule(rule: AlarmRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            rulesRepo.duplicateRule(rule)
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun deleteRule(rule: AlarmRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            ScheduleReconciliation.cancelRuleOccurrences(getApplication(), rule.ruleId, scheduler)
            rulesRepo.deleteRule(rule)
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun updateSettings(newSettings: PrayerSettingsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.updateSettings(newSettings)
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun selectCityPreset(city: CityPreset) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settingsRepo.getSettings()
            val updated = current.copy(
                cityNameArabic = city.cityNameArabic,
                cityNameEnglish = city.cityNameEnglish,
                countryArabic = city.countryArabic,
                countryEnglish = city.countryEnglish,
                latitude = city.latitude,
                longitude = city.longitude,
                altitudeMeters = city.altitudeMeters,
                timeZoneId = city.timeZoneId,
                method = city.recommendedMethod,
                isAutoLocation = false
            )
            settingsRepo.updateSettings(updated)
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun updateLocationManual(
        cityName: String,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        timeZoneId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settingsRepo.getSettings()
            val updated = current.copy(
                cityNameArabic = cityName,
                cityNameEnglish = cityName,
                latitude = latitude,
                longitude = longitude,
                altitudeMeters = altitude,
                timeZoneId = timeZoneId,
                isAutoLocation = false
            )
            settingsRepo.updateSettings(updated)
            ScheduleReconciliation.reconcile(getApplication(), scheduler)
        }
    }

    fun setPreviewVolume(volume: Float) {
        _previewVolume.value = volume
        previewMediaPlayer?.setVolume(volume, volume)
    }

    fun playAudioPreview(asset: AudioAssetEntity) {
        if (_previewingAssetId.value == asset.id && _isPlayingPreview.value) {
            stopAudioPreview()
            return
        }

        stopAudioPreview()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val player = MediaPlayer()
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                player.setAudioAttributes(audioAttributes)
                val vol = _previewVolume.value
                player.setVolume(vol, vol)

                if (!asset.filePath.isNullOrBlank() && File(asset.filePath).exists()) {
                    player.setDataSource(asset.filePath)
                } else {
                    val resName = asset.resourceName ?: "reminder_generic"
                    val resId = getApplication<Application>().resources.getIdentifier(resName, "raw", getApplication<Application>().packageName)
                    if (resId != 0) {
                        val afd = getApplication<Application>().resources.openRawResourceFd(resId)
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                    } else {
                        Log.e("MainViewModel", "Resource $resName not found")
                        return@launch
                    }
                }

                player.setOnCompletionListener {
                    stopAudioPreview()
                }
                player.setOnErrorListener { _, _, _ ->
                    stopAudioPreview()
                    true
                }

                player.prepare()
                previewMediaPlayer = player
                player.start()

                _previewingAssetId.value = asset.id
                _isPlayingPreview.value = true

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error previewing audio", e)
                stopAudioPreview()
            }
        }
    }

    fun stopAudioPreview() {
        try {
            previewMediaPlayer?.stop()
            previewMediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        previewMediaPlayer = null
        _isPlayingPreview.value = false
        _previewingAssetId.value = null
    }

    fun importCustomAudio(uri: Uri, displayName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            audioRepo.importAudio(uri, displayName)
        }
    }

    fun triggerInstantTestAlarm(prayerType: PrayerType = PrayerType.FAJR) {
        viewModelScope.launch(Dispatchers.IO) {
            val intent = Intent(getApplication(), AlarmPlaybackService::class.java).apply {
                action = AlarmPlaybackService.ACTION_PLAY
                putExtra(AlarmPlaybackService.EXTRA_OCCURRENCE_ID, "test_instant_${System.currentTimeMillis()}")
                putExtra(AlarmPlaybackService.EXTRA_RULE_ID, "test_rule")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplication<Application>().startForegroundService(intent)
            } else {
                getApplication<Application>().startService(intent)
            }
        }
    }

    fun fetchGpsLocation() {
        if (_isGpsLoading.value) return
        _isGpsLoading.value = true
        _gpsMessage.value = "جاري البحث عن موقع GPS بدقة..."

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val detected = com.example.util.LocationHelper.getFreshLocation(getApplication())
                if (detected != null) {
                    val current = settingsRepo.getSettings()
                    val isMorocco = detected.countryArabic.contains("المغرب") ||
                            detected.countryEnglish.contains("Morocco", ignoreCase = true) ||
                            detected.timeZoneId == "Africa/Casablanca"
                    val updated = current.copy(
                        cityNameArabic = detected.cityNameArabic,
                        cityNameEnglish = detected.cityNameEnglish,
                        countryArabic = detected.countryArabic,
                        countryEnglish = detected.countryEnglish,
                        latitude = detected.latitude,
                        longitude = detected.longitude,
                        altitudeMeters = detected.altitudeMeters,
                        timeZoneId = detected.timeZoneId,
                        method = if (isMorocco) com.example.model.CalculationMethod.MOROCCO else current.method,
                        isAutoLocation = true
                    )
                    settingsRepo.updateSettings(updated)
                    ScheduleReconciliation.reconcile(getApplication(), scheduler)
                    _isGpsLoading.value = false
                    val latStr = String.format(java.util.Locale.US, "%.4f", detected.latitude)
                    val lngStr = String.format(java.util.Locale.US, "%.4f", detected.longitude)
                    _gpsMessage.value = "تم تحديد الموقع بنجاح: ${detected.cityNameArabic} ($latStr, $lngStr)"
                } else {
                    _isGpsLoading.value = false
                    val enabled = com.example.util.LocationHelper.isLocationEnabled(getApplication())
                    _gpsMessage.value = if (!enabled) {
                        "خدمة الموقع (GPS) معطلة على الجهاز. يرجى تفعيل الموقع في الإعدادات."
                    } else {
                        "تعذر تحديد إحداثيات GPS بدقة. يرجى التأكد من التواجد في مكان مفتوح أو اختيار المدينة يدوياً من القائمة."
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching GPS location", e)
                _isGpsLoading.value = false
                _gpsMessage.value = "حدث خطأ أثناء تحديد الموقع: ${e.localizedMessage ?: "خطأ غير معروف"}"
            }
        }
    }

    fun clearGpsMessage() {
        _gpsMessage.value = null
    }

    fun canScheduleExactAlarms(): Boolean {
        return scheduler.canScheduleExact()
    }

    override fun onCleared() {
        super.onCleared()
        stopAudioPreview()
    }
}
