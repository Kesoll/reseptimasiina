package com.example.reseptimasiina

import android.content.Context
import android.util.Log
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Singleton object responsible for processing and formatting recipe responses from external sources.
 *
 * This utility class handles parsing JSON recipe data, saving recipes to the local database,
 * and formatting recipe content for display in the UI with proper HTML styling.
 *
 * Must be initialized with a Context before use to establish database connectivity.
 */
object RecipeFormatter {

    /** Log tag for debugging and error tracking */
    private const val TAG = "RecipeFormatter"

    /**
     * Database access object for recipe operations.
     * Must be initialized via init() method before using other functions.
     */
    lateinit var recipeDao: RecipeDao

    /**
     * Initializes the RecipeFormatter with database access.
     *
     * This method must be called once during app startup before using any other
     * RecipeFormatter functions. It establishes the connection to the Room database
     * and prepares the DAO for recipe storage operations.
     *
     * @param context Android context needed for database initialization
     */
    fun init(context: Context) {
        val db = AppDatabase.getDatabase(context)
        recipeDao = db.recipeDao()
    }

    /**
     * Processes a raw recipe response string and formats it for display.
     *
     * This method performs several key operations:
     * 1. Extracts JSON data from response (handles both plain JSON and markdown-wrapped JSON)
     * 2. Parses the JSON to extract recipe components (title, description, ingredients, instructions)
     * 3. Saves the recipe to the local database asynchronously
     * 4. Formats the recipe as HTML with emojis and styling for UI display
     * 5. Returns the formatted content via callback
     *
     * Expected JSON format:
     * {
     *   "Title": "Recipe Name",
     *   "Description": "Recipe description",
     *   "Ingredients": ["ingredient1", "ingredient2", ...],
     *   "Instructions": ["step1", "step2", ...]
     * }
     *
     * @param response Raw response string containing recipe data (JSON or markdown-wrapped JSON)
     * @param callback Function called with the formatted HTML content or error message
     */
    fun processRecipeResponse(response: String?, callback: (CharSequence) -> Unit) {
        // Handle null response with error message
        if (response == null) {
            callback("Error getting reply.")
            return
        }

        // Extract JSON from markdown code blocks or use response as-is
        // Regex pattern matches: ```json { ... } ```
        val jsonRegex = Regex("```json\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL)
        val match = jsonRegex.find(response)
        val jsonText = match?.groupValues?.get(1) ?: response

        try {
            // Parse the JSON response into recipe components
            val recipeJson = JSONObject(jsonText)
            val title = recipeJson.optString("Title", "No title")
            val description = recipeJson.optString("Description", "")
            val ingredientsArray = recipeJson.optJSONArray("Ingredients")
            val instructionsArray = recipeJson.optJSONArray("Instructions")

            // Convert ingredient JSON array to newline-separated string for database storage
            val ingredients = buildString {
                ingredientsArray?.let {
                    for (i in 0 until it.length()) {
                        append(it.getString(i))
                        if (i < it.length() - 1) append("\n")
                    }
                }
            }

            // Convert instructions JSON array to newline-separated string for database storage
            val instructions = buildString {
                instructionsArray?.let {
                    for (i in 0 until it.length()) {
                        append(it.getString(i))
                        if (i < it.length() - 1) append("\n")
                    }
                }
            }

            // Save recipe to database asynchronously on IO thread
            // This prevents blocking the UI thread during database operations
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val recipeEntity = RecipeEntity(
                        title = title,
                        description = description,
                        ingredients = ingredients,
                        instructions = instructions
                    )
                    recipeDao.insert(recipeEntity)
                    Log.d(TAG, "Recipe saved to DB: $title")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving recipe to DB: ${e.message}", e)
                }
            }

            // Build HTML-formatted string for display with emojis and styling
            val html = buildString {
                // Recipe title with cooking emoji
                append("🍽️ <b>$title</b><br><br>")

                // Optional description with memo emoji
                if (description.isNotEmpty()) append("📝 $description<br><br>")

                // Ingredients section with salt emoji and bullet points
                append("<b>🧂 Ingredients:</b><br>")
                ingredientsArray?.let {
                    for (i in 0 until it.length()) {
                        append("- ${it.getString(i)}<br>")
                    }
                }

                // Instructions section with chef emoji and bullet points
                append("<br><b>👩‍🍳 Instructions:</b><br>")
                instructionsArray?.let {
                    for (i in 0 until it.length()) {
                        append("• ${it.getString(i)}<br><br>")
                    }
                }
            }

            // Convert HTML string to formatted CharSequence for TextView display
            val formattedText = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
            callback(formattedText)

        } catch (e: Exception) {
            // Handle JSON parsing errors and return error message
            Log.e(TAG, "JSON parsing error: ${e.message}", e)
            callback("JSON parsing error: ${e.message}")
        }
    }
}