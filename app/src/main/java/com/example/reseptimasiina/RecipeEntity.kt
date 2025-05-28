package com.example.reseptimasiina

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a recipe entity in the Room database.
 *
 * This class defines the structure of the "recipes" table in the local database,
 * storing all necessary information for a complete recipe including title,
 * description, ingredients list, and cooking instructions.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    /**
     * Primary key for the recipe entity.
     *
     * Auto-generates unique integer IDs starting from 1 for each new recipe.
     * Defaults to 0 when creating new instances (Room will assign the actual ID).
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * The name/title of the recipe.
     *
     * Stores the recipe's display name that users will see in lists and headers.
     * Should be a concise, descriptive name for the dish.
     */
    val title: String,

    /**
     * Brief description or summary of the recipe.
     *
     * Contains additional details about the dish such as cuisine type,
     * difficulty level, cooking time, or any special notes about the recipe.
     */
    val description: String,

    /**
     * Complete list of ingredients needed for the recipe.
     *
     * Stored as a single string containing all ingredients with quantities.
     * Format should include measurements and ingredient names
     * (e.g., "2 cups flour\n1 tsp salt\n3 eggs").
     */
    val ingredients: String,

    /**
     * Step-by-step cooking instructions.
     *
     * Contains the complete procedure for preparing the recipe.
     * Should be formatted as clear, sequential steps that guide the user
     * through the entire cooking process from preparation to completion.
     */
    val instructions: String
)