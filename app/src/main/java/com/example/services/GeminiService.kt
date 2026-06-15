package com.example.services

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    // Ordered list of model candidacies for robust fallback resolve support
    private val MODELS = listOf(
        "gemini-3.5-flash",
        "gemini-3.1-pro-preview"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        prompt: String,
        base64Media: String? = null,
        mimeType: String? = null,
        systemInstruction: String = "You are an expert personalized AI Doubts Solving Tutor for students using 'Aspirant Management'. Provide concise, friendly dynamic feedback, breaking down STEM, literature, worksheets, and literature (e.g. primary CVC worksheets, phonics, Hindi literature like Bhakti Kal and Chayavad). Keep explanations scannable, engaging, and precise.",
        customApiKey: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey else BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is empty or placeholder!")
            return@withContext "Error: Gemini API Key is missing. Please configure it in the AI Studio Secrets panel or dynamic Cloud Settings."
        }

        // Try candidate models in descending order of preference
        var lastErrorMsg = ""
        for (modelName in MODELS) {
            try {
                // Build request JSON for current model candidate
                val requestJson = JSONObject()
                
                // Contents
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()

                // 1. Add Text Prompt
                val textPart = JSONObject().put("text", prompt)
                partsArray.put(textPart)

                // 2. Add Multimodal Media Part if present
                if (base64Media != null && mimeType != null) {
                    val inlineDataObj = JSONObject()
                        .put("mimeType", mimeType)
                        .put("data", base64Media)
                    val mediaPart = JSONObject().put("inlineData", inlineDataObj)
                    partsArray.put(mediaPart)
                }

                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                requestJson.put("contents", contentsArray)

                // System Instruction
                if (systemInstruction.isNotEmpty()) {
                    val sysPart = JSONObject().put("text", systemInstruction)
                    val sysContent = JSONObject().put("parts", JSONArray().put(sysPart))
                    requestJson.put("systemInstruction", sysContent)
                }

                // Generation config
                val configObj = JSONObject().put("temperature", 0.7)
                requestJson.put("generationConfig", configObj)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = requestJson.toString().toRequestBody(mediaType)

                val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful) {
                        val errorDetail = JSONObject(responseBody ?: "").optJSONObject("error")?.optString("message") ?: "HTTP error ${response.code}"
                        Log.w(TAG, "Model $modelName request failed with code: ${response.code} description: $errorDetail")
                        lastErrorMsg = "[$modelName error: $errorDetail]"
                        // Continue to try next candidate in list
                        return@use
                    }

                    if (responseBody != null) {
                        val root = JSONObject(responseBody)
                        val candidates = root.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                return@withContext parts.getJSONObject(0).optString("text", "No response text found.")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception running model candidate $modelName", e)
                lastErrorMsg = e.localizedMessage ?: "Unknown exception"
            }
        }

        // If we reach here, all model candidates failed
        Log.e(TAG, "All Gemini models failed. Last error: $lastErrorMsg")
        return@withContext "Error: Gemini API call failed. Please check your API key validity and network connectivity. details: $lastErrorMsg"
    }
}
