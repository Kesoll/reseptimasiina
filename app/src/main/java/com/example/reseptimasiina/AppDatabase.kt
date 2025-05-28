package com.example.reseptimasiina

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database class for the Recipe application.
 *
 * This abstract class serves as the main database holder and provides access to DAOs.
 * It's configured to use RecipeEntity as its only table with version 1.
 *
 * @Database annotation parameters:
 * - entities: Array of entity classes that belong to this database
 * - version: Database schema version number for migrations
 * - exportSchema: Whether to export database schema to a folder (disabled for simplicity)
 */
@Database(entities = [RecipeEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to recipe-related database operations.
     *
     * @return RecipeDao instance for performing CRUD operations on recipes
     */
    abstract fun recipeDao(): RecipeDao

    companion object {
        /**
         * Singleton instance of the database.
         * @Volatile ensures that changes to INSTANCE are immediately visible to all threads
         */
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton database instance using the double-checked locking pattern.
         *
         * This method ensures thread-safe creation of the database instance while avoiding
         * the overhead of synchronization after the instance is created.
         *
         * @param context Application context used to create the database
         * @return AppDatabase singleton instance
         */
        fun getDatabase(context: Context): AppDatabase {
            // Return existing instance if available, otherwise create new one
            return INSTANCE ?: synchronized(this) {
                // Double-check: another thread might have created instance while waiting for lock
                val instance = Room.databaseBuilder(
                    context.applicationContext,  // Use application context to prevent memory leaks
                    AppDatabase::class.java,     // Database class type
                    "recipe_database"            // Database file name
                ).build()

                // Store the instance and return it
                INSTANCE = instance
                instance
            }
        }
    }
}