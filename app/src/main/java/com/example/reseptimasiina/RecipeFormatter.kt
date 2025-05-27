package com.example.reseptimasiina

import android.content.Context
import android.util.Log
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

object RecipeFormatter {

    private const val TAG = "RecipeFormatter"

    // We need the DAO to save recipes
    lateinit var recipeDao: RecipeDao


    // Initialize with DAO, call this once in your app before using RecipeFormatter
    fun init(context: Context) {
        val db = AppDatabase.getDatabase(context)
        recipeDao = db.recipeDao()
    }

    fun processRecipeResponse(response: String?, callback: (CharSequence) -> Unit) {
        if (response == null) {
            callback("Error getting reply.")
            return
        }

        // Try to extract JSON inside ```json ... ``` or use plain response
        val jsonRegex = Regex("```json\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL)
        val match = jsonRegex.find(response)
        val jsonText = match?.groupValues?.get(1) ?: response

        try {
            val recipeJson = JSONObject(jsonText)
            val title = recipeJson.optString("Title", "No title")
            val description = recipeJson.optString("Description", "")
            val ingredientsArray = recipeJson.optJSONArray("Ingredients")
            val instructionsArray = recipeJson.optJSONArray("Instructions")

            // Convert JSONArrays to plain text strings separated by newlines or some delimiter
            val ingredients = buildString {
                ingredientsArray?.let {
                    for (i in 0 until it.length()) {
                        append(it.getString(i))
                        if (i < it.length() - 1) append("\n")
                    }
                }
            }

            val instructions = buildString {
                instructionsArray?.let {
                    for (i in 0 until it.length()) {
                        append(it.getString(i))
                        if (i < it.length() - 1) append("\n")
                    }
                }
            }

            // Save to DB asynchronously
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

            val html = buildString {
                append("🍽️ <b>$title</b><br><br>")
                if (description.isNotEmpty()) append("📝 $description<br><br>")
                append("<b>🧂 Ingredients:</b><br>")
                ingredientsArray?.let {
                    for (i in 0 until it.length()) {
                        append("- ${it.getString(i)}<br>")
                    }
                }
                append("<br><b>👩‍🍳 Instructions:</b><br>")
                instructionsArray?.let {
                    for (i in 0 until it.length()) {
                        append("• ${it.getString(i)}<br><br>")
                    }
                }
            }

            val formattedText = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
            callback(formattedText)

        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error: ${e.message}", e)
            callback("JSON parsing error: ${e.message}")
        }
    }
}
