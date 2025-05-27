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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Initializes buttons and sets listeners to launch activities.

        RecipeFormatter.init(applicationContext) // Initialize early

        setContentView(R.layout.activity_main)

        val openCameraButton: Button = findViewById(R.id.openCameraButton)
        val loadPictureButton: Button = findViewById(R.id.openLoadPicture)
        val recipesButton: Button = findViewById(R.id.openRecipes)
        val ingredientsButton: Button = findViewById(R.id.addIngredients)

        openCameraButton.setOnClickListener {
            val intent = Intent(this, ImageRecipeActivity::class.java)
            startActivity(intent)
        }

        loadPictureButton.setOnClickListener {
            val intent = Intent(this, ImageRecipeActivity::class.java)
            startActivity(intent)
        }

        recipesButton.setOnClickListener {
            val intent = Intent(this, RecipesActivity::class.java)
            startActivity(intent)
        }

        ingredientsButton.setOnClickListener {
            val intent = Intent(this, IngredientsActivity::class.java)
            startActivity(intent)
        }
    }




    companion object {
        private const val GALLERY_REQUEST_CODE = 1002
    }
}