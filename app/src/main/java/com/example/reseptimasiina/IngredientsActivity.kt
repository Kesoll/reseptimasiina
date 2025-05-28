package com.example.reseptimasiina

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity that allows users to input ingredients and generate recipes using AI.
 *
 * Features:
 * - Dynamic ingredient input fields that auto-expand as users type
 * - Recipe generation from ingredient lists using DeepSeek API
 * - Automatic UI updates with generated recipe content
 * - Clean, user-friendly interface for ingredient management
 */
class IngredientsActivity : AppCompatActivity() {

    // UI Components
    private lateinit var ingredientsLayout: LinearLayout
    private lateinit var addButton: Button
    private lateinit var responseTextView: TextView

    /**
     * Internal list to store ingredient strings (currently unused but available for future features)
     */
    private val ingredientList = mutableListOf<String>()

    /**
     * Initializes the activity and sets up the user interface.
     *
     * Sets up:
     * - View references from the layout
     * - Initial ingredient input field
     * - Click listeners for generate recipe and back navigation
     * - Recipe generation workflow using DeepSeek service
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)

        // Initialize UI components
        ingredientsLayout = findViewById(R.id.ingredientContainer)
        addButton = findViewById(R.id.saveIngredientButton)
        responseTextView = findViewById(R.id.response)
        val backButton: ImageButton = findViewById(R.id.backButton)

        // Add the first ingredient input field
        addIngredientField()

        // Generate recipe button click handler
        addButton.setOnClickListener {
            val allInputs = collectIngredients()
            if (allInputs.isNotEmpty()) {
                Toast.makeText(this, "Generating recipe...", Toast.LENGTH_SHORT).show()
                // Use DeepSeek service to generate recipe from ingredients
                DeepSeekService.getRecipeFromIngredients(allInputs) { response ->
                    responseTextView.text = response
                    Log.d("debug", "Response: $response")
                }
            }
        }

        // Back button navigation
        backButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Collects all non-empty ingredient text from the dynamically created EditText fields.
     *
     * Iterates through all child views in the ingredients layout container,
     * extracts text content from EditText fields, and filters out empty entries.
     *
     * @return List<String> containing all valid ingredient entries
     */
    private fun collectIngredients(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until ingredientsLayout.childCount) {
            val editText = ingredientsLayout.getChildAt(i) as? EditText
            val text = editText?.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) list.add(text)
        }
        return list
    }

    /**
     * Dynamically creates and adds a new ingredient input field to the layout.
     *
     * Features:
     * - Auto-expanding functionality: when user finishes typing in the last field,
     *   a new empty field automatically appears
     * - Proper layout parameters for consistent UI appearance
     * - Focus change listener for seamless user experience
     *
     * The auto-expansion works by detecting when a user moves focus away from
     * a non-empty field that happens to be the last field in the container.
     */
    private fun addIngredientField() {
        val newEditText = EditText(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            hint = "Add ingredient"
        }

        /**
         * Focus change listener that implements auto-expansion behavior.
         *
         * When user finishes typing in the last ingredient field (loses focus),
         * and the field contains text, automatically adds a new empty field below it.
         * This creates a seamless experience where users always have an empty
         * field available for the next ingredient.
         */
        newEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !newEditText.text.isNullOrEmpty()) {
                val lastChild = ingredientsLayout.getChildAt(ingredientsLayout.childCount - 1)
                if (lastChild == newEditText) {
                    addIngredientField()
                }
            }
        }

        // Add the new field to the layout container
        ingredientsLayout.addView(newEditText)
    }
}