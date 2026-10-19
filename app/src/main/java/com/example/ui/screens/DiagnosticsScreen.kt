package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.OccurrenceStatus
import com.example.ui.UiState
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DiagnosticsScreen(
    uiState: UiState,
    canScheduleExact: Boolean,
    onTriggerTestAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "تشخيص النظام وجدولة المنبهات",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "التحقق من موثوقية عمل التطبيق في الخلفية ودقة المواعيد",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Exact Alarm Permission Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (canScheduleExact) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (canScheduleExact) EmeraldPrimary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "إذن التنبيه الدقيق (Exact Alarms)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (canScheduleExact) "مفعل ومسموح به (دقة في الثواني)" else "غير مفعل - يتطلب إذن النظام",
                                fontSize = 12.sp,
                                color = if (canScheduleExact) EmeraldPrimary else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("تفعيل", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Notification Permission Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "إذن الإشعارات (Post Notifications)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ضروري لإظهار شاشة التنبيه وخيارات الغفوة",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        OutlinedButton(
                            onClick = {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("طلب الإذن", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Test Foreground Service Alarm
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اختبار التنبيه الصوتي الفوري",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "تشغيل خدمة التنبيه وإشعار التحكم بالصوت",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = onTriggerTestAlarm,
                        modifier = Modifier.testTag("btn_diag_trigger_test"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بدء الاختبار", fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Upcoming Occurrences from SQLite
        item {
            Text(
                text = "قائمة المواعيد المجدولة القادمة (${uiState.upcomingOccurrences.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (uiState.upcomingOccurrences.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "لا توجد مواعيد مجدولة حالياً في قاعدة البيانات.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(uiState.upcomingOccurrences.size) { idx ->
                val occ = uiState.upcomingOccurrences[idx]
                val instant = Instant.ofEpochMilli(occ.scheduledTimestamp)
                val zone = try { ZoneId.of(uiState.settings.timeZoneId) } catch (e: Exception) { ZoneId.systemDefault() }
                val zdt = instant.atZone(zone)
                val formatted = zdt.format(DateTimeFormatter.ofPattern("EEEE d MMMM • HH:mm:ss"))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.testTag("occurrence_card_${occ.occurrenceId}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldPrimary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = occ.prayerType.arabicName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "معرّف الموعد: ${occ.occurrenceId}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (occ.status) {
                                        OccurrenceStatus.SCHEDULED -> EmeraldAccent.copy(alpha = 0.2f)
                                        OccurrenceStatus.FIRING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        OccurrenceStatus.SNOOZED -> Color(0xFFFFF3E0)
                                        else -> Color.LightGray.copy(alpha = 0.3f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (occ.status) {
                                    OccurrenceStatus.SCHEDULED -> "مجدول"
                                    OccurrenceStatus.FIRING -> "يعمل الآن"
                                    OccurrenceStatus.SNOOZED -> "في حالة غفوة"
                                    OccurrenceStatus.COMPLETED -> "مكتمل"
                                    OccurrenceStatus.DISMISSED -> "تم الإيقاف"
                                    OccurrenceStatus.EXPIRED -> "منتهي"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (occ.status) {
                                    OccurrenceStatus.SCHEDULED -> EmeraldPrimary
                                    OccurrenceStatus.FIRING -> EmeraldPrimary
                                    OccurrenceStatus.SNOOZED -> Color(0xFFE65100)
                                    else -> Color.DarkGray
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
