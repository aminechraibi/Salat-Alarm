package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.entity.AlarmRuleEntity
import com.example.model.PrayerType
import com.example.ui.UiState
import com.example.ui.components.AlarmRuleCard
import com.example.ui.theme.EmeraldPrimary

@Composable
fun AlarmsScreen(
    uiState: UiState,
    initialFilterPrayer: PrayerType?,
    onAddNewAlarm: (PrayerType?) -> Unit,
    onAddAllPrayersAlarms: (Int) -> Unit,
    onEditRule: (AlarmRuleEntity) -> Unit,
    onDuplicateRule: (AlarmRuleEntity) -> Unit,
    onDeleteRule: (AlarmRuleEntity) -> Unit,
    onToggleRule: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<PrayerType?>(initialFilterPrayer) }

    val filteredRules = if (selectedFilter == null) {
        uiState.rules
    } else {
        uiState.rules.filter { it.targetPrayer == selectedFilter }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddNewAlarm(selectedFilter) },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_alarm")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة تنبيه"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title & total count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة التنبيهات المستقلة",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${uiState.rules.count { it.isEnabled }} مفعل من ${uiState.rules.size}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick bulk add buttons for all prayers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddAllPrayersAlarms(0) },
                    modifier = Modifier.weight(1f).testTag("btn_bulk_add_at_time"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AlarmAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("كل الصلوات (في الوقت)", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = { onAddAllPrayersAlarms(-10) },
                    modifier = Modifier.weight(1f).testTag("btn_bulk_add_10min_before"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AlarmAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("كل الصلوات (-10د قبل)", fontSize = 11.sp, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prayer Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("الكل (${uiState.rules.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                items(PrayerType.entries.size) { index ->
                    val prayer = PrayerType.entries[index]
                    val count = uiState.rules.count { it.targetPrayer == prayer }
                    FilterChip(
                        selected = selectedFilter == prayer,
                        onClick = { selectedFilter = prayer },
                        label = { Text("${prayer.arabicName} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick add for currently filtered prayer button
            if (selectedFilter != null) {
                Button(
                    onClick = { onAddNewAlarm(selectedFilter) },
                    modifier = Modifier.fillMaxWidth().testTag("btn_add_alarm_for_prayer"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة تنبيه جديد لصلاة ${selectedFilter?.arabicName} +", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (filteredRules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedFilter != null) "لا توجد تنبيهات لصلاة ${selectedFilter?.arabicName}" else "لا توجد تنبيهات مضافة",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اضغط على زر (+) لإضافة تنبيه مخصص بأصوات الحرمين وتكبيرات واقعية",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRules.size) { idx ->
                        val rule = filteredRules[idx]
                        val audioAsset = uiState.audioAssets.firstOrNull { it.id == rule.audioAssetId }

                        AlarmRuleCard(
                            rule = rule,
                            audioAsset = audioAsset,
                            onToggle = { isChecked ->
                                onToggleRule(rule.ruleId, isChecked)
                            },
                            onEdit = { onEditRule(rule) },
                            onDuplicate = { onDuplicateRule(rule) },
                            onDelete = { onDeleteRule(rule) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
                    }
                }
            }
        }
    }
}
