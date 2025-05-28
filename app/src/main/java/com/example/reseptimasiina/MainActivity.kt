package com.example.reseptimasiina

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reseptimasiina.RecipeFormatter

/**
 * Main entry point activity for the Recipe Machine (Reseptimasiina) application.
 *
 * Serves as the navigation hub that provides access to all major app features:
 * - Image-based recipe generation (camera/gallery)
 * - Ingredient-based recipe generation
 * - Saved recipes viewing
 *
 * This activity acts as a launcher screen with navigation buttons to different
 * functional areas of the application.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Initializes the main activity and sets up the navigation interface.
     *
     * Responsibilities:
     * - Initializes the RecipeFormatter service for the entire application
     * - Sets up the main layout with navigation buttons
     * - Configures click listeners for each major app feature
     * - Provides seamless navigation to specialized activities
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Initializes buttons and sets listeners to launch activities.

        // Initialize RecipeFormatter early to ensure it's available throughout the app lifecycle
        RecipeFormatter.init(applicationContext)

        setContentView(R.layout.activity_main)

        // Initialize navigation buttons from the layout
        val openCameraButton: Button = findViewById(R.id.openCameraButton)
        val loadPictureButton: Button = findViewById(R.id.openLoadPicture)
        val recipesButton: Button = findViewById(R.id.openRecipes)
        val ingredientsButton: Button = findViewById(R.id.addIngredients)

        /**
         * Camera button click handler.
         * Launches ImageRecipeActivity for camera-based recipe generation.
         * Users can capture photos of food/ingredients to generate recipes.
         */
        openCameraButton.setOnClickListener {
            val intent = Intent(this, ImageRecipeActivity::class.java)
            startActivity(intent)
        }

        /**
         * Load picture button click handler.
         * Launches ImageRecipeActivity for gallery-based recipe generation.
         * Users can select existing photos from device gallery to generate recipes.
         *
         * Note: Both camera and load picture buttons currently launch the same activity,
         * where ImageRecipeActivity handles both camera capture and gallery selection internally.
         */
        loadPictureButton.setOnClickListener {
            val intent = Intent(this, ImageRecipeActivity::class.java)
            startActivity(intent)
        }

        /**
         * Recipes button click handler.
         * Launches RecipesActivity to view and manage saved recipes.
         * Provides access to recipe history and favorites functionality.
         */
        recipesButton.setOnClickListener {
            val intent = Intent(this, RecipesActivity::class.java)
            startActivity(intent)
        }

        /**
         * Ingredients button click handler.
         * Launches IngredientsActivity for text-based recipe generation.
         * Users can input available ingredients to get recipe suggestions.
         */
        ingredientsButton.setOnClickListener {
            val intent = Intent(this, IngredientsActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        /**
         * Request code for gallery image selection operations.
         * Currently unused but available for future gallery-specific functionality
         * that might be handled directly in MainActivity rather than delegated
         * to ImageRecipeActivity.
         */
        private const val GALLERY_REQUEST_CODE = 1002
    }
}