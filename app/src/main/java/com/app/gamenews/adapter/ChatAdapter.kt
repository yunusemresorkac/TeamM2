package com.app.gamenews.adapter



import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.ChatItemBinding
import com.app.gamenews.databinding.ChatItemRightBinding
import com.app.gamenews.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import www.sanju.motiontoast.MotionToastStyle


class ChatAdapter(private val chatList: ArrayList<Chat>, private val context: Context

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var firebaseUser: FirebaseUser
    private val MSG_TYPE_LEFT = 0
    private val MSG_TYPE_RIGHT = 1

    inner class LeftViewHolder( val binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Define your left view holder
    }

    inner class RightViewHolder(val binding: ChatItemRightBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Define your right view holder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MSG_TYPE_LEFT -> {
                val binding = ChatItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LeftViewHolder(binding)
            }
            MSG_TYPE_RIGHT -> {
                val binding = ChatItemRightBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RightViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val chat = chatList[position]


        holder.itemView.setOnLongClickListener {
            DummyMethods.copyText(context, "message", chat.message)
            DummyMethods.showMotionToast(context,"Kopyalandı","",MotionToastStyle.INFO)
            true
        }

        when (holder) {
            is LeftViewHolder -> {
                holder.binding.message.text = chat.message
                holder.binding.time.text = DummyMethods.formatIsoDate(chat.time)
                // Bind left view holder data
            }
            is RightViewHolder -> {
                holder.binding.message.text = chat.message
                holder.binding.time.text = DummyMethods.formatIsoDate(chat.time)
                updateReadStatus(holder, chat.read)

                // Bind right view holder data
            }
        }

    }
    private fun updateReadStatus(holder: RightViewHolder, isRead: Boolean) {
        if (isRead) {
            holder.binding.readText.text = "✓✓"
        } else {
            holder.binding.readText.text = "✓"
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        return if (chatList[position].senderId.equals(firebaseUser.uid)) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }



}