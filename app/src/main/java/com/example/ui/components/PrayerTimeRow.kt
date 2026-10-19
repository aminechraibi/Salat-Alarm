package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AlarmRuleEntity
import com.example.model.PrayerType
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrayerTimeRow(
    prayerType: PrayerType,
    timeFormatted: String,
    isNextPrayer: Boolean,
    rules: List<AlarmRuleEntity>,
    onToggleRule: (String, Boolean) -> Unit,
    onAddNewAlarm: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (prayerType) {
        PrayerType.FAJR -> Icons.Default.WbTwilight
        PrayerType.CHOROUK -> Icons.Default.WbSunny
        PrayerType.DHUHR -> Icons.Default.WbSunny
        PrayerType.ASR -> Icons.Default.WbSunny
        PrayerType.MAGHRIB -> Icons.Default.WbTwilight
        PrayerType.ISHA -> Icons.Default.NightsStay
    }

    val enabledRulesCount = rules.count { it.isEnabled }
    val anyRuleEnabled = enabledRulesCount > 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("prayer_row_${prayerType.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNextPrayer) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNextPrayer) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isNextPrayer) EmeraldAccent.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isNextPrayer) EmeraldPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = prayerType.arabicName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isNextPrayer) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldAccent)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "التالية",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${prayerType.englishName} • ${rules.size} تنبيهات (${enabledRulesCount} مفعل)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNextPrayer) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Dedicated Quick Add Alarm button for this specific prayer
                    FilledTonalIconButton(
                        onClick = onAddNewAlarm,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_add_alarm_${prayerType.name.lowercase()}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = EmeraldPrimary.copy(alpha = 0.15f),
                            contentColor = EmeraldPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlarm,
                            contentDescription = "إضافة تنبيه لصلاة ${prayerType.arabicName}",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Switch(
                        checked = anyRuleEnabled,
                        onCheckedChange = { isChecked ->
                            if (rules.isEmpty() && isChecked) {
                                onAddNewAlarm()
                            } else {
                                rules.forEach { rule ->
                                    onToggleRule(rule.ruleId, isChecked)
                                }
                            }
                        },
                        modifier = Modifier.testTag("switch_${prayerType.name.lowercase()}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary
                        )
                    )
                }
            }

            // Display individual alarms tags for this prayer if any exist
            if (rules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rules.forEach { rule ->
                        val chipText = when {
                            rule.offsetMinutes == 0 -> "في الوقت"
                            rule.offsetMinutes < 0 -> "${rule.offsetMinutes}د"
                            else -> "+${rule.offsetMinutes}د"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (rule.isEnabled) EmeraldPrimary.copy(alpha = 0.12f)
                                    else Color.Gray.copy(alpha = 0.15f)
                                )
                                .clickable { onToggleRule(rule.ruleId, !rule.isEnabled) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$chipText ${if (rule.isEnabled) "✓" else "✕"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (rule.isEnabled) EmeraldPrimary else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
