package com.jeff.geminidemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeff.geminidemo.adapters.ChatAdapter
import com.jeff.geminidemo.databinding.ActivityMainBinding
import com.jeff.geminidemo.dataclass.ChatMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    val apikey = Constants.apiKey
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.goolebooks.setOnClickListener {
            val intent = Intent(this, GoogleBooks::class.java)
            startActivity(intent)
        }

        setupRecyclerView()
        setupSendButton()

    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val userInput = binding.editTextInput.text.toString()
            if (userInput.isNotEmpty()) {
                addMessage(userInput, isUser = true)
                binding.editTextInput.text.clear()
                showLoader(true) // Show the loader when sending the request
                fetchBotResponse(userInput)
            }
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        chatMessages.add(ChatMessage(message, isUser))
        adapter.notifyItemInserted(chatMessages.size - 1)
        binding.recyclerView.smoothScrollToPosition(chatMessages.size - 1)
    }


    private fun fetchBotResponse(userInput: String) {
        val client = OkHttpClient()

        val jsonBody = """
        {
            "contents": [
                {
                    "parts": [
                        { "text": "$userInput" }
                    ]
                }
            ],
            "safetySettings": [
                {
                    "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                    "threshold": "BLOCK_ONLY_HIGH"
                }
            ],
            "generationConfig": {
                "stopSequences": ["Title"],
                "temperature": 1.0,
                "maxOutputTokens": 800,
                "topP": 0.8,
                "topK": 10
            }
        }
        """.trimIndent()

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBody
        )

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apikey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AiFragment", "Request failed: ${e.message}", e)
                this@MainActivity.runOnUiThread {
                    showLoader(false) // Hide the loader
                    addMessage("Failed to connect. Please try again.", isUser = false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    this@MainActivity.runOnUiThread {
                        showLoader(false) // Hide the loader
                        addMessage("Error: ${response.message}", isUser = false)
                    }
                    return
                }

                val responseBody = response.body?.string()
                val botResponse = parseBotResponse(responseBody)

                this@MainActivity.runOnUiThread {
                    showLoader(false) // Hide the loader
                    addMessage(botResponse, isUser = false)
                }
            }
        })
    }

    private fun parseBotResponse(responseBody: String?): String {
        return try {
            val jsonObject = JSONObject(responseBody ?: "")
            val candidates = jsonObject.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val content = candidates.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                if (content != null && content.length() > 0) {
                    content.optJSONObject(0)?.optString("text", "No response") ?: "No response"
                } else {
                    "No response"
                }
            } else {
                "No response"
            }
        } catch (e: Exception) {
            Log.e("AiFragment", "Error parsing response: ${e.message}", e)
            "Error parsing response"
        }
    }

    private fun showLoader(show: Boolean) {
        binding.loaderLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(chatMessages)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }


}