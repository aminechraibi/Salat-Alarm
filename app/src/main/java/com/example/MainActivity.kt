package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.AlarmRuleEntity
import com.example.model.PrayerType
import com.example.ui.MainViewModel
import com.example.ui.screens.AlarmEditorDialog
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.AudioCreditsScreen
import com.example.ui.screens.AudioLibraryScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme

enum class AppNavTab(val titleArabic: String) {
    HOME("الرئيسية"),
    ALARMS("التنبيهات"),
    AUDIO("الصوتيات"),
    SETTINGS("الإعدادات")
}

enum class SubScreen {
    NONE,
    DIAGNOSTICS,
    AUDIO_CREDITS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        PrayerReminderMainApp()
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerReminderMainApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppNavTab.HOME) }
    var activeSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var initialPrayerFilter by remember { mutableStateOf<PrayerType?>(null) }

    // Alarm Editor state
    var showAlarmEditor by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<AlarmRuleEntity?>(null) }
    var defaultPrayerForNewAlarm by remember { mutableStateOf<PrayerType?>(null) }

    // Handle Back Press when in sub-screens
    BackHandler(enabled = activeSubScreen != SubScreen.NONE) {
        activeSubScreen = SubScreen.NONE
    }

    if (showAlarmEditor) {
        AlarmEditorDialog(
            initialRule = ruleToEdit,
            defaultPrayer = defaultPrayerForNewAlarm,
            audioAssets = uiState.audioAssets,
            previewingAssetId = uiState.previewingAssetId,
            isPlayingPreview = uiState.isPlayingPreview,
            onPlayPreview = { viewModel.playAudioPreview(it) },
            onStopPreview = { viewModel.stopAudioPreview() },
            onSave = { rule, applyToAll ->
                if (applyToAll) {
                    viewModel.applyRuleToAllPrayers(rule)
                } else {
                    viewModel.saveRule(rule)
                }
                showAlarmEditor = false
                ruleToEdit = null
            },
            onDismiss = {
                showAlarmEditor = false
                ruleToEdit = null
            }
        )
    }

    if (activeSubScreen == SubScreen.AUDIO_CREDITS) {
        AudioCreditsScreen(
            audioAssets = uiState.audioAssets,
            onBack = { activeSubScreen = SubScreen.NONE }
        )
        return
    }

    if (activeSubScreen == SubScreen.DIAGNOSTICS) {
        DiagnosticsScreen(
            uiState = uiState,
            canScheduleExact = viewModel.canScheduleExactAlarms(),
            onTriggerTestAlarm = { viewModel.triggerInstantTestAlarm() }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppNavTab.HOME,
                    onClick = { currentTab = AppNavTab.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "الرئيسية"
                        )
                    },
                    label = { Text("الرئيسية") },
                    modifier = Modifier.testTag("nav_home"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.ALARMS,
                    onClick = {
                        initialPrayerFilter = null
                        currentTab = AppNavTab.ALARMS
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.ALARMS) Icons.Filled.Alarm else Icons.Outlined.Alarm,
                            contentDescription = "التنبيهات"
                        )
                    },
                    label = { Text("التنبيهات") },
                    modifier = Modifier.testTag("nav_alarms"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.AUDIO,
                    onClick = { currentTab = AppNavTab.AUDIO },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.AUDIO) Icons.Filled.Audiotrack else Icons.Outlined.Audiotrack,
                            contentDescription = "الصوتيات"
                        )
                    },
                    label = { Text("الصوتيات") },
                    modifier = Modifier.testTag("nav_audio"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.SETTINGS,
                    onClick = { currentTab = AppNavTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "الإعدادات"
                        )
                    },
                    label = { Text("الإعدادات") },
                    modifier = Modifier.testTag("nav_settings"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary
                    )
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppNavTab.HOME -> {
                HomeScreen(
                    uiState = uiState,
                    onNavigateToAlarms = { prayer ->
                        initialPrayerFilter = prayer
                        currentTab = AppNavTab.ALARMS
                    },
                    onAddNewAlarm = { prayer ->
                        ruleToEdit = null
                        defaultPrayerForNewAlarm = prayer
                        showAlarmEditor = true
                    },
                    onQuickAddAlarm = { prayer, offsetMinutes, audioId, label ->
                        viewModel.addQuickAlarmForPrayer(prayer, offsetMinutes, audioId, label)
                    },
                    onAddAllPrayersAlarms = { offsetMinutes ->
                        viewModel.addAllPrayersAlarms(offsetMinutes)
                    },
                    onToggleRule = { ruleId, enabled ->
                        viewModel.toggleRule(ruleId, enabled)
                    },
                    onTriggerTestAlarm = {
                        viewModel.triggerInstantTestAlarm()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppNavTab.ALARMS -> {
                AlarmsScreen(
                    uiState = uiState,
                    initialFilterPrayer = initialPrayerFilter,
                    onAddNewAlarm = { prayer ->
                        ruleToEdit = null
                        defaultPrayerForNewAlarm = prayer
                        showAlarmEditor = true
                    },
                    onAddAllPrayersAlarms = { offsetMinutes ->
                        viewModel.addAllPrayersAlarms(offsetMinutes)
                    },
                    onEditRule = { rule ->
                        ruleToEdit = rule
                        defaultPrayerForNewAlarm = rule.targetPrayer
                        showAlarmEditor = true
                    },
                    onDuplicateRule = { rule ->
                        viewModel.duplicateRule(rule)
                    },
                    onDeleteRule = { rule ->
                        viewModel.deleteRule(rule)
                    },
                    onToggleRule = { ruleId, enabled ->
                        viewModel.toggleRule(ruleId, enabled)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppNavTab.AUDIO -> {
                AudioLibraryScreen(
                    uiState = uiState,
                    onPlayAudio = { viewModel.playAudioPreview(it) },
                    onStopAudio = { viewModel.stopAudioPreview() },
                    onVolumeChanged = { viewModel.setPreviewVolume(it) },
                    onImportAudio = { uri, name -> viewModel.importCustomAudio(uri, name) },
                    onNavigateToCredits = { activeSubScreen = SubScreen.AUDIO_CREDITS },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppNavTab.SETTINGS -> {
                SettingsScreen(
                    uiState = uiState,
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onSelectCity = { viewModel.selectCityPreset(it) },
                    onFetchGpsLocation = { viewModel.fetchGpsLocation() },
                    onClearGpsMessage = { viewModel.clearGpsMessage() },
                    onNavigateToDiagnostics = { activeSubScreen = SubScreen.DIAGNOSTICS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
