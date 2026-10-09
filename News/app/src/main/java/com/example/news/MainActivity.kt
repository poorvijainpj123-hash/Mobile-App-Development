package com.example.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.news.databinding.ActivityMainBinding
import com.example.news.model.Article
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NewsAdapter

    private val categoryValues = listOf(
        "general", "business", "entertainment", "health", "science", "sports", "technology"
    )
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restore the last chosen category
        val prefs = getSharedPreferences("news_prefs", MODE_PRIVATE)
        currentIndex = prefs.getInt("category_index", 0)
        supportActionBar?.subtitle = resources.getStringArray(R.array.category_names)[currentIndex]

        adapter = NewsAdapter(emptyList()) { article -> openDetail(article) }
        binding.rvNews.layoutManager = LinearLayoutManager(this)
        binding.rvNews.adapter = adapter

        loadNews()
    }

    private fun loadNews() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = Client.api.getTopHeadlines(
                    country = "in",
                    category = categoryValues[currentIndex],
                    apiKey = BuildConfig.NEWS_API_KEY
                )
                Log.d("NEWS", "headlines: status=${response.status}, total=${response.totalResults}")
                var articles = response.articles.orEmpty().filter { !it.title.isNullOrBlank() }

                if (articles.isEmpty()) {
                    val query = if (currentIndex == 0) "India" else "India ${categoryValues[currentIndex]}"
                    val fallback = Client.api.searchNews(
                        query = query,
                        language = "en",
                        sortBy = "publishedAt",
                        pageSize = 50,
                        apiKey = BuildConfig.NEWS_API_KEY
                    )
                    Log.d("NEWS", "fallback: status=${fallback.status}, total=${fallback.totalResults}")
                    articles = fallback.articles.orEmpty().filter { !it.title.isNullOrBlank() }
                }

                adapter.updateData(articles)
                if (articles.isEmpty()) showError(getString(R.string.error_empty))
            } catch (e: Exception) {
                Log.e("NEWS", "request failed", e)
                adapter.updateData(emptyList())
                showError(getString(R.string.error_load))
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    // Explicit Intent: MainActivity -> NewsDetailActivity
    private fun openDetail(article: Article) {
        val intent = Intent(this@MainActivity, NewsDetailActivity::class.java)
        intent.putExtra("title", article.title)
        intent.putExtra("description", article.description)
        intent.putExtra("urlToImage", article.urlToImage)
        intent.putExtra("url", article.url)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                Toast.makeText(this, R.string.refreshing, Toast.LENGTH_SHORT).show()
                loadNews()
                true
            }
            R.id.action_category -> {
                showCategoryDialog()
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showCategoryDialog() {
        val names = resources.getStringArray(R.array.category_names)
        AlertDialog.Builder(this)
            .setTitle(R.string.select_category)
            .setItems(names) { _, which ->
                currentIndex = which
                getSharedPreferences("news_prefs", MODE_PRIVATE)
                    .edit().putInt("category_index", which).apply()
                supportActionBar?.subtitle = names[which]
                loadNews()
            }
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setMessage(R.string.about_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}