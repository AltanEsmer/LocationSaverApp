package com.example.locationtrackerapp.repository

import com.example.locationtrackerapp.data.LocationDao
import com.example.locationtrackerapp.data.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that manages location data operations.
 * Acts as a single source of truth for location data and provides
 * a clean interface between the UI and data sources.
 * 
 * This repository handles all database operations for locations
 * and can be easily extended to support additional data sources
 * like remote APIs in the future.
 */
class LocationRepository(private val locationDao: LocationDao) {
    
    /**
     * Get all saved locations as a Flow for reactive UI updates.
     * 
     * @return Flow of list of locations ordered by timestamp (newest first)
     */
    fun getAllLocations(): Flow<List<LocationEntity>> = locationDao.getAllLocations()
    
    /**
     * Save a new location to the database.
     * 
     * @param name User-defined name for the location
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return The ID of the saved location
     */
    suspend fun saveLocation(name: String, latitude: Double, longitude: Double): Long {
        val location = LocationEntity(
            name = name,
            latitude = latitude,
            longitude = longitude
        )
        return locationDao.insertLocation(location)
    }
    
    /**
     * Get a specific location by ID.
     * 
     * @param id The ID of the location to retrieve
     * @return The location entity or null if not found
     */
    suspend fun getLocationById(id: Long): LocationEntity? = locationDao.getLocationById(id)
    
    /**
     * Delete a location by ID.
     * 
     * @param id The ID of the location to delete
     */
    suspend fun deleteLocation(id: Long) = locationDao.deleteLocation(id)
    
    /**
     * Delete all saved locations.
     */
    suspend fun deleteAllLocations() = locationDao.deleteAllLocations()
}
