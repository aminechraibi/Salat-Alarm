package com.example.model

data class CityPreset(
    val cityNameArabic: String,
    val cityNameEnglish: String,
    val countryArabic: String,
    val countryEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val timeZoneId: String,
    val recommendedMethod: CalculationMethod
)

object CityPresets {
    // Comprehensive offline database of Moroccan cities and provinces
    val moroccoList: List<CityPreset> = MoroccoCities.list

    // Major International capitals and religious centers
    val internationalList: List<CityPreset> = listOf(
        // Saudi Arabia
        CityPreset("مكة المكرمة", "Makkah", "المملكة العربية السعودية", "Saudi Arabia", 21.4225, 39.8262, 277.0, "Asia/Riyadh", CalculationMethod.SAUDI_ARABIA),
        CityPreset("المدينة المنورة", "Madinah", "المملكة العربية السعودية", "Saudi Arabia", 24.5247, 39.5692, 608.0, "Asia/Riyadh", CalculationMethod.SAUDI_ARABIA),
        CityPreset("الرياض", "Riyadh", "المملكة العربية السعودية", "Saudi Arabia", 24.7136, 46.6753, 612.0, "Asia/Riyadh", CalculationMethod.SAUDI_ARABIA),
        CityPreset("جدة", "Jeddah", "المملكة العربية السعودية", "Saudi Arabia", 21.5433, 39.1728, 12.0, "Asia/Riyadh", CalculationMethod.SAUDI_ARABIA),

        // Palestine & Levant & Iraq
        CityPreset("القدس الشريف", "Jerusalem", "فلسطين", "Palestine", 31.7683, 35.2137, 754.0, "Asia/Jerusalem", CalculationMethod.MWL),
        CityPreset("عمان", "Amman", "الأردن", "Jordan", 31.9454, 35.9284, 773.0, "Asia/Amman", CalculationMethod.MWL),
        CityPreset("بيروت", "Beirut", "لبنان", "Lebanon", 33.8938, 35.5018, 35.0, "Asia/Beirut", CalculationMethod.MWL),
        CityPreset("دمشق", "Damascus", "سوريا", "Syria", 33.5138, 36.2765, 680.0, "Asia/Damascus", CalculationMethod.MWL),
        CityPreset("بغداد", "Baghdad", "العراق", "Iraq", 33.3152, 44.3661, 34.0, "Asia/Baghdad", CalculationMethod.MWL),

        // Egypt
        CityPreset("القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357, 23.0, "Africa/Cairo", CalculationMethod.EGYPT),
        CityPreset("الإسكندرية", "Alexandria", "مصر", "Egypt", 31.2001, 29.9187, 5.0, "Africa/Cairo", CalculationMethod.EGYPT),

        // North Africa
        CityPreset("الجزائر", "Algiers", "الجزائر", "Algeria", 36.7538, 3.0588, 10.0, "Africa/Algiers", CalculationMethod.MWL),
        CityPreset("تونس", "Tunis", "تونس", "Tunisia", 36.8065, 10.1815, 7.0, "Africa/Tunis", CalculationMethod.MWL),
        CityPreset("طرابلس", "Tripoli", "ليبيا", "Libya", 32.8872, 13.1913, 8.0, "Africa/Tripoli", CalculationMethod.MWL),
        CityPreset("الخرطوم", "Khartoum", "السودان", "Sudan", 15.5007, 32.5599, 381.0, "Africa/Khartoum", CalculationMethod.MWL),
        CityPreset("نواكشوط", "Nouakchott", "موريتانيا", "Mauritania", 18.0735, -15.9582, 7.0, "Africa/Nouakchott", CalculationMethod.MWL),

        // Gulf
        CityPreset("دبي", "Dubai", "الإمارات العربية المتحدة", "UAE", 25.2048, 55.2708, 5.0, "Asia/Dubai", CalculationMethod.MWL),
        CityPreset("أبو ظبي", "Abu Dhabi", "الإمارات العربية المتحدة", "UAE", 24.4539, 54.3773, 10.0, "Asia/Dubai", CalculationMethod.MWL),
        CityPreset("الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310, 13.0, "Asia/Qatar", CalculationMethod.QATAR),
        CityPreset("الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774, 15.0, "Asia/Kuwait", CalculationMethod.KUWAIT),
        CityPreset("المنامة", "Manama", "البحرين", "Bahrain", 26.2285, 50.5860, 2.0, "Asia/Bahrain", CalculationMethod.MWL),
        CityPreset("مسقط", "Muscat", "سلطنة عمان", "Oman", 23.5880, 58.3829, 10.0, "Asia/Muscat", CalculationMethod.MWL),

        // Turkey
        CityPreset("إسطنبول", "Istanbul", "تركيا", "Turkey", 41.0082, 28.9784, 40.0, "Europe/Istanbul", CalculationMethod.TURKEY),
        CityPreset("أنقرة", "Ankara", "تركيا", "Turkey", 39.9334, 32.8597, 938.0, "Europe/Istanbul", CalculationMethod.TURKEY),

        // Asia
        CityPreset("كراتشي", "Karachi", "باكستان", "Pakistan", 24.8607, 67.0011, 8.0, "Asia/Karachi", CalculationMethod.KARACHI),
        CityPreset("لاهور", "Lahore", "باكستان", "Pakistan", 31.5204, 74.3587, 217.0, "Asia/Karachi", CalculationMethod.KARACHI),
        CityPreset("إسلام آباد", "Islamabad", "باكستان", "Pakistan", 33.6844, 73.0479, 507.0, "Asia/Karachi", CalculationMethod.KARACHI),
        CityPreset("جاكرتا", "Jakarta", "إندونيسيا", "Indonesia", -6.2088, 106.8456, 8.0, "Asia/Jakarta", CalculationMethod.MWL),
        CityPreset("كوالالمبور", "Kuala Lumpur", "ماليزيا", "Malaysia", 3.1390, 101.6869, 21.0, "Asia/Kuala_Lumpur", CalculationMethod.MWL),
        CityPreset("دكا", "Dhaka", "بنغلاديش", "Bangladesh", 23.8103, 90.4125, 4.0, "Asia/Dhaka", CalculationMethod.KARACHI),

        // Europe & Americas
        CityPreset("لندن", "London", "المملكة المتحدة", "United Kingdom", 51.5074, -0.1278, 11.0, "Europe/London", CalculationMethod.MWL),
        CityPreset("باريس", "Paris", "فرنسا", "France", 48.8566, 2.3522, 35.0, "Europe/Paris", CalculationMethod.MWL),
        CityPreset("مدريد", "Madrid", "إسبانيا", "Spain", 40.4168, -3.7038, 650.0, "Europe/Madrid", CalculationMethod.MWL),
        CityPreset("نيويورك", "New York", "الولايات المتحدة", "USA", 40.7128, -74.0060, 10.0, "America/New_York", CalculationMethod.ISNA),
        CityPreset("تورونتو", "Toronto", "كندا", "Canada", 43.6532, -79.3832, 76.0, "America/Toronto", CalculationMethod.ISNA)
    )

    // Complete merged list: Morocco cities first, then international cities
    val list: List<CityPreset> = moroccoList + internationalList

    val defaultCity: CityPreset = moroccoList[0] // Casablanca default
}
