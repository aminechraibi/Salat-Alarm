package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.draw.clip
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PrayerSettingsEntity
import com.example.model.AsrMadhhab
import com.example.model.CalculationMethod
import com.example.model.CityPreset
import com.example.model.CityPresets
import com.example.model.HighLatitudeRule
import com.example.model.PrayerType
import com.example.ui.UiState
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: UiState,
    onUpdateSettings: (PrayerSettingsEntity) -> Unit,
    onSelectCity: (CityPreset) -> Unit,
    onFetchGpsLocation: () -> Unit,
    onClearGpsMessage: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    val context = LocalContext.current

    var showCityPickerDialog by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            onFetchGpsLocation()
        }
    }

    if (showCityPickerDialog) {
        com.example.ui.components.CityPickerDialog(
            currentCityName = settings.cityNameArabic,
            onSelectCity = { city ->
                onSelectCity(city)
                showCityPickerDialog = false
            },
            onDismiss = { showCityPickerDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إعدادات الحساب والموقع",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedButton(
                    onClick = onNavigateToDiagnostics,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_diagnostics")
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("التشخيص والأذونات", fontSize = 12.sp)
                }
            }
        }

        // 1. City / Location selector
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("المدينة والموقع الجغرافي", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        // Badge showing Morocco cities count
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "104 مدينة مغربية",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selected city card & trigger for CityPickerDialog
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCityPickerDialog = true }
                            .testTag("btn_open_city_picker")
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
                                    text = "المدينة المختارة حالياً:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${settings.cityNameArabic} (${settings.countryArabic})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${settings.cityNameEnglish}, ${settings.countryEnglish} • ${settings.timeZoneId}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { showCityPickerDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("تغيير", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "الإحداثيات الحالية: خط العرض ${String.format(java.util.Locale.US, "%.4f", settings.latitude)}° • خط الطول ${String.format(java.util.Locale.US, "%.4f", settings.longitude)}° • الارتفاع: ${settings.altitudeMeters.toInt()}م",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (com.example.util.LocationHelper.hasLocationPermission(context)) {
                                onFetchGpsLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !uiState.isGpsLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_request_location"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState.isGpsLoading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري تحديد موقعك عبر GPS...", fontSize = 13.sp)
                        } else {
                            Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحديد الموقع الدقيق عبر GPS", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.gpsMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.gpsMessage.contains("نجاح"))
                                    Color(0xFFE6F4EA)
                                else
                                    Color(0xFFFCE8E6)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.gpsMessage,
                                    fontSize = 12.sp,
                                    color = if (uiState.gpsMessage.contains("نجاح"))
                                        Color(0xFF137333)
                                    else
                                        Color(0xFFC5221F),
                                    modifier = Modifier.weight(1f)
                                )
                                androidx.compose.material3.IconButton(
                                    onClick = onClearGpsMessage,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "إغلاق",
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Calculation Method
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("طريقة حساب مواقيت الصلاة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = methodDropdownExpanded,
                        onExpandedChange = { methodDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = settings.method.titleArabic,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الهيئة / الطريقة الفلكية") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = methodDropdownExpanded,
                            onDismissRequest = { methodDropdownExpanded = false }
                        ) {
                            CalculationMethod.entries.forEach { method ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(method.titleArabic, fontWeight = FontWeight.Bold)
                                            Text(method.titleEnglish, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        onUpdateSettings(settings.copy(method = method))
                                        methodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Asr Madhhab
                    Text("مذهب حساب وقت صلاة العصر", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsrMadhhab.entries.forEach { madhhab ->
                            FilterChip(
                                selected = settings.asrMadhhab == madhhab,
                                onClick = { onUpdateSettings(settings.copy(asrMadhhab = madhhab)) },
                                label = { Text(madhhab.titleArabic, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // High latitude rule
                    Text("قاعدة المناطق ذات خطوط العرض العليا", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HighLatitudeRule.entries.forEach { rule ->
                            FilterChip(
                                selected = settings.highLatitudeRule == rule,
                                onClick = { onUpdateSettings(settings.copy(highLatitudeRule = rule)) },
                                label = { Text(rule.titleArabic.take(15), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Per-Prayer Minute Adjustments
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تعديل يدوي بالدقائق لكل صلاة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "يمكنك مطابقة أوقات الصلاة مع المسجد المحلي بإضافة أو خصم دقائق",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val prayerAdjustments = listOf(
                        Triple(PrayerType.FAJR, settings.fajrAdjustment) { v: Int -> settings.copy(fajrAdjustment = v) },
                        Triple(PrayerType.CHOROUK, settings.sunriseAdjustment) { v: Int -> settings.copy(sunriseAdjustment = v) },
                        Triple(PrayerType.DHUHR, settings.dhuhrAdjustment) { v: Int -> settings.copy(dhuhrAdjustment = v) },
                        Triple(PrayerType.ASR, settings.asrAdjustment) { v: Int -> settings.copy(asrAdjustment = v) },
                        Triple(PrayerType.MAGHRIB, settings.maghribAdjustment) { v: Int -> settings.copy(maghribAdjustment = v) },
                        Triple(PrayerType.ISHA, settings.ishaAdjustment) { v: Int -> settings.copy(ishaAdjustment = v) }
                    )

                    prayerAdjustments.forEach { (prayer, currentAdj, updater) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prayer.arabicName, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
                            Slider(
                                value = currentAdj.toFloat(),
                                onValueChange = { onUpdateSettings(updater(it.toInt())) },
                                valueRange = -15f..15f,
                                steps = 29,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentAdj > 0) "+$currentAdj د" else "$currentAdj د",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentAdj != 0) EmeraldPrimary else Color.Gray,
                                modifier = Modifier.width(44.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Time format
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("نظام عرض الوقت (24 ساعة)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (settings.is24HourFormat) "صيغة 24 ساعة (مثال: 15:30)" else "صيغة 12 ساعة (مثال: 03:30 م)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = settings.is24HourFormat,
                        onCheckedChange = { onUpdateSettings(settings.copy(is24HourFormat = it)) },
                        colors = SwitchDefaults.colors(checkedTrackColor = EmeraldPrimary)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
