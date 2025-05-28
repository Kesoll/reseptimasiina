package com.example.reseptimasiina

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity that displays detailed information about a selected recipe.
 *
 * This activity receives recipe data through Intent extras and presents it in a
 * formatted, readable layout. It serves as the detailed view when users select
 * a recipe from a list or want to view the complete recipe information.
 *
 * Expected Intent extras:
 * - "title": Recipe name/title
 * - "description": Brief recipe description
 * - "ingredients": List of required ingredients
 * - "instructions": Step-by-step cooking instructions
 *
 * Typically launched from RecipesActivity when a user taps on a recipe item.
 */
class RecipeDetailActivity : AppCompatActivity() {

    /**
     * Initializes the recipe detail view and populates it with data from Intent extras.
     *
     * This method:
     * 1. Sets up the detail layout
     * 2. Extracts recipe data from the launching Intent
     * 3. Populates TextView components with the recipe information
     * 4. Handles potential null values gracefully
     *
     * The activity expects to receive complete recipe data through Intent extras,
     * which are then displayed in corresponding TextView components for easy reading.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Extract recipe data from Intent extras
        // These values may be null if not properly passed from the launching activity
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val ingredients = intent.getStringExtra("ingredients")
        val instructions = intent.getStringExtra("instructions")

        // Populate TextViews with recipe data
        // The text property handles null values by displaying empty strings
        findViewById<TextView>(R.id.recipeTitle).text = title
        findViewById<TextView>(R.id.recipeDescription).text = description
        findViewById<TextView>(R.id.recipeIngredients).text = ingredients
        findViewById<TextView>(R.id.recipeInstructions).text = instructions
    }
}