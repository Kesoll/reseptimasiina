package com.example.reseptimasiina

import RecipeAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import android.content.Intent


class RecipesActivity : AppCompatActivity() {

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes) // create this layout with a RecyclerView inside

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recipeAdapter = RecipeAdapter { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java).apply {
                putExtra("title", recipe.title)
                putExtra("description", recipe.description)
                putExtra("ingredients", recipe.ingredients)
                putExtra("instructions", recipe.instructions)
            }
            startActivity(intent)
        }
        recyclerView.adapter = recipeAdapter

        // Fetch recipes from DB asynchronously using a coroutine
        lifecycleScope.launch {
            val recipes = RecipeFormatter.recipeDao.getAllRecipes()
            recipeAdapter.submitList(recipes)
        }
    }
}
