package com.example.reseptimasiina

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val ingredients = intent.getStringExtra("ingredients")
        val instructions = intent.getStringExtra("instructions")

        findViewById<TextView>(R.id.recipeTitle).text = title
        findViewById<TextView>(R.id.recipeDescription).text = description
        findViewById<TextView>(R.id.recipeIngredients).text = ingredients
        findViewById<TextView>(R.id.recipeInstructions).text = instructions
    }
}
