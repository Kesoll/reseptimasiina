package com.example.reseptimasiina

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.core.text.HtmlCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.example.reseptimasiina.RecipeFormatter


/**
 * Utility class for interacting with the DeepSeek API
 */
object DeepSeekService {

    private const val API_URL = "https://openrouter.ai/api/v1/chat/completions"
    private const val API_KEY = "Bearer API-KEY-HERE"
    private const val TAG = "DeepSeekService"

    private val mainHandler = Handler(Looper.getMainLooper())


    /**
     * Send a prompt to the DeepSeek API and receive the raw response
     *
     * @param prompt The text prompt to send to the API
     * @param callback Callback that will be invoked with the API response or error message
     */
    fun askDeepSeek(prompt: String, callback: (String?) -> Unit) {
        Thread {
            try {
                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", API_KEY)
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("HTTP-Referer", "https://yourapp.example.com")
                conn.setRequestProperty("X-Title", "Reseptimasiina")
                conn.doOutput = true

                val requestBody = """
                {
                    "model": "mistralai/mistral-small-3.1-24b-instruct:free",
                    "messages": [
                        {
                            "role": "user",
                            "content": "$prompt"
                        }
                    ]
                }
                """.trimIndent()

                OutputStreamWriter(conn.outputStream).use { it.write(requestBody) }

                val responseCode = conn.responseCode
                val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                }

                Log.d(TAG, "Response: $response")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonResponse = JSONObject(response)
                    val choices = jsonResponse.getJSONArray("choices")
                    val message = choices.getJSONObject(0).getJSONObject("message")
                    val content = message.getString("content").trim()

                    runOnMainThread { callback(content) }
                } else {
                    runOnMainThread { callback("Error: $response") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                runOnMainThread { callback("Error: ${e.message}") }
            }
        }.start()
    }


    /**
     * Send a recipe prompt to the DeepSeek API and parse the response as a recipe
     *
     * @param ingredients List of ingredients to generate a recipe for
     * @param callback Callback that will be invoked with the formatted HTML recipe or error message
     */
    fun getRecipeFromIngredients(ingredients: List<String>, callback: (CharSequence) -> Unit) {
        if (ingredients.isEmpty()) {
            runOnMainThread { callback("Please add at least one ingredient") }
            return
        }

        val prompt = "Suggest recipe from the following ingredients: ${ingredients.joinToString(", return the answer in json format with Title: , Description: Ingredients: and Instructions: :")}"

        askDeepSeek(prompt) { response ->
            runOnMainThread {
                RecipeFormatter.processRecipeResponse(response, callback)
            }

        }
    }


    /**
     * Run a callback on the main thread
     */
    private fun runOnMainThread(action: () -> Unit) {
        mainHandler.post(action)
    }
}