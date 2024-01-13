package com.app.gamenews.adapter




import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.activities.ChatActivity
import com.app.gamenews.activities.ProfileActivity
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.ChatItemBinding
import com.app.gamenews.databinding.MessagesItemBinding
import com.app.gamenews.model.Chat
import com.app.gamenews.model.Comment
import com.app.gamenews.viewmodel.ChatViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import www.sanju.motiontoast.MotionToastStyle


class MessagesAdapter(private val chatList: ArrayList<Chat>, private val context: Context
    ,private val userViewModel: UserViewModel,private val chatViewModel: ChatViewModel

) : RecyclerView.Adapter<MessagesAdapter.MyHolder>() {

    private lateinit var firebaseUser: FirebaseUser

    class MyHolder(val binding: MessagesItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = MessagesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val chat = chatList[position]

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        holder.binding.lastMessage.text = chat.message
        holder.binding.time.text = DummyMethods.formatIsoDate(chat.time)

        if (firebaseUser.uid.equals(chat.senderId)){
            userViewModel.getUser(context,chat.receiverId){ user->
               user?.let {
                   holder.binding.username.text = it.username
                   if (it.profileImage.isNotEmpty()){
                       Glide.with(context.applicationContext).load(it.profileImage).into(holder.binding.userImage)
                   }
               }
            }
        }else{
            userViewModel.getUser(context,chat.senderId){ user ->
                user?.let {
                    holder.binding.username.text = it.username
                    if (it.profileImage.isNotEmpty()){
                        Glide.with(context.applicationContext).load(it.profileImage).into(holder.binding.userImage)
                    }
                }

            }
        }


        holder.itemView.setOnClickListener {
            goToChat(chatList[position])
        }

        holder.binding.userImage.setOnClickListener{
            goToProfile(chatList[position])
        }

    }

    private fun goToProfile(chat: Chat){
        if (firebaseUser.uid.equals(chat.senderId)){
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("userId",chat.receiverId)
            context.startActivity(intent)
        }else{
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("userId",chat.senderId)
            context.startActivity(intent)
        }
    }


    private fun goToChat(chat: Chat){
        if (firebaseUser.uid.equals(chat.senderId)){
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("receiverId",chat.receiverId)
            context.startActivity(intent)
        }else{
            chatViewModel.markLastMessageAsRead(chat)
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("receiverId",chat.senderId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}