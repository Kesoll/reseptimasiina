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

/**
 * Activity that allows users to capture or select images of food/ingredients
 * and generate recipes using AI image analysis through OpenRouter API.
 *
 * Features:
 * - Camera capture functionality with permission handling
 * - Gallery image selection
 * - Dynamic ingredient input fields
 * - AI-powered recipe generation from images
 * - Image-to-base64 conversion for API transmission
 */
class ImageRecipeActivity : AppCompatActivity() {

    // UI Components
    private lateinit var cameraButton: Button
    private lateinit var galleryButton: Button
    private lateinit var generateButton: Button
    private lateinit var imageView: ImageView
    private lateinit var additionalIngredientsLayout: LinearLayout
    private lateinit var responseTextView: TextView

    /**
     * Stores the captured or selected image bitmap for processing
     */
    private var capturedImage: Bitmap? = null

    /**
     * Handles camera permission request result.
     * If permission is granted, opens camera; otherwise shows permission denied message.
     */
    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Handles camera capture result.
     * Extracts bitmap from camera intent extras and displays it in ImageView.
     * Enables generate button once image is successfully captured.
     */
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

    /**
     * Handles gallery image selection result.
     * Converts selected image URI to bitmap and displays it.
     * Enables generate button once image is successfully loaded.
     */
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

    /**
     * Initializes the activity, sets up UI components and event listeners.
     *
     * Sets up:
     * - View references from layout
     * - Button click listeners for camera, gallery, generate, and back actions
     * - Initial ingredient input field
     * - Generate button state (disabled until image is selected)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recipe)

        // Initialize UI components
        cameraButton = findViewById(R.id.cameraButton)
        galleryButton = findViewById(R.id.galleryButton)
        generateButton = findViewById(R.id.generateButton)
        imageView = findViewById(R.id.imagePreview)
        additionalIngredientsLayout = findViewById(R.id.additionalIngredientsContainer)
        responseTextView = findViewById(R.id.response)
        val backButton: ImageButton = findViewById(R.id.backButton)
        val addIngredientButton: Button = findViewById(R.id.addIngredientButton)

        // Disable generate button until image is selected
        generateButton.isEnabled = false
        addIngredientField()

        // Camera button: Check permission and open camera
        cameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
            }
        }

        // Gallery button: Open image picker
        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }

        // Generate button: Process image and generate recipe
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

        // Add ingredient button: Add new ingredient input field
        addIngredientButton.setOnClickListener {
            addIngredientField()
        }

        // Back button: Close activity
        backButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Launches camera intent for image capture.
     * Uses MediaStore.ACTION_IMAGE_CAPTURE to open the device's camera app.
     */
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    /**
     * Collects all non-empty ingredient text from dynamically added EditText fields.
     *
     * @return List of ingredient strings entered by the user
     */
    private fun collectIngredients(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until additionalIngredientsLayout.childCount) {
            val editText = additionalIngredientsLayout.getChildAt(i) as? EditText
            val text = editText?.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) list.add(text)
        }
        return list
    }

    /**
     * Dynamically adds a new EditText field for additional ingredient input.
     * Each field is added to the additionalIngredientsLayout with proper styling.
     */
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

    /**
     * Converts a Bitmap image to Base64 encoded string for API transmission.
     *
     * @param bitmap The image bitmap to convert
     * @return Base64 encoded string representation of the image
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    }

    /**
     * Sends the base64-encoded image to OpenRouter API for recipe generation.
     *
     * Uses GPT-4o-mini model to analyze the image and generate a recipe in JSON format.
     * The API is instructed to return structured data with Title, Description, Ingredients, and Instructions.
     *
     * @param base64Image Base64 encoded image string
     * @param prompt Additional text prompt (currently unused but available for future enhancements)
     * @param callback Function called with the API response content (null on failure)
     */
    private fun sendImageToOpenRouter(base64Image: String, prompt: String, callback: (String?) -> Unit) {
        val client = OkHttpClient()
        val apiKey = "API-KEY-HERE"
        val url = "https://openrouter.ai/api/v1/chat/completions"

        // Construct chat messages for the API
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

        // Create request body
        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messages)
        }

        val body = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Build HTTP request
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        // Execute request asynchronously
        client.newCall(request).enqueue(object : Callback {
            /**
             * Handles network request failures.
             * Logs error and calls callback with null to indicate failure.
             */
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ImageRecipeActivity", "OpenRouter request failed: ${e.message}")
                callback(null)
            }

            /**
             * Handles successful HTTP response.
             * Parses JSON response to extract the generated recipe content.
             * Calls callback with the content string or null if parsing fails.
             */
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