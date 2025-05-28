package com.example.reseptimasiina

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) interface for Recipe database operations.
 *
 * Provides a clean API for performing CRUD (Create, Read, Update, Delete) operations
 * on the recipes table using Room persistence library. All operations are suspend
 * functions to support coroutine-based asynchronous database access.
 *
 * This interface abstracts the SQL operations and provides type-safe methods
 * for interacting with recipe data.
 */
@Dao
interface RecipeDao {

    /**
     * Inserts a new recipe into the database or replaces an existing one.
     *
     * Uses REPLACE conflict strategy, which means if a recipe with the same
     * primary key already exists, it will be completely replaced with the new data.
     * This is useful for updating recipes or handling duplicate insertions gracefully.
     *
     * @param recipe The RecipeEntity object to insert or update
     * @return The row ID of the newly inserted recipe (Long value)
     *
     * Usage: Saving new recipes from AI generation or updating existing recipes
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    /**
     * Retrieves a specific recipe by its unique identifier.
     *
     * Performs a SELECT query to find a recipe with the matching ID.
     * Returns null if no recipe is found with the specified ID.
     *
     * @param id The unique identifier of the recipe to retrieve
     * @return RecipeEntity object if found, null otherwise
     *
     * Usage: Loading recipe details for viewing or editing
     */
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): RecipeEntity?

    /**
     * Retrieves all recipes from the database.
     *
     * Performs a SELECT ALL query to fetch every recipe stored in the database.
     * Returns an empty list if no recipes are found.
     *
     * @return List of all RecipeEntity objects in the database
     *
     * Usage: Displaying recipe lists, populating RecyclerView adapters
     */
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeEntity>

    /**
     * Deletes a specific recipe by its unique identifier.
     *
     * Removes the recipe with the matching ID from the database.
     * Returns the number of rows affected (0 if no recipe was found, 1 if deleted).
     *
     * @param id The unique identifier of the recipe to delete
     * @return Int representing the number of rows deleted (0 or 1)
     *
     * Usage: Removing unwanted recipes, implementing delete functionality in UI
     */
    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Int): Int  // optionally return number of rows deleted

    /**
     * Deletes all recipes from the database.
     *
     * Removes every recipe from the recipes table. This operation cannot be undone,
     * so it should be used carefully (typically with user confirmation).
     *
     * @return Int representing the total number of rows deleted
     *
     * Usage: Clear all data functionality, app reset, or testing cleanup
     */
    @Query("DELETE FROM recipes")
    suspend fun deleteAll(): Int
}