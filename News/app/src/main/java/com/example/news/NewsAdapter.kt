package com.example.news

import android.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.news.databinding.ItemNewsBinding
import com.example.news.model.Article

class NewsAdapter(
    private var newsList: List<Article>,
    private val onItemClick: (Article) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = newsList[position]
        holder.binding.tvTitle.text = article.title
        holder.binding.tvSource.text = article.source?.name ?: ""

        Glide.with(holder.itemView)
            .load(article.urlToImage)
            .placeholder(R.color.darker_gray)
            .centerCrop()
            .into(holder.binding.ivThumb)

        holder.itemView.setOnClickListener { onItemClick(article) }
    }

    override fun getItemCount(): Int = newsList.size

    fun updateData(newList: List<Article>) {
        newsList = newList
        notifyDataSetChanged()
    }
}