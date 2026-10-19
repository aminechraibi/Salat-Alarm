# Salat Alarm (منبّه ومذكّر مواقيت الصلاة) 🕌⏰

An elegant, battery-efficient, and 100% offline Android application for accurate Islamic prayer time calculations, custom adhan alarms, and spoken Arabic reminders.

Designed with **Jetpack Compose**, **Material Design 3**, and modern Android architectural best practices, **Salat Alarm** works completely without internet access, ensuring absolute privacy, precision, and reliability.

---

## ✨ Features

### 1. 🧭 Offline Precision Calculation Engine
- **Pure Astronomical Math**: High-precision solar calculations for Fajr, Sunrise (Chorouk), Dhuhr, Asr, Maghrib, and Isha.
- **Global Calculation Methods**:
  - Umm Al-Qura (Makkah, Saudi Arabia)
  - Muslim World League (MWL)
  - Egyptian General Authority of Survey
  - University of Islamic Sciences, Karachi
  - Directorate of Religious Affairs, Turkey (Diyanet)
  - Ministry of Endowments and Islamic Affairs, Morocco
  - Institute of Geophysics, University of Tehran
  - Islamic Society of North America (ISNA)
  - Union des Organisations Islamiques de France (UOIF)
  - Custom angles for Fajr and Isha
- **Asr Juristic Methods (Madhhab)**: Standard (Shafi'i, Maliki, Hanbali) and Hanafi (shadow factor 2x).
- **High Latitude Rules**: Middle of the night, One-seventh, and Angle-based adjustments for polar and northern latitudes.
- **Per-Prayer Minute Adjustments**: Fine-tune each prayer time by ±30 minutes to match local mosque timetables.

### 2. 📍 Location: GPS & Complete Offline Moroccan Database (104+ Cities)
- **Comprehensive Morocco Database**: Bundled offline database containing **104+ Moroccan cities, prefectures, and municipalities** across all 12 regions (Tanger, Tétouan, Al Hoceima, Nador, Oujda, Fès, Meknès, Rabat, Salé, Casablanca, Marrakech, Agadir, Beni Mellal, Khouribga, Errachidia, Ouarzazate, Taroudant, Guelmim, Laâyoune, Dakhla, etc.) with official coordinates and elevation data.
- **GPS Auto-Detection**: One-click location acquisition via device hardware GPS/Network provider, automatically matching your exact location and selecting the Moroccan Ministry of Habous calculation method (`CalculationMethod.MOROCCO`).
- **Interactive City Picker**: Filter by country (Morocco vs. International) with real-time bilingual search (Arabic & French/English).

### 3. ⏰ Advanced Prayer Alarm & Reminder System
- **Multiple Alarms Per Prayer**: Set independent alarms for each prayer (e.g. 15 min before Fajr for Suhur, at exact Fajr time, and 20 min after for Iqama).
- **Offset Range (-60 min to +60 min)**:
  - `0`: Exactly at the time of Adhan.
  - Negative values (`-1` to `-60` min): Alerts **before** Adhan (e.g., wake up, prepare for prayer).
  - Positive values (`+1` to `+60` min): Alerts **after** Adhan (e.g., Iqama, Dhikr).
- **Customizable Repeat Schedule**: Choose specific days of the week (Mon–Sun).
- **Audio Repetition**: Repeat the selected audio up to 5 times with custom intervals.
- **Snooze & Volume Controls**: Configurable snooze duration and volume overrides.

### 4. 🔊 Authentic Audio Library & Spoken Reminders
- **Renowned Adhan Recordings (MP3)**:
  - Adhan Makkah Al-Mukarramah
  - Adhan Al-Madinah Al-Munawwarah
  - Adhan Al-Masjid Al-Aqsa (Jerusalem)
  - Sheikh Mishary Rashid Alafasy
  - Sheikh Abdulbasit Abdussamad
- **Short Spoken Arabic Reminders**: Clear Arabic spoken alerts for each prayer ("حان الآن موعد صلاة الفجر", "خمس دقائق على موعد الصلاة", "الصلاة خير من النوم", etc.).
- **Custom Audio Import**: Import any local MP3 file from device storage to use as custom alarm tone.

### 5. 🛡️ Reliability & Exact Timing
- **Android Exact Alarms**: Utilizes `AlarmManager.setExactAndAllowWhileIdle()` to ensure alarms ring reliably even in Doze mode.
- **Device Reboot Resilience**: `RECEIVE_BOOT_COMPLETED` receiver automatically reschedules all upcoming alarms after system restart.
- **Foreground Playback Service**: Dedicated audio playback service with notification controls (Dismiss / Snooze).
- **Diagnostics Screen**: Real-time permission checker for Exact Alarms, Battery Optimization exclusions, Notification permissions, and audio test alarms.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.x
- **UI Toolkit**: Jetpack Compose with Material 3 (M3) theming, dark/light theme support, and RTL Arabic layout.
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture:
  - `calc`: Astronomical solar positioning and prayer time calculations.
  - `data`: Room Database (SQLite) with DAOs and Repositories.
  - `alarm`: Android AlarmManager scheduler and occurrence reconciliation engine.
  - `service` & `receiver`: Foreground playback service and boot/alarm broadcast receivers.
  - `ui`: Composables, Navigation, Dialogs, and reactive StateFlow ViewModel.
- **Local Persistence**: Room SQLite for user rules, occurrences, settings, and audio metadata.
- **CI/CD**: GitHub Actions workflow for automated testing and signed APK releases.

---

## 📦 Project Structure

```text
├── .github/workflows/
│   └── release.yml               # Automated CI test & signed APK release workflow
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── alarm/        # Exact alarm scheduling & reconciliation
│   │   │   │   ├── calc/         # Pure Kotlin solar prayer calculations
│   │   │   │   ├── data/         # Room Database entities, DAOs, repositories
│   │   │   │   ├── model/        # PrayerType, CityPresets, CalculationSettings
│   │   │   │   ├── receiver/     # AlarmReceiver, BootReceiver
│   │   │   │   ├── service/      # AlarmPlaybackService (Foreground)
│   │   │   │   ├── ui/           # Screens, Components, Theme, MainViewModel
│   │   │   │   └── util/         # LocationHelper (GPS & reverse geocoding)
│   │   │   ├── res/
│   │   │   │   ├── raw/          # Bundled Adhan & spoken Arabic MP3s
│   │   │   │   └── values/       # strings.xml, colors, themes
│   │   │   └── AndroidManifest.xml
│   │   └── test/                 # Local JVM Robolectric & Unit tests
│   └── build.gradle.kts          # App dependencies & signingConfigs
├── build.gradle.kts              # Root build configuration
└── settings.gradle.kts           # Module definitions
```

---

## 🚀 Building & Installing

### Prerequisites
- **JDK**: Java Development Kit 21 (or 17+)
- **Android SDK**: `compileSdk = 36`, `minSdk = 24` (Android 7.0+)
- **Gradle**: Gradle 9.3+ (or standard Gradle build tools)

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/salat-alarm.git
cd salat-alarm
```

### 2. Run Local Unit Tests
```bash
gradle testDebugUnitTest
```

### 3. Build Debug APK
```bash
gradle assembleDebug
```
The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 4. Build Signed Release APK
To build a production release APK locally:
```bash
export KEYSTORE_PATH="/path/to/your/upload-keystore.jks"
export ANDROID_KEYSTORE_PASSWORD="your_keystore_password"
export ANDROID_KEY_ALIAS="your_key_alias"
export ANDROID_KEY_PASSWORD="your_key_password"

gradle assembleRelease
```
The output signed APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## 🤖 GitHub Actions CI/CD Setup

The repository includes a production-ready GitHub Actions workflow (`.github/workflows/release.yml`) that automatically runs unit tests and compiles signed release APKs on push to `main`/`master` branches.

### Required GitHub Repository Secrets
To enable automated APK signing and GitHub Releases, configure the following secrets in **Settings > Secrets and variables > Actions**:

| Secret Name | Description |
| :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded string of your release `.jks` keystore file (`base64 -w 0 my-release.jks`) |
| `ANDROID_KEYSTORE_PASSWORD` | Password for the release keystore |
| `ANDROID_KEY_ALIAS` | Alias of the signing key |
| `ANDROID_KEY_PASSWORD` | Password for the signing key |

---

## 🔒 Permissions & Privacy

Salat Alarm is designed with strict privacy principles:
- **Zero Internet Requirement**: No network requests are made for calculations or adhan playback.
- **No Ads, Trackers, or Analytics**: No user telemetry or personal identifiers are stored or shared.
- **Location Permissions (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`)**: Used strictly on-device to obtain coordinates for solar angles.
- **Exact Alarm (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`)**: Required by Android 12+ to ring alarms at the exact minute requested.
- **Boot (`RECEIVE_BOOT_COMPLETED`)**: Ensures your prayer alarms remain active after restarting your phone.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
Audio assets and adhan recordings are licensed under public domain / CC0 attribution.
