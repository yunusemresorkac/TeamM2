package com.app.gamenews.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.databinding.UserItemBinding
import com.app.gamenews.model.User
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import www.sanju.motiontoast.MotionToastStyle


class UserAdapter(private val userList: ArrayList<User>, private val context: Context, private val userClick: UserClick

) : RecyclerView.Adapter<UserAdapter.MyHolder>() {


    class MyHolder(val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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