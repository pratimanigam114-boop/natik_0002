package com.example.data.ai

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

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateResponse(
        prompt: String,
        systemInstruction: String = "You are AI Life Assistant, a modern, highly practical personal life assistant. Be concise, actionable, and warm. Format key items clearly."
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(IllegalStateException("API key not configured in secrets."))
        }

        try {
            val rootJson = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    put(JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArr)
                    })
                }
                put("contents", contentsArr)

                val sysInstructionJson = JSONObject().apply {
                    val sysParts = JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    }
                    put("parts", sysParts)
                }
                put("systemInstruction", sysInstructionJson)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.9)
                }
                put("generationConfig", genConfig)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                if (!response.isSuccessful || bodyString == null) {
                    return@withContext Result.failure(
                        Exception("Gemini API error code: ${response.code}")
                    )
                }

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        return@withContext Result.success(text)
                    }
                }
                Result.failure(Exception("No response content from Gemini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
