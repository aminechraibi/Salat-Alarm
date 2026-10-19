package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.CityPreset
import com.example.model.CityPresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class DetectedLocation(
    val cityNameArabic: String,
    val cityNameEnglish: String,
    val countryArabic: String,
    val countryEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val timeZoneId: String,
    val provider: String
)

object LocationHelper {

    private const val TAG = "LocationHelper"

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false

        return try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            isGpsEnabled || isNetworkEnabled
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getFreshLocation(context: Context): DetectedLocation? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted")
            return@withContext null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        var location: Location? = null

        // 1. Try to get best last known location if recent (< 10 minutes)
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        val tenMinutesAgo = System.currentTimeMillis() - 10 * 60 * 1000

        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val last = locationManager.getLastKnownLocation(provider)
                    if (last != null && last.time >= tenMinutesAgo) {
                        if (location == null || last.accuracy < location.accuracy) {
                            location = last
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error querying last known location from $provider", e)
            }
        }

        // 2. If no recent location, request a fresh location update with timeout
        if (location == null) {
            location = withTimeoutOrNull(10000) {
                requestSingleUpdate(context, locationManager)
            }
        }

        // 3. Fallback to any last known location regardless of age if still null
        if (location == null) {
            for (provider in providers) {
                try {
                    val last = locationManager.getLastKnownLocation(provider)
                    if (last != null) {
                        if (location == null || last.accuracy < location.accuracy) {
                            location = last
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        val finalLoc = location ?: return@withContext null

        // 4. Resolve city & country details
        val detected = resolveLocationDetails(context, finalLoc)
        detected
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleUpdate(context: Context, locationManager: LocationManager): Location? =
        suspendCancellableCoroutine { continuation ->
            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            }

            if (provider == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cancellationSignal = CancellationSignal()
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                try {
                    locationManager.getCurrentLocation(
                        provider,
                        cancellationSignal,
                        ContextCompat.getMainExecutor(context),
                        { loc ->
                            if (continuation.isActive) {
                                continuation.resume(loc)
                            }
                        }
                    )
                    return@suspendCancellableCoroutine
                } catch (e: Exception) {
                    Log.w(TAG, "getCurrentLocation failed, falling back to listener", e)
                }
            }

            // Fallback for API < 30 or if getCurrentLocation fails
            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    try {
                        locationManager.removeUpdates(this)
                    } catch (e: Exception) {
                        // ignore
                    }
                    if (continuation.isActive) {
                        continuation.resume(loc)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            continuation.invokeOnCancellation {
                try {
                    locationManager.removeUpdates(listener)
                } catch (e: Exception) {
                    // ignore
                }
            }

            try {
                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting location updates", e)
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }

    private fun resolveLocationDetails(context: Context, location: Location): DetectedLocation {
        val lat = location.latitude
        val lng = location.longitude
        val alt = if (location.hasAltitude()) location.altitude else 10.0
        val defaultTz = TimeZone.getDefault().id

        var cityNameAr: String? = null
        var cityNameEn: String? = null
        var countryNameAr: String? = null
        var countryNameEn: String? = null

        // Try Geocoder with Arabic locale
        try {
            if (Geocoder.isPresent()) {
                val geocoderAr = Geocoder(context, Locale("ar"))
                @Suppress("DEPRECATION")
                val addressesAr = geocoderAr.getFromLocation(lat, lng, 1)
                if (!addressesAr.isNullOrEmpty()) {
                    val addr = addressesAr[0]
                    cityNameAr = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    countryNameAr = addr.countryName
                }

                val geocoderEn = Geocoder(context, Locale.ENGLISH)
                @Suppress("DEPRECATION")
                val addressesEn = geocoderEn.getFromLocation(lat, lng, 1)
                if (!addressesEn.isNullOrEmpty()) {
                    val addr = addressesEn[0]
                    cityNameEn = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    countryNameEn = addr.countryName
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder resolution error", e)
        }

        // If Geocoder did not return a city name (e.g. offline device), find the nearest city preset
        val nearestPreset = findNearestPreset(lat, lng)

        val finalCityAr = cityNameAr ?: nearestPreset?.cityNameArabic ?: "موقعي الحالي (GPS)"
        val finalCityEn = cityNameEn ?: nearestPreset?.cityNameEnglish ?: "Current Location (GPS)"
        val finalCountryAr = countryNameAr ?: nearestPreset?.countryArabic ?: ""
        val finalCountryEn = countryNameEn ?: nearestPreset?.countryEnglish ?: ""
        val finalTz = nearestPreset?.timeZoneId ?: defaultTz

        return DetectedLocation(
            cityNameArabic = finalCityAr,
            cityNameEnglish = finalCityEn,
            countryArabic = finalCountryAr,
            countryEnglish = finalCountryEn,
            latitude = lat,
            longitude = lng,
            altitudeMeters = alt,
            timeZoneId = finalTz,
            provider = location.provider ?: "GPS"
        )
    }

    private fun findNearestPreset(lat: Double, lng: Double): CityPreset? {
        return CityPresets.list.minByOrNull { city ->
            haversineDistanceKm(lat, lng, city.latitude, city.longitude)
        }
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
