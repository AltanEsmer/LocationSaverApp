package com.example.locationtrackerapp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

/**
 * Room database class for the Location Saver App.
 * Manages the local SQLite database for storing saved locations.
 *
 * This is a singleton database that provides access to the LocationDao.
 *
 * NOTE: The app used to also manage order and customer data (delivery
 * features that have since been removed from the UI). Those tables are
 * dropped by [MIGRATION_2_3] below. The "locations" table, which holds the
 * user's real saved-location data, is never touched by this migration.
 */
@Database(
    entities = [LocationEntity::class],
    version = 3,
    exportSchema = false
)
abstract class LocationDatabase : RoomDatabase() {

    /**
     * Provides access to the LocationDao for database operations.
     */
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: LocationDatabase? = null

        /**
         * Migrates from the old delivery-management schema (v2, which included
         * "orders" and "customers" tables) to the location-only schema (v3).
         * Only the unused "orders" and "customers" tables are dropped here;
         * the "locations" table and all of its rows are left completely
         * untouched, so existing saved locations are preserved.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS orders")
                db.execSQL("DROP TABLE IF EXISTS customers")
            }
        }

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
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
