package com.example.locationtrackerapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for LocationEntity operations.
 * Provides methods to interact with the locations table in the Room database.
 */
@Dao
interface LocationDao {
    
    /**
     * Insert a new location into the database.
     * 
     * @param location The location entity to insert
     * @return The ID of the inserted location
     */
    @Insert
    suspend fun insertLocation(location: LocationEntity): Long
    
    /**
     * Get all saved locations ordered by timestamp (newest first).
     * 
     * @return Flow of list of locations for reactive UI updates
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>
    
    /**
     * Get locations by category.
     * 
     * @param category The category to filter by
     * @return Flow of list of locations in the specified category
     */
    @Query("SELECT * FROM locations WHERE category = :category ORDER BY timestamp DESC")
    fun getLocationsByCategory(category: LocationCategory): Flow<List<LocationEntity>>
    
    /**
     * Get favorite locations.
     * 
     * @return Flow of list of favorite locations
     */
    @Query("SELECT * FROM locations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteLocations(): Flow<List<LocationEntity>>
    
    /**
     * Search locations by name or address.
     * 
     * @param query Search query
     * @return Flow of list of matching locations
     */
    @Query("SELECT * FROM locations WHERE name LIKE :query OR address LIKE :query ORDER BY timestamp DESC")
    fun searchLocations(query: String): Flow<List<LocationEntity>>
    
    /**
     * Update location visit count and last visited time.
     * 
     * @param id Location ID
     * @param timestamp Current timestamp
     */
    @Query("UPDATE locations SET visitCount = visitCount + 1, lastVisited = :timestamp WHERE id = :id")
    suspend fun incrementVisitCount(id: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Update favorite status of a location.
     * 
     * @param id Location ID
     * @param isFavorite New favorite status
     */
    @Query("UPDATE locations SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    
    /**
     * Get a specific location by ID.
     * 
     * @param id The ID of the location to retrieve
     * @return The location entity or null if not found
     */
    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Long): LocationEntity?
    
    /**
     * Delete a location by ID.
     * 
     * @param id The ID of the location to delete
     */
    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteLocation(id: Long)
    
    /**
     * Delete all locations from the database.
     */
    @Query("DELETE FROM locations")
    suspend fun deleteAllLocations()
}
