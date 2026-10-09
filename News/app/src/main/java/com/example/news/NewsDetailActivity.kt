package com.example.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.news.databinding.ActivityNewsDetailBinding

class NewsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Data sent by the explicit Intent
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val imageUrl = intent.getStringExtra("urlToImage")
        val url = intent.getStringExtra("url")

        binding.tvDetailTitle.text = title
        binding.tvDetailDescription.text = description ?: getString(R.string.no_description)
        Glide.with(this).load(imageUrl).centerCrop().into(binding.ivDetail)

        // Implicit Intent: open the article in a browser
        binding.btnReadMore.setOnClickListener {
            if (url.isNullOrBlank()) {
                Toast.makeText(this, R.string.no_link, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        // Implicit Intent: share the article
        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "$title\n$url")
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}