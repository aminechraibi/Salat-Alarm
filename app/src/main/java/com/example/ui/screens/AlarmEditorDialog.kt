package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.AlarmRuleEntity
import com.example.data.entity.AudioAssetEntity
import com.example.model.PrayerType
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorDialog(
    initialRule: AlarmRuleEntity?,
    defaultPrayer: PrayerType?,
    audioAssets: List<AudioAssetEntity>,
    previewingAssetId: String?,
    isPlayingPreview: Boolean,
    onPlayPreview: (AudioAssetEntity) -> Unit,
    onStopPreview: () -> Unit,
    onSave: (AlarmRuleEntity, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPrayer by remember { mutableStateOf(initialRule?.targetPrayer ?: defaultPrayer ?: PrayerType.FAJR) }
    var offsetMinutes by remember { mutableIntStateOf(initialRule?.offsetMinutes ?: 0) }
    var selectedAudioId by remember {
        mutableStateOf(
            initialRule?.audioAssetId ?: when (selectedPrayer) {
                PrayerType.FAJR -> "takbir_makkah"
                PrayerType.CHOROUK -> "reminder_chorouk"
                PrayerType.DHUHR -> "takbir_madinah"
                PrayerType.ASR -> "takbir_alafasy"
                PrayerType.MAGHRIB -> "takbir_alaqsa"
                PrayerType.ISHA -> "takbir_abdulbasit"
            }
        )
    }
    var repeatDaysMask by remember { mutableIntStateOf(initialRule?.repeatDaysMask ?: 127) }
    var audioRepeatCount by remember { mutableIntStateOf(initialRule?.audioRepeatCount ?: 1) }
    var audioRepeatIntervalSec by remember { mutableIntStateOf(initialRule?.audioRepeatIntervalSec ?: 5) }
    var snoozeEnabled by remember { mutableStateOf(initialRule?.snoozeEnabled ?: true) }
    var snoozeDurationMinutes by remember { mutableIntStateOf(initialRule?.snoozeDurationMinutes ?: 5) }
    var maxSnoozeCount by remember { mutableIntStateOf(initialRule?.maxSnoozeCount ?: 3) }
    var volumePercent by remember { mutableIntStateOf(initialRule?.volumePercent ?: 100) }
    var vibrationEnabled by remember { mutableStateOf(initialRule?.vibrationEnabled ?: true) }
    var label by remember { mutableStateOf(initialRule?.label ?: "") }
    var applyToAllPrayers by remember { mutableStateOf(false) }

    var audioDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            onStopPreview()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialRule == null) "إضافة تنبيه جديد" else "تعديل التنبيه",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                            onStopPreview()
                            onDismiss()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Target Prayer Selection
                Text(
                    text = "الصلاة المستهدفة",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val prayers = PrayerType.entries
                    prayers.take(3).forEach { prayer ->
                        FilterChip(
                            selected = selectedPrayer == prayer,
                            onClick = { selectedPrayer = prayer },
                            label = { Text(prayer.arabicName, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val prayers = PrayerType.entries
                    prayers.drop(3).forEach { prayer ->
                        FilterChip(
                            selected = selectedPrayer == prayer,
                            onClick = { selectedPrayer = prayer },
                            label = { Text(prayer.arabicName, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Exact Minute Offset from Adhan (Range -60 to +60 min, middle is 0)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تحديد وقت التنبيه (بالدقائق):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            offsetMinutes == 0 -> EmeraldPrimary
                                            offsetMinutes < 0 -> Color(0xFFC05621) // Amber for before
                                            else -> Color(0xFF2B6CB0) // Blue for after
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when {
                                        offsetMinutes == 0 -> "0 د • في وقت الأذان"
                                        offsetMinutes < 0 -> "$offsetMinutes د • قبل الأذان"
                                        else -> "+$offsetMinutes د • بعد الأذان"
                                    },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Descriptive explanation
                        Text(
                            text = when {
                                offsetMinutes == 0 -> "التنبيه سيعمل في نفس دقيقة أذان صلاة ${selectedPrayer.arabicName} تماماً (الوسط = 0)."
                                offsetMinutes < 0 -> "التنبيه سيعمل قبل أذان ${selectedPrayer.arabicName} بـ ${kotlin.math.abs(offsetMinutes)} دقيقة (سالب)."
                                else -> "التنبيه سيعمل بعد أذان ${selectedPrayer.arabicName} بـ $offsetMinutes دقيقة — وقت الإقامة (موجب)."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stepper buttons for exact minute adjustment (-5, -1, 0, +1, +5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { offsetMinutes = (offsetMinutes - 5).coerceIn(-60, 60) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("-5د", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { offsetMinutes = (offsetMinutes - 1).coerceIn(-60, 60) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("-1د", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { offsetMinutes = 0 },
                                modifier = Modifier.weight(1.4f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (offsetMinutes == 0) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (offsetMinutes == 0) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("الأذان (0)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { offsetMinutes = (offsetMinutes + 1).coerceIn(-60, 60) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("+1د", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { offsetMinutes = (offsetMinutes + 5).coerceIn(-60, 60) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("+5د", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Axis: [-60 min قبل الأذان] <--- [0 وقت الأذان] ---> [+60 min بعد الأذان]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("قبل الأذان (-60د)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("وقت الأذان (0)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            Text("بعد الأذان (+60د)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Slider(
                            value = offsetMinutes.toFloat(),
                            onValueChange = { offsetMinutes = it.toInt() },
                            valueRange = -60f..60f,
                            steps = 119,
                            modifier = Modifier.testTag("slider_offset"),
                            colors = SliderDefaults.colors(
                                thumbColor = when {
                                    offsetMinutes == 0 -> EmeraldPrimary
                                    offsetMinutes < 0 -> Color(0xFFC05621)
                                    else -> Color(0xFF2B6CB0)
                                },
                                activeTrackColor = EmeraldPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick chips row: Negative (before)
                        Text("خيارات سريعة قبل الأذان (سالب):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(-45, -30, -20, -15, -10, -5).forEach { off ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (offsetMinutes == off) Color(0xFFC05621) else MaterialTheme.colorScheme.surface)
                                        .clickable { offsetMinutes = off }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${off}د",
                                        fontSize = 10.sp,
                                        color = if (offsetMinutes == off) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick chips row: Positive (after)
                        Text("خيارات سريعة بعد الأذان / الإقامة (موجب):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(5, 10, 15, 20, 30, 45).forEach { off ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (offsetMinutes == off) Color(0xFF2B6CB0) else MaterialTheme.colorScheme.surface)
                                        .clickable { offsetMinutes = off }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${off}د",
                                        fontSize = 10.sp,
                                        color = if (offsetMinutes == off) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Audio Asset Selection & Live Preview
                Text(
                    text = "المقطع الصوتي القصير (تسجيلات الحرمين وقراء معتمدين):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                val currentSelectedAsset = audioAssets.firstOrNull { it.id == selectedAudioId }
                    ?: audioAssets.firstOrNull()

                ExposedDropdownMenuBox(
                    expanded = audioDropdownExpanded,
                    onExpandedChange = { audioDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currentSelectedAsset?.displayNameArabic ?: "اختر صوتاً",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = audioDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = audioDropdownExpanded,
                        onDismissRequest = { audioDropdownExpanded = false }
                    ) {
                        audioAssets.forEach { asset ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(asset.displayNameArabic, fontWeight = FontWeight.Bold)
                                        Text("${asset.durationSeconds} ثانية • ${asset.sourceDescription}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedAudioId = asset.id
                                    audioDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Audio preview button
                if (currentSelectedAsset != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val isCurrentPreviewing = previewingAssetId == currentSelectedAsset.id && isPlayingPreview
                    OutlinedButton(
                        onClick = {
                            if (isCurrentPreviewing) onStopPreview()
                            else onPlayPreview(currentSelectedAsset)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_preview_dialog"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrentPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isCurrentPreviewing) MaterialTheme.colorScheme.error else EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCurrentPreviewing) "إيقاف الاستماع" else "استماع إلى التسجيل الصوتي",
                            color = if (isCurrentPreviewing) MaterialTheme.colorScheme.error else EmeraldPrimary,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Repeat Weekdays
                Text(
                    text = "أيام التكرار الأسبوعي",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val days = listOf("إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت", "أحد")
                    for (i in 1..7) {
                        val bit = 1 shl (i - 1)
                        val isDaySelected = (repeatDaysMask and bit) != 0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isDaySelected) EmeraldPrimary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    repeatDaysMask = if (isDaySelected) repeatDaysMask and bit.inv()
                                    else repeatDaysMask or bit
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = days[i - 1].take(2),
                                fontSize = 11.sp,
                                color = if (isDaySelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Repetition & Snooze Configuration
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "إعدادات التكرار والغفوة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Audio repeat count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مرات إعادة المقطع الصوتي:", fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                (1..4).forEach { count ->
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (audioRepeatCount == count) EmeraldPrimary else Color.Transparent)
                                            .clickable { audioRepeatCount = count },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$count",
                                            color = if (audioRepeatCount == count) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Snooze toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("تفعيل الغفوة (Snooze):", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("تأجيل التنبيه إذا لم يتم إيقافه", fontSize = 11.sp, color = Color.Gray)
                            }
                            Switch(
                                checked = snoozeEnabled,
                                onCheckedChange = { snoozeEnabled = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = EmeraldPrimary)
                            )
                        }

                        if (snoozeEnabled) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("مدة الغفوة:", fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(3, 5, 10, 15).forEach { mins ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (snoozeDurationMinutes == mins) EmeraldPrimary else Color.LightGray.copy(alpha = 0.3f))
                                                .clickable { snoozeDurationMinutes = mins }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$mins د",
                                                fontSize = 11.sp,
                                                color = if (snoozeDurationMinutes == mins) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Volume & Vibration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الاهتزاز مع التنبيه", fontSize = 13.sp)
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = EmeraldPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مستوى الصوت: $volumePercent%", fontSize = 13.sp)
                }
                Slider(
                    value = volumePercent.toFloat(),
                    onValueChange = { volumePercent = it.toInt() },
                    valueRange = 10f..100f,
                    colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 7. Apply to all prayers toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("تطبيق التنبيه على جميع الصلوات", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("إنشاء هذا المنبه لكل الصلوات الست (الفجر، الشروق، الظهر، العصر، المغرب، العشاء)", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = applyToAllPrayers,
                        onCheckedChange = { applyToAllPrayers = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = EmeraldPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("تسمية أو ملاحظة التنبيه (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onStopPreview()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            onStopPreview()
                            val rule = (initialRule ?: AlarmRuleEntity(targetPrayer = selectedPrayer)).copy(
                                targetPrayer = selectedPrayer,
                                offsetMinutes = offsetMinutes,
                                audioAssetId = selectedAudioId,
                                repeatDaysMask = repeatDaysMask,
                                audioRepeatCount = audioRepeatCount,
                                audioRepeatIntervalSec = audioRepeatIntervalSec,
                                snoozeEnabled = snoozeEnabled,
                                snoozeDurationMinutes = snoozeDurationMinutes,
                                maxSnoozeCount = maxSnoozeCount,
                                volumePercent = volumePercent,
                                vibrationEnabled = vibrationEnabled,
                                label = label
                            )
                            onSave(rule, applyToAllPrayers)
                        },
                        modifier = Modifier.weight(1f).testTag("btn_save_alarm"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ التنبيه")
                    }
                }
            }
        }
    }
}
