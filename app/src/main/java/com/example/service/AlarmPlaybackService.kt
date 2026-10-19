package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.alarm.ScheduleReconciliation
import com.example.alarm.SnoozeManager
import com.example.data.AppDatabase
import com.example.data.entity.AlarmRuleEntity
import com.example.data.entity.OccurrenceStatus
import com.example.data.entity.ScheduledOccurrenceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class AlarmPlaybackService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var vibrator: Vibrator? = null
    private var currentOccurrenceId: String? = null
    private var playbackJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_PLAY -> {
                val occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: return START_NOT_STICKY
                val ruleId = intent.getStringExtra(EXTRA_RULE_ID)
                currentOccurrenceId = occurrenceId
                startAlarmSequence(occurrenceId, ruleId)
            }
            ACTION_SNOOZE -> {
                val occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: currentOccurrenceId
                if (occurrenceId != null) {
                    serviceScope.launch(Dispatchers.IO) {
                        SnoozeManager.snooze(applicationContext, occurrenceId)
                        stopSelf()
                    }
                } else {
                    stopSelf()
                }
            }
            ACTION_DISMISS, ACTION_STOP -> {
                val occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID) ?: currentOccurrenceId
                if (occurrenceId != null) {
                    serviceScope.launch(Dispatchers.IO) {
                        SnoozeManager.dismiss(applicationContext, occurrenceId)
                        ScheduleReconciliation.reconcile(applicationContext)
                        stopSelf()
                    }
                } else {
                    stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun startAlarmSequence(occurrenceId: String, ruleId: String?) {
        serviceScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val occurrence = db.occurrenceDao().getOccurrenceById(occurrenceId)
            val rule = if (ruleId != null) db.alarmRuleDao().getRuleById(ruleId) else null

            val prayerName = occurrence?.prayerType?.arabicName ?: "الصلاة"
            val notificationTitle = "حان وقت صلاة $prayerName"
            val notificationBody = rule?.getOffsetSummary() ?: "تذكير صوتي بمواقيت الصلاة"

            val notification = buildNotification(occurrenceId, notificationTitle, notificationBody)
            startForeground(NOTIFICATION_ID, notification)

            startAudioAndVibration(occurrence, rule)
        }
    }

    private fun startAudioAndVibration(
        occurrence: ScheduledOccurrenceEntity?,
        rule: AlarmRuleEntity?
    ) {
        playbackJob?.cancel()
        playbackJob = serviceScope.launch(Dispatchers.IO) {
            try {
                // Request audio focus
                requestAudioFocus()

                // Start vibration if enabled
                if (rule?.vibrationEnabled != false) {
                    startVibration()
                }

                val db = AppDatabase.getInstance(applicationContext)
                val assetId = rule?.audioAssetId ?: "reminder_generic"
                val audioAsset = db.audioAssetDao().getAssetById(assetId)

                val repeatCount = (rule?.audioRepeatCount ?: 1).coerceIn(1, 5)
                val repeatIntervalSec = (rule?.audioRepeatIntervalSec ?: 5).coerceIn(2, 30)
                val maxDurationSec = (rule?.maxAudioDurationSec ?: 45).coerceIn(10, 120)
                val volume = (rule?.volumePercent ?: 100).toFloat() / 100f

                val startTime = System.currentTimeMillis()

                for (i in 1..repeatCount) {
                    // Check if max duration expired
                    if ((System.currentTimeMillis() - startTime) >= maxDurationSec * 1000L) {
                        Log.d(TAG, "Max audio safety duration reached ($maxDurationSec s)")
                        break
                    }

                    playClip(audioAsset?.resourceName, audioAsset?.filePath, volume)

                    // If more repetitions remain, wait the configured interval
                    if (i < repeatCount) {
                        delay(repeatIntervalSec * 1000L)
                    }
                }

                // Finished repetition normally
                stopVibration()
                abandonAudioFocus()

                // Mark occurrence as COMPLETED
                occurrence?.let {
                    db.occurrenceDao().updateStatus(it.occurrenceId, OccurrenceStatus.COMPLETED)
                }
                ScheduleReconciliation.reconcile(applicationContext)

            } catch (e: Exception) {
                Log.e(TAG, "Error in audio playback sequence", e)
            } finally {
                stopVibration()
                abandonAudioFocus()
            }
        }
    }

    private suspend fun playClip(resourceName: String?, filePath: String?, volume: Float) {
        val completedJob = Job()
        val player = MediaPlayer()

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            player.setAudioAttributes(audioAttributes)
            player.setVolume(volume, volume)

            if (!filePath.isNullOrBlank() && File(filePath).exists()) {
                player.setDataSource(filePath)
            } else {
                val resName = resourceName ?: "reminder_generic"
                val resId = resources.getIdentifier(resName, "raw", packageName)
                if (resId != 0) {
                    val afd = resources.openRawResourceFd(resId)
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                } else {
                    Log.e(TAG, "Audio resource $resName not found in raw")
                    completedJob.complete()
                    return
                }
            }

            player.setOnCompletionListener {
                completedJob.complete()
            }
            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                completedJob.complete()
                true
            }

            player.prepare()
            mediaPlayer = player
            player.start()

            completedJob.join()

        } catch (e: Exception) {
            Log.e(TAG, "Failed playing audio clip", e)
        } finally {
            try {
                player.reset()
                player.release()
            } catch (e: Exception) {
                // Ignore
            }
            mediaPlayer = null
        }
    }

    private fun requestAudioFocus() {
        val audioManager = this.audioManager ?: return
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stopSelf()
                    }
                }
                .build()
            audioFocusRequest = req
            audioManager.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stopSelf()
                    }
                },
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 500, 300, 500, 300, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun buildNotification(occurrenceId: String, title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmPlaybackService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_OCCURRENCE_ID, occurrenceId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmPlaybackService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_OCCURRENCE_ID, occurrenceId)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.salat_alarm_icon_1792351898782)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(R.drawable.salat_alarm_icon_1792351898782, "إيقاف", dismissPendingIntent)
            .addAction(R.drawable.salat_alarm_icon_1792351898782, "غفوة (5د)", snoozePendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تنبيهات مواقيت الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات التذكير الصوتي بمواقيت الصلاة"
                setSound(null, null) // Sound is handled directly by MediaPlayer
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
        stopVibration()
        abandonAudioFocus()
    }

    companion object {
        private const val TAG = "AlarmPlaybackService"
        const val CHANNEL_ID = "prayer_alarm_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.service.ACTION_PLAY"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_DISMISS = "com.example.service.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.example.service.ACTION_SNOOZE"

        const val EXTRA_OCCURRENCE_ID = "extra_occurrence_id"
        const val EXTRA_RULE_ID = "extra_rule_id"
    }
}
