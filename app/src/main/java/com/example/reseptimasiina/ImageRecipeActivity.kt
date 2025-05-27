package com.example.reseptimasiina

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import okhttp3.*
import java.io.IOException

class ImageRecipeActivity : AppCompatActivity() {

    private lateinit var cameraButton: Button
    private lateinit var galleryButton: Button
    private lateinit var generateButton: Button
    private lateinit var imageView: ImageView
    private lateinit var additionalIngredientsLayout: LinearLayout
    private lateinit var responseTextView: TextView
    private var capturedImage: Bitmap? = null

    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                capturedImage = imageBitmap
                imageView.setImageBitmap(imageBitmap)
                generateButton.isEnabled = true
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                try {
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    capturedImage = imageBitmap
                    imageView.setImageBitmap(imageBitmap)
                    generateButton.isEnabled = true
                } catch (e: Exception) {
                    Log.e("ImageRecipeActivity", "Error loading image: ${e.message}")
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recipe)

        cameraButton = findViewById(R.id.cameraButton)
        galleryButton = findViewById(R.id.galleryButton)
        generateButton = findViewById(R.id.generateButton)
        imageView = findViewById(R.id.imagePreview)
        additionalIngredientsLayout = findViewById(R.id.additionalIngredientsContainer)
        responseTextView = findViewById(R.id.response)
        val backButton: ImageButton = findViewById(R.id.backButton)
        val addIngredientButton: Button = findViewById(R.id.addIngredientButton)

        generateButton.isEnabled = false
        addIngredientField()

        cameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
            }
        }

        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }

        generateButton.setOnClickListener {
            val image = capturedImage
            if (image != null) {
                Toast.makeText(this, "Analysoidaan kuvaa...", Toast.LENGTH_SHORT).show()
                val base64Image = bitmapToBase64(image)

                sendImageToOpenRouter(base64Image, "") { caption ->
                    RecipeFormatter.processRecipeResponse(caption) { formatted ->
                        runOnUiThread {
                            responseTextView.text = formatted
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Valitse kuva ensin", Toast.LENGTH_SHORT).show()
            }
        }

        addIngredientButton.setOnClickListener {
            addIngredientField()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun collectIngredients(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until additionalIngredientsLayout.childCount) {
            val editText = additionalIngredientsLayout.getChildAt(i) as? EditText
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
            hint = "Additional ingredient (optional)"
        }

        additionalIngredientsLayout.addView(newEditText)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    }

    private fun sendImageToOpenRouter(base64Image: String, prompt: String, callback: (String?) -> Unit) {
        val client = OkHttpClient()
        val apiKey = "API-KEY-HERE"
        val url = "https://openrouter.ai/api/v1/chat/completions"

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", "You are an AI that generates a recipe based on an image description. return the answer in json format with Title: , Description: Ingredients: and Instructions:")
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", "Analyze this image: data:image/jpeg;base64,$base64Image\nAdditional prompt: $prompt")
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messages)
        }

        val body = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ImageRecipeActivity", "OpenRouter request failed: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) {
                    callback(null)
                    return
                }

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val message = choices.getJSONObject(0).getJSONObject("message")
                        val content = message.optString("content", null)
                        callback(content)
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    Log.e("ImageRecipeActivity", "Failed to parse OpenRouter response: ${e.message}")
                    callback(null)
                }
            }
        })
    }
}
