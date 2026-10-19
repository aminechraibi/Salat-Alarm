package com.example.model

enum class CalculationMethod(
    val titleArabic: String,
    val titleEnglish: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int? = null
) {
    MOROCCO("وزارة الأوقاف والشؤون الإسلامية (المغرب)", "Morocco (Awqaf)", 19.0, 17.0),
    SAUDI_ARABIA("أم القرى (مكة المكرمة)", "Umm al-Qura (Makkah)", 18.5, 0.0, ishaMinutesAfterMaghrib = 90),
    MWL("رابطة العالم الإسلامي", "Muslim World League", 18.0, 17.0),
    ISNA("الجمعية الإسلامية لأمريكا الشمالية (ISNA)", "ISNA (North America)", 15.0, 15.0),
    EGYPT("الهيئة المصرية العامة للمساحة", "Egyptian General Authority", 19.5, 17.5),
    KARACHI("جامعة العلوم الإسلامية بكراتشي", "Univ. of Islamic Sciences, Karachi", 18.0, 18.0),
    TURKEY("رئاسة الشؤون الدينية (تركيا)", "Diyanet (Turkey)", 18.0, 17.0),
    KUWAIT("وزارة الأوقاف بالكويت", "Kuwait Awqaf", 18.0, 17.5),
    QATAR("وزارة الأوقاف والشؤون الإسلامية (قطر)", "Qatar Awqaf", 18.0, 0.0, ishaMinutesAfterMaghrib = 90),
    CUSTOM("مخصص (زوايا يدوية)", "Custom Parameters", 18.0, 17.0)
}

enum class AsrMadhhab(val titleArabic: String, val titleEnglish: String, val shadowFactor: Double) {
    STANDARD("الشافعي، المالكي، الحنبلي (ظل 1×)", "Standard / Shafi'i / Maliki / Hanbali", 1.0),
    HANAFI("الحنفي (ظل 2×)", "Hanafi", 2.0)
}

enum class HighLatitudeRule(val titleArabic: String, val titleEnglish: String) {
    NONE("بدون تعديل", "None"),
    ANGLE_BASED("نسبة الزاوية (Angle-based)", "Angle Based"),
    MIDNIGHT("منتصف الليل (Midnight rule)", "Midnight Rule"),
    ONE_SEVENTH("سبع الليل (One-seventh rule)", "One Seventh Rule")
}
