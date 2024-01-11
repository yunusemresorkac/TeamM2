package com.app.gamenews.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.databinding.PhotoItemBinding
import com.bumptech.glide.Glide

class PhotoAdapter(private val imageList: MutableList<String>, private val context: Context
) : RecyclerView.Adapter<PhotoAdapter.MyHolder>() {


    class MyHolder(val binding: PhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = PhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val image = imageList[position]

        holder.binding.listSize.text = "${position + 1}/${imageList.size}"

        Glide.with(context.applicationContext)
            .load(image)
            .placeholder(R.mipmap.ic_launcher)
            .into(holder.binding.newsImage)


    }



    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}