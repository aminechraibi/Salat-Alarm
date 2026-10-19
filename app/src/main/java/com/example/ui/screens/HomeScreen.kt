package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AlarmRuleEntity
import com.example.model.PrayerType
import com.example.ui.UiState
import com.example.ui.components.NextPrayerCard
import com.example.ui.components.PrayerTimeRow
import com.example.ui.components.QuickAddAlarmDialog
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: UiState,
    onNavigateToAlarms: (PrayerType?) -> Unit,
    onAddNewAlarm: (PrayerType) -> Unit,
    onQuickAddAlarm: (PrayerType, Int, String, String) -> Unit,
    onAddAllPrayersAlarms: (Int) -> Unit,
    onToggleRule: (String, Boolean) -> Unit,
    onTriggerTestAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    val prayerTimes = uiState.todayPrayerTimes
    val nextPrayer = uiState.nextPrayer
    val rules = uiState.rules

    var quickAddPrayer by remember { mutableStateOf<PrayerType?>(null) }

    if (quickAddPrayer != null) {
        val prayer = quickAddPrayer!!
        QuickAddAlarmDialog(
            prayer = prayer,
            audioAssets = uiState.audioAssets,
            onQuickAdd = { offset, audioId, label ->
                onQuickAddAlarm(prayer, offset, audioId, label)
                quickAddPrayer = null
            },
            onOpenFullCustomizer = {
                val target = prayer
                quickAddPrayer = null
                onAddNewAlarm(target)
            },
            onDismiss = { quickAddPrayer = null }
        )
    }

    // Date formatting
    val today = LocalDate.now()
    val gregorianDateStr = today.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale("ar")))
    val hijriDate = try {
        val hijrah = HijrahDate.from(today)
        DateTimeFormatter.ofPattern("d MMMM yyyy هـ", Locale("ar")).format(hijrah)
    } catch (e: Exception) {
        ""
    }

    val timeFormatter = if (settings.is24HourFormat) {
        DateTimeFormatter.ofPattern("HH:mm")
    } else {
        DateTimeFormatter.ofPattern("hh:mm a", Locale("ar"))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Header Location & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${settings.cityNameArabic}، ${settings.countryArabic}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = gregorianDateStr,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hijriDate.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = hijriDate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Next prayer card banner
        item {
            NextPrayerCard(
                nextPrayer = nextPrayer,
                cityName = settings.cityNameArabic,
                activeRulesCount = rules.count { it.isEnabled }
            )
        }

        // Quick Test & Add All Alarms Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTriggerTestAlarm,
                    modifier = Modifier.weight(1f).testTag("btn_test_instant_alarm"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تجربة التكبير الآن", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onAddAllPrayersAlarms(0) },
                    modifier = Modifier.weight(1f).testTag("btn_add_all_prayers"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AlarmAdd,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تنبيه لكل الصلوات", fontSize = 12.sp)
                }
            }
        }

        // Section Title: Prayer Timetable
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مواقيت صلاة اليوم والتنبيهات",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "اضغط على (+) لإضافة منبه لأي صلاة، أو المس السطر لفتح تفاصيلها",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = settings.method.titleArabic.take(15),
                    fontSize = 11.sp,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 6 Daily Prayer Rows with dedicated Add Alarm (+) button on each
        if (prayerTimes != null) {
            val prayers = listOf(
                PrayerType.FAJR,
                PrayerType.CHOROUK,
                PrayerType.DHUHR,
                PrayerType.ASR,
                PrayerType.MAGHRIB,
                PrayerType.ISHA
            )

            items(prayers.size) { index ->
                val prayer = prayers[index]
                val time = prayerTimes.getTime(prayer)
                val isNext = nextPrayer?.prayerType == prayer
                val prayerRules = rules.filter { it.targetPrayer == prayer }

                PrayerTimeRow(
                    prayerType = prayer,
                    timeFormatted = time.format(timeFormatter),
                    isNextPrayer = isNext,
                    rules = prayerRules,
                    onToggleRule = onToggleRule,
                    onAddNewAlarm = {
                        quickAddPrayer = prayer
                    },
                    onClick = {
                        onNavigateToAlarms(prayer)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
