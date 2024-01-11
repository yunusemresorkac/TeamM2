package com.app.gamenews.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.activities.MyPostsActivity
import com.app.gamenews.databinding.PreviewItemBinding
import com.app.gamenews.model.Post
import com.bumptech.glide.Glide

class PreviewAdapter(private val previewList: ArrayList<Post>, private val context: Context

) : RecyclerView.Adapter<PreviewAdapter.MyHolder>() {


    class MyHolder(val binding: PreviewItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = PreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val post = previewList[position]


        Glide.with(context.applicationContext).load(post.imageUrls[0]).into(holder.binding.previewImg)

        holder.itemView.setOnClickListener {
            val intent = Intent(context,MyPostsActivity::class.java)
            intent.putExtra("postId",post.postId)
            intent.putExtra("postOwnerId",post.userId)
            context.startActivity(intent)
        }

    }




    override fun getItemCount(): Int {
        return previewList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}