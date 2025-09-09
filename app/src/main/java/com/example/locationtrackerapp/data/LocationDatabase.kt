package com.example.locationtrackerapp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Room database class for the Location Saver App.
 * Manages the local SQLite database for storing saved locations.
 * 
 * This is a singleton database that provides access to the LocationDao.
 */
@Database(
    entities = [LocationEntity::class, OrderEntity::class, CustomerEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LocationDatabase : RoomDatabase() {
    
    /**
     * Provides access to the LocationDao for database operations.
     */
    abstract fun locationDao(): LocationDao
    
    /**
     * Provides access to the OrderDao for order operations.
     */
    abstract fun orderDao(): OrderDao
    
    /**
     * Provides access to the CustomerDao for customer operations.
     */
    abstract fun customerDao(): CustomerDao
    
    companion object {
        @Volatile
        private var INSTANCE: LocationDatabase? = null
        
        /**
         * Gets the database instance. Creates a new one if it doesn't exist.
         * Uses singleton pattern to ensure only one database instance exists.
         * 
         * @param context Application context
         * @return LocationDatabase instance
         */
        fun getDatabase(context: Context): LocationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocationDatabase::class.java,
                    "location_database"
                )
                .fallbackToDestructiveMigration() // Add this to handle migration issues
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
