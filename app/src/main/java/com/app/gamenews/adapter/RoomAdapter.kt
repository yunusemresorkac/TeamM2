package com.app.gamenews.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.RoomItemBinding
import com.app.gamenews.model.Room
import com.bumptech.glide.Glide


class RoomAdapter(private val roomList: ArrayList<Room>, private val context: Context,
    private val roomClick: RoomClick
) : RecyclerView.Adapter<RoomAdapter.MyHolder>() {


    class MyHolder(val binding: RoomItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = RoomItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val room = roomList[position]


        holder.binding.title.text = room.title
        holder.binding.game.text = "Oyun: ${room.game}"
        holder.binding.time.text = DummyMethods.getTimeAgo(room.time)
        holder.binding.users.text = "${room.currentUsers} / ${room.maxUsers}"

        if (room.roomImage.isNotEmpty()){
            Glide.with(context.applicationContext).load(room.roomImage).into(holder.binding.gameImage)
        }else{
            holder.binding.gameImage.setImageResource(R.drawable.logo_transparent)
        }

        holder.itemView.setOnClickListener {
            roomClick.clickRoom(roomList[position])
        }

    }





    override fun getItemCount(): Int {
        return roomList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }


    interface RoomClick{

        fun clickRoom(room: Room)

    }

}