package com.jeff.geminidemo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeff.geminidemo.adapters.BooksAdapter
import com.jeff.geminidemo.databinding.ActivityGoogleBooksBinding
import com.jeff.geminidemo.dataclass.Book
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class GoogleBooks : AppCompatActivity() {
    private lateinit var binding: ActivityGoogleBooksBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGoogleBooksBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSearch.setOnClickListener {
            searchBook()
        }

        binding.ivBack.setOnClickListener{
            onBackPressed()
        }

    }

    private fun searchBook() {
        val query = binding.bookDetails.text.toString().trim()

        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a book name or author to search.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        val client = AsyncHttpClient()
        val url = "https://www.googleapis.com/books/v1/volumes?q=${query}"

        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>,
                responseBody: ByteArray?
            ) {
                binding.progressBar.visibility = View.GONE

                val result = responseBody?.let { String(it) }
                val books = mutableListOf<Book>()

                try {
                    val jsonObject = JSONObject(result ?: "")
                    val itemArray = jsonObject.getJSONArray("items")

                    for (i in 0 until itemArray.length()) {
                        val book = itemArray.getJSONObject(i)
                        val volumeInfo = book.getJSONObject("volumeInfo")
                        val title = volumeInfo.optString("title", "No Title")
                        val author = volumeInfo.optJSONArray("authors")?.join(", ") ?: "Unknown Author"
                        val infoLink = volumeInfo.optString("infoLink", "")

                        books.add(Book(title, author, infoLink))
                    }

                    setupRecyclerView(books)

                } catch (e: Exception) {
                    Toast.makeText(this@GoogleBooks, "Error parsing results.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@GoogleBooks, "Failed to fetch books: ${error?.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView(books: List<Book>) {
        val adapter = BooksAdapter(this, books)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    companion object {
        private val TAG = GoogleBooks::class.java.simpleName
    }
}