package com.example.locationtrackerapp.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Service class for handling location-related operations.
 * Uses Google Play Services FusedLocationProviderClient to get current location.
 * 
 * This service handles permission checks and provides a clean interface
 * for getting the user's current location.
 */
class LocationService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        try {
            LocationServices.getFusedLocationProviderClient(context)
        } catch (e: Exception) {
            throw RuntimeException("Google Play Services not available", e)
        }
    }
    
    /**
     * Checks if location permissions are granted.
     * 
     * @return true if both fine and coarse location permissions are granted
     */
    fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && 
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Gets the current location of the device.
     * This is a suspend function that should be called from a coroutine.
     * 
     * @return Location object containing latitude, longitude, and other location data
     * @throws SecurityException if location permissions are not granted
     * @throws Exception if location cannot be obtained
     */
    suspend fun getCurrentLocation(): Location = suspendCoroutine { continuation ->
        if (!hasLocationPermissions()) {
            continuation.resumeWithException(
                SecurityException("Location permissions not granted")
            )
            return@suspendCoroutine
        }
        
        try {
            android.util.Log.d("LocationService", "Requesting fresh location...")
            
            // Always try to get fresh location first
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    android.util.Log.d("LocationService", "Got fresh location: ${location.latitude}, ${location.longitude}")
                    continuation.resume(location)
                } else {
                    android.util.Log.w("LocationService", "Fresh location is null, trying last known location...")
                    // If fresh location fails, try last known location
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            android.util.Log.d("LocationService", "Using last known location: ${lastLocation.latitude}, ${lastLocation.longitude}")
                            continuation.resume(lastLocation)
                        } else {
                            android.util.Log.e("LocationService", "No location available")
                            continuation.resumeWithException(
                                Exception("Unable to get current location - no location data available")
                            )
                        }
                    }.addOnFailureListener { exception ->
                        android.util.Log.e("LocationService", "Last location failed: ${exception.message}")
                        continuation.resumeWithException(exception)
                    }
                }
            }.addOnFailureListener { exception ->
                android.util.Log.e("LocationService", "Fresh location failed: ${exception.message}")
                // If fresh location fails, try last known location
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                    if (lastLocation != null) {
                        android.util.Log.d("LocationService", "Using last known location as fallback: ${lastLocation.latitude}, ${lastLocation.longitude}")
                        continuation.resume(lastLocation)
                    } else {
                        android.util.Log.e("LocationService", "No fallback location available")
                        continuation.resumeWithException(exception)
                    }
                }.addOnFailureListener { lastLocationException ->
                    android.util.Log.e("LocationService", "All location methods failed")
                    continuation.resumeWithException(exception)
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationService", "Security exception: ${e.message}")
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Gets the last known location of the device.
     * This is faster than getCurrentLocation() but may be less accurate.
     * 
     * @return Location object or null if no last known location
     * @throws SecurityException if location permissions are not granted
     */
    suspend fun getLastKnownLocation(): Location? = suspendCoroutine { continuation ->
        if (!hasLocationPermissions()) {
            continuation.resumeWithException(
                SecurityException("Location permissions not granted")
            )
            return@suspendCoroutine
        }
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                continuation.resume(location)
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        } catch (e: SecurityException) {
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Forces a fresh location request by clearing any cached data.
     * This method should be used when you need the most current location.
     * 
     * @return Location object containing fresh latitude, longitude, and other location data
     * @throws SecurityException if location permissions are not granted
     * @throws Exception if location cannot be obtained
     */
    suspend fun getFreshLocation(): Location = suspendCoroutine { continuation ->
        if (!hasLocationPermissions()) {
            continuation.resumeWithException(
                SecurityException("Location permissions not granted")
            )
            return@suspendCoroutine
        }
        
        try {
            android.util.Log.d("LocationService", "Requesting fresh location (forced)...")
            
            // Force fresh location request
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    android.util.Log.d("LocationService", "Got forced fresh location: ${location.latitude}, ${location.longitude}")
                    continuation.resume(location)
                } else {
                    android.util.Log.e("LocationService", "Forced fresh location is null")
                    continuation.resumeWithException(
                        Exception("Unable to get fresh location - location data is null")
                    )
                }
            }.addOnFailureListener { exception ->
                android.util.Log.e("LocationService", "Forced fresh location failed: ${exception.message}")
                continuation.resumeWithException(exception)
            }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationService", "Security exception in fresh location: ${e.message}")
            continuation.resumeWithException(e)
        }
    }
}
