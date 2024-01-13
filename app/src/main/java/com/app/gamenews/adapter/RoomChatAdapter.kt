package com.app.gamenews.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.RoomChatItemBinding
import com.app.gamenews.model.RoomChat
import com.app.gamenews.viewmodel.RoomChatViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import www.sanju.motiontoast.MotionToastStyle


class RoomChatAdapter(private val chatList: ArrayList<RoomChat>, private val context: Context
    ,private val userViewModel: UserViewModel,private val roomChatViewModel: RoomChatViewModel
) : RecyclerView.Adapter<RoomChatAdapter.MyHolder>() {


    class MyHolder(val binding: RoomChatItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = RoomChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val chat = chatList[position]

        holder.binding.message.text = chat.message
        holder.binding.time.text = DummyMethods.formatIsoDate(chat.time)


        holder.itemView.setOnLongClickListener {
            DummyMethods.copyText(context, "message", chat.message)
            DummyMethods.showMotionToast(context,"Kopyalandı","",MotionToastStyle.INFO)
            true
        }


        holder.binding.reportBtn.setOnClickListener {
            showActionsDialog("Bildir","İptal","Bu mesajı bildirmek ister misin","Bir ihlal yaptığını düşünüyorsan bildirebilirsin",
                {
                   roomChatViewModel.reportMessage(chatList[position]) {
                       DummyMethods.showMotionToast(
                           context,
                           "Bildirin İçin Teşekkürler",
                           "",
                           MotionToastStyle.SUCCESS
                       )
                   }
                },{

                })


        }

        getUserData(holder,chat)
    }

    private fun getUserData(holder: MyHolder, roomChat: RoomChat){
        userViewModel.getUsername(context,roomChat.senderId){ username ->
            userViewModel.getUserImage(context,roomChat.senderId) { image ->
                holder.binding.username.text = username
                if (image.isNotEmpty()){
                    Glide.with(context.applicationContext).load(image).into(holder.binding.userImage)
                }else{
                    holder.binding.userImage.setImageResource(R.drawable.profile_user_svgrepo_com)
                }
            }

        }
    }

    private fun showActionsDialog(
        positiveBtnText: String,
        negativeBtnText: String,
        title: String,
        message: String,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {

        val mDialog = MaterialDialog.Builder(context as Activity)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(positiveBtnText) { dialogInterface, which ->
                onPositiveClick()
                dialogInterface?.dismiss()
            }
            .setNegativeButton(negativeBtnText) { dialogInterface, which ->
                onNegativeClick()
                dialogInterface.dismiss()
            }
            .build()

        mDialog.show()


    }


    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}