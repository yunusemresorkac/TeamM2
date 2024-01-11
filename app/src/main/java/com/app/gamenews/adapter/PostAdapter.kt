package com.app.gamenews.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.activities.ProfileActivity
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.controller.LikeManager
import com.app.gamenews.databinding.PostItemBinding
import com.app.gamenews.model.Post
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import www.sanju.motiontoast.MotionToastStyle


class PostAdapter(
    private val postList: ArrayList<Post>, private val context: Context
    ,private val userViewModel: UserViewModel,private val postClick: PostClick
) : RecyclerView.Adapter<PostAdapter.MyHolder>() {

    private var firebaseUser : FirebaseUser? = null


    class MyHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val post = postList[position]
        firebaseUser = FirebaseAuth.getInstance().currentUser
        readMore(post, holder)
        holder.binding.likeSize.text = post.countOfLikes.toString()
        holder.binding.commentSize.text = post.countOfComments.toString()
        holder.binding.time.text = DummyMethods.convertTime(post.time)

        initPhotoAdapter(holder, post)

        userViewModel.getUsername(context,post.userId){ username ->
            holder.binding.username.text = username
        }

        userViewModel.getUserImage(context,post.userId){ image ->
            if (image.isNotEmpty())
                Glide.with(context.applicationContext).load(image).into(holder.binding.userImage)
        }

        val likeManager = firebaseUser?.let { user ->
            LikeManager(post.postId, user.uid, object : LikeManager.LikeStatusListener {
                override fun onLikeStatusChanged(liked: Boolean) {
                    if (liked) {
                        holder.binding.likeBtn.setImageResource(R.drawable.baseline_favorite_24)
                    } else {
                        holder.binding.likeBtn.setImageResource(R.drawable.baseline_favorite_border_24)
                    }
                }
            },"Posts")
        }
        likeManager?.startListening()


        holder.binding.userLay.setOnClickListener {
            goToProfile(post)
        }



        holder.binding.shareBtn.setOnClickListener {
            postClick.clickShareBtn(postList[position])
        }
        holder.binding.likeBtn.setOnClickListener {
            postClick.clickLikeBtn(postList[position], likeManager!!)
        }
        holder.binding.commentBtn.setOnClickListener {
            postClick.clickCommentBtn(postList[position])
        }

        holder.binding.actionBtn.setOnClickListener {
            postClick.clickPostActions(postList[position])
        }


    }

    private fun goToProfile(post: Post){
        firebaseUser?.let {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("userId", post.userId)
            context.startActivity(intent)
        } ?: run {
            DummyMethods.showMotionToast(context,"Profilleri görmek için giriş yapmalısın","",
                MotionToastStyle.INFO)

        }
    }

    private fun readMore(post: Post, holder: MyHolder){
        val article = post.title

        if (article.length < 50){
            holder.binding.title.text = post.title
        }else{
            holder.binding.title.text = "${post.title.substring(0,50)} \nDevamını Oku..."
            holder.binding.title.setOnClickListener {
                holder.binding.title.text = post.title
            }
        }

    }


    private fun initPhotoAdapter(holder: MyHolder, post: Post){
        holder.binding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        holder.binding.recyclerView.setHasFixedSize(true)
        val photoAdapter = PhotoAdapter(post.imageUrls, context)
        holder.binding.recyclerView.adapter = photoAdapter
        val snapHelper = PagerSnapHelper()
        if (holder.binding.recyclerView.onFlingListener == null) {
            snapHelper.attachToRecyclerView(holder.binding.recyclerView)
        }

    }


    interface PostClick{

        fun clickCommentBtn(post: Post)

        fun clickLikeBtn(post: Post, likeManager: LikeManager)

        fun clickShareBtn(post: Post)

        fun clickPostActions(post: Post)

    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }


}