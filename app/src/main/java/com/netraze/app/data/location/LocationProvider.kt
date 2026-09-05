package com.netraze.app.data.location

data class DeviceLocationFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double,
    val capturedAt: Long
)

interface LocationProvider {
    suspend fun getCurrentLocation(): DeviceLocationFix
}
