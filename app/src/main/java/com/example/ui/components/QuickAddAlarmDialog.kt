package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import com.example.data.entity.AudioAssetEntity
import com.example.model.PrayerType
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary

@Composable
fun QuickAddAlarmDialog(
    prayer: PrayerType,
    audioAssets: List<AudioAssetEntity>,
    onQuickAdd: (offsetMinutes: Int, audioId: String, label: String) -> Unit,
    onOpenFullCustomizer: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedOffset by remember { mutableIntStateOf(0) }
    var selectedAudioId by remember {
        mutableStateOf(
            when (prayer) {
                PrayerType.FAJR -> "takbir_makkah"
                PrayerType.CHOROUK -> "reminder_chorouk"
                PrayerType.DHUHR -> "takbir_madinah"
                PrayerType.ASR -> "takbir_alafasy"
                PrayerType.MAGHRIB -> "takbir_alaqsa"
                PrayerType.ISHA -> "takbir_abdulbasit"
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAlert,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "إضافة منبه لصلاة ${prayer.arabicName}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "حدد الدقيقة بالضبط (الأذان = 0، قبل = سالب، بعد = موجب)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Selected Offset Display
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "وقت التنبيه بالنسبة للأذان:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            selectedOffset == 0 -> EmeraldPrimary
                                            selectedOffset < 0 -> Color(0xFFC05621)
                                            else -> Color(0xFF2B6CB0)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = when {
                                        selectedOffset == 0 -> "0 دقيقة (في وقت الأذان)"
                                        selectedOffset < 0 -> "$selectedOffset د (قبل الأذان)"
                                        else -> "+$selectedOffset د (بعد الأذان)"
                                    },
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stepper buttons (-5, -1, 0, +1, +5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { selectedOffset = (selectedOffset - 5).coerceIn(-60, 60) },
                                modifier = Modifier.weight(1f).height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("-5د", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { selectedOffset = (selectedOffset - 1).coerceIn(-60, 60) },
                                modifier = Modifier.weight(1f).height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("-1د", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { selectedOffset = 0 },
                                modifier = Modifier.weight(1.3f).height(34.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedOffset == 0) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedOffset == 0) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("الأذان (0)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { selectedOffset = (selectedOffset + 1).coerceIn(-60, 60) },
                                modifier = Modifier.weight(1f).height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("+1د", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { selectedOffset = (selectedOffset + 5).coerceIn(-60, 60) },
                                modifier = Modifier.weight(1f).height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Text("+5د", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Slider from -60 to +60 min
                        Slider(
                            value = selectedOffset.toFloat(),
                            onValueChange = { selectedOffset = it.toInt() },
                            valueRange = -60f..60f,
                            steps = 119,
                            modifier = Modifier.testTag("slider_quick_offset"),
                            colors = SliderDefaults.colors(
                                thumbColor = when {
                                    selectedOffset == 0 -> EmeraldPrimary
                                    selectedOffset < 0 -> Color(0xFFC05621)
                                    else -> Color(0xFF2B6CB0)
                                },
                                activeTrackColor = EmeraldPrimary
                            )
                        )

                        // Quick preset chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(-30, -15, -10, -5, 0, 5, 10, 15, 20).forEach { off ->
                                val chipText = if (off == 0) "الأذان" else if (off < 0) "${off}" else "+$off"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedOffset == off) EmeraldPrimary else MaterialTheme.colorScheme.surface)
                                        .clickable { selectedOffset = off }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chipText,
                                        fontSize = 10.sp,
                                        color = if (selectedOffset == off) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Realistic Audio Selection
                Text(
                    text = "التسجيل الصوتي (مقاطع واقعية من الحرمين وقراء معتمدين):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val topAudios = audioAssets.filter { it.id.startsWith("takbir_") || it.id == "reminder_${prayer.name.lowercase()}" }.take(5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    topAudios.forEach { audio ->
                        val isSelected = selectedAudioId == audio.id
                        val shortLabel = when (audio.id) {
                            "takbir_makkah" -> "الحرم المكي"
                            "takbir_madinah" -> "النبوي"
                            "takbir_alafasy" -> "العفاسي"
                            "takbir_alaqsa" -> "الأقصى"
                            "takbir_abdulbasit" -> "عبد الباسط"
                            else -> audio.displayNameArabic.take(10)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedAudioId = audio.id }
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shortLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenFullCustomizer()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تخصيص كامل", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val label = when {
                                selectedOffset == 0 -> "وقت صلاة ${prayer.arabicName}"
                                selectedOffset < 0 -> "قبل ${prayer.arabicName} بـ ${kotlin.math.abs(selectedOffset)}د"
                                else -> "بعد ${prayer.arabicName} بـ ${selectedOffset}د"
                            }
                            onQuickAdd(selectedOffset, selectedAudioId, label)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("btn_confirm_quick_add"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ التنبيه", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
