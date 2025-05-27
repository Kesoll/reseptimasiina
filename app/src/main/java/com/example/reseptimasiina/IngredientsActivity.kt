package com.example.reseptimasiina

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class IngredientsActivity : AppCompatActivity() {

    private lateinit var ingredientsLayout: LinearLayout
    private lateinit var addButton: Button
    private lateinit var responseTextView: TextView
    private val ingredientList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)

        ingredientsLayout = findViewById(R.id.ingredientContainer)
        addButton = findViewById(R.id.saveIngredientButton)
        responseTextView = findViewById(R.id.response)
        val backButton: ImageButton = findViewById(R.id.backButton)

        addIngredientField()

        addButton.setOnClickListener {
            val allInputs = collectIngredients()
            if (allInputs.isNotEmpty()) {
                Toast.makeText(this, "Generating recipe...", Toast.LENGTH_SHORT).show()
                // Use our new service instead of the original implementation
                DeepSeekService.getRecipeFromIngredients(allInputs) { response ->
                    responseTextView.text = response
                    Log.d("debug", "Response: $response")
                }
            }
        }

        backButton.setOnClickListener {
            finish()

        }
    }

    private fun collectIngredients(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until ingredientsLayout.childCount) {
            val editText = ingredientsLayout.getChildAt(i) as? EditText
            val text = editText?.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) list.add(text)
        }
        return list
    }

    private fun addIngredientField() {
        val newEditText = EditText(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            hint = "Add ingredient"
        }

        newEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !newEditText.text.isNullOrEmpty()) {
                val lastChild = ingredientsLayout.getChildAt(ingredientsLayout.childCount - 1)
                if (lastChild == newEditText) {
                    addIngredientField()
                }
            }
        }

        ingredientsLayout.addView(newEditText)
    }
}