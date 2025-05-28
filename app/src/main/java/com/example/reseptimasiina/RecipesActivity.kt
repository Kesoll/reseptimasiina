package com.example.reseptimasiina

import RecipeAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import android.content.Intent

/**
 * Activity that displays a list of saved recipes in a RecyclerView.
 *
 * This activity serves as the main recipe browsing screen, showing all recipes
 * stored in the local database. Users can tap on any recipe to view its full
 * details in a separate activity.
 *
 * The activity uses a RecyclerView with LinearLayoutManager for efficient
 * scrolling through large recipe collections and leverages coroutines for
 * asynchronous database operations.
 */
class RecipesActivity : AppCompatActivity() {

    /**
     * Adapter for managing recipe data display in the RecyclerView.
     * Handles the binding of recipe data to individual list item views.
     */
    private lateinit var recipeAdapter: RecipeAdapter

    /**
     * RecyclerView component for displaying the scrollable list of recipes.
     * Provides efficient view recycling for large datasets.
     */
    private lateinit var recyclerView: RecyclerView

    /**
     * Initializes the activity and sets up the recipe list display.
     *
     * This method performs the following setup operations:
     * 1. Sets the activity layout containing the RecyclerView
     * 2. Configures the RecyclerView with LinearLayoutManager for vertical scrolling
     * 3. Creates and sets up the RecipeAdapter with click handling
     * 4. Loads all recipes from the database asynchronously
     * 5. Populates the RecyclerView with the loaded recipe data
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, or null
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout file that contains a RecyclerView with id "recyclerView"
        setContentView(R.layout.activity_recipes)

        // Initialize RecyclerView and configure it for vertical scrolling
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create adapter with click listener that navigates to recipe details
        // The lambda function defines what happens when a user taps on a recipe item
        recipeAdapter = RecipeAdapter { recipe ->
            // Create intent to launch RecipeDetailActivity with recipe data
            val intent = Intent(this, RecipeDetailActivity::class.java).apply {
                // Pass all recipe details as extras to the detail activity
                putExtra("title", recipe.title)
                putExtra("description", recipe.description)
                putExtra("ingredients", recipe.ingredients)
                putExtra("instructions", recipe.instructions)
            }
            // Start the detail activity with the recipe data
            startActivity(intent)
        }

        // Attach the adapter to the RecyclerView
        recyclerView.adapter = recipeAdapter

        // Load recipes from database asynchronously to avoid blocking the UI thread
        // lifecycleScope ensures the coroutine is cancelled if the activity is destroyed
        lifecycleScope.launch {
            // Fetch all recipes from the database on a background thread
            val recipes = RecipeFormatter.recipeDao.getAllRecipes()

            // Update the adapter with the loaded recipes on the main thread
            // submitList() efficiently updates the RecyclerView with new data
            recipeAdapter.submitList(recipes)
        }
    }
}