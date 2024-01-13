package com.app.gamenews.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.databinding.RoomUserItemBinding
import com.app.gamenews.databinding.UserItemBinding
import com.app.gamenews.model.User
import com.bumptech.glide.Glide


class RoomUserAdapter(private val userList: ArrayList<User>, private val context: Context, private val userClick: UserClick

) : RecyclerView.Adapter<RoomUserAdapter.MyHolder>() {


    class MyHolder(val binding: RoomUserItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = RoomUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val user = userList[position]

        holder.binding.username.text = user.username
        if (user.profileImage.isNotEmpty()){
            Glide.with(context.applicationContext).load(user.profileImage).into(holder.binding.userImage)
        }

        holder.itemView.setOnClickListener {
            userClick.clickUser(userList[position])
        }

    }



    interface UserClick{

        fun clickUser(user: User)

    }


    override fun getItemCount(): Int {
        return userList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }


}

