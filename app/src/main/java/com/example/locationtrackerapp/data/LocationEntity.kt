package com.example.locationtrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity class representing a saved location in the Room database.
 * 
 * @property id Unique identifier for the location (auto-generated)
 * @property name User-defined name for the location
 * @property latitude Latitude coordinate of the location
 * @property longitude Longitude coordinate of the location
 * @property address Full address of the location
 * @property category Category of the location (Home, Work, Customer, etc.)
 * @property notes Additional notes about the location
 * @property isFavorite Whether this is a favorite location
 * @property visitCount How many times this location has been visited
 * @property lastVisited When this location was last visited
 * @property timestamp When the location was saved
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val category: LocationCategory = LocationCategory.OTHER,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val visitCount: Int = 0,
    val lastVisited: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Enum representing the category of a location.
 */
enum class LocationCategory {
    HOME,           // Ev
    WORK,           // İş
    CUSTOMER,       // Müşteri
    RESTAURANT,     // Restoran
    CAFE,           // Kafe
    HOSPITAL,       // Hastane
    SCHOOL,         // Okul
    OTHER           // Diğer
}
