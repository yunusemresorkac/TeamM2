package com.app.gamenews.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.GameNewsItemBinding
import com.app.gamenews.model.GameNews

class GameNewsAdapter(private var newsList: ArrayList<GameNews>, private val context: Context


) : RecyclerView.Adapter<GameNewsAdapter.MyHolder>() {


    class MyHolder(val binding: GameNewsItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = GameNewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val news = newsList[position]

        holder.binding.title.text = news.title
        holder.binding.desc.text = news.description




    }

    fun setGameNewsList(newsList2: ArrayList<GameNews>) {
        newsList = newsList2
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}