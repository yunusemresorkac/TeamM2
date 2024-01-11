package com.app.gamenews.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.activities.ChatActivity
import com.app.gamenews.activities.ProfileActivity
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.databinding.CommentItemBinding
import com.app.gamenews.model.Comment
import com.app.gamenews.viewmodel.CommentViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import www.sanju.motiontoast.MotionToastStyle


class CommentAdapter(private val commentList: ArrayList<Comment>, private val context: Context
    ,private val userViewModel: UserViewModel
) : RecyclerView.Adapter<CommentAdapter.MyHolder>() {

    private var firebaseUser : FirebaseUser? = null

    class MyHolder(val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val comment = commentList[position]

        firebaseUser = FirebaseAuth.getInstance().currentUser
        holder.binding.comment.text = comment.comment
        holder.binding.time.text = DummyMethods.convertTime(comment.time)

        userViewModel.getUsername(context,comment.userId){ username ->
            holder.binding.username.text = username
        }

        userViewModel.getUserImage(context,comment.userId){ image ->
            if (image.isNotEmpty())
                Glide.with(context.applicationContext).load(image).into(holder.binding.userImage)
        }

        holder.binding.username.paintFlags = holder.binding.username.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        holder.binding.userImage.setOnClickListener {
            goToProfile(commentList[position])
        }

        holder.binding.username.setOnClickListener {
            goToProfile(commentList[position])
        }

    }

    private fun goToProfile(comment: Comment){
        firebaseUser?.let {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("userId", comment.userId)
            context.startActivity(intent)
        } ?: run {
            DummyMethods.showMotionToast(context,"Profilleri görmek için giriş yapmalısın","",MotionToastStyle.INFO)

        }
    }




    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}