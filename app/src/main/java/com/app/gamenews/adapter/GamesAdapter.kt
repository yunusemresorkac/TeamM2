package com.app.gamenews.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.controller.DummyMethods
import com.app.gamenews.controller.LikeManager
import com.app.gamenews.databinding.GameItemBinding
import com.app.gamenews.model.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class GamesAdapter(
    private val gameList: ArrayList<Game>, private val context: Context
    , private val collectionName: String, private val gamesClick: GamesClick
) : RecyclerView.Adapter<GamesAdapter.MyHolder>() {

    private var firebaseUser: FirebaseUser? = null

    class MyHolder(val binding: GameItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = GameItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val news = gameList[position]
        firebaseUser = FirebaseAuth.getInstance().currentUser

        holder.binding.title.text = news.title
        holder.binding.time.text = DummyMethods.convertTime(news.time.toLong())
        holder.binding.commentSize.text = news.countOfComments.toString()
        holder.binding.likeSize.text = news.countOfLikes.toString()
        initPhotoAdapter(holder, news)


        if (firebaseUser!=null){
            val likeManager = firebaseUser?.let { user ->
                LikeManager(news.time, user.uid, object : LikeManager.LikeStatusListener {
                    override fun onLikeStatusChanged(liked: Boolean) {
                        if (liked) {
                            holder.binding.likeBtn.setImageResource(R.drawable.baseline_favorite_24)
                        } else {
                            holder.binding.likeBtn.setImageResource(R.drawable.baseline_favorite_border_24)
                        }
                    }
                },collectionName)
            }
            likeManager?.startListening()
            holder.binding.likeBtn.setOnClickListener {
                gamesClick.clickLikeBtn(gameList[position], likeManager!!)
            }
        }


        holder.binding.detailsBtn.setOnClickListener {
            gamesClick.clickDetailsBtn(gameList[position])
        }
        holder.binding.shareBtn.setOnClickListener {
            gamesClick.clickShareBtn(gameList[position])
        }
        holder.binding.commentBtn.setOnClickListener {
            gamesClick.clickCommentBtn(gameList[position])
        }


        readMore(news, holder)

    }

    private fun readMore(news: Game , holder: MyHolder){
        val article = news.title

        if (article.length < 50){
            holder.binding.title.text = news.title
        }else{
            holder.binding.title.text = "${news.title.substring(0,50)} \nDevamını Oku..."
            holder.binding.title.setOnClickListener {
                holder.binding.title.text = news.title
            }
        }

    }


    private fun initPhotoAdapter(holder: MyHolder, game: Game){
        holder.binding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        holder.binding.recyclerView.setHasFixedSize(true)
        val photoAdapter = PhotoAdapter(game.imageUrls, context)
        holder.binding.recyclerView.adapter = photoAdapter
        val snapHelper = PagerSnapHelper()
        if (holder.binding.recyclerView.onFlingListener == null) {
            snapHelper.attachToRecyclerView(holder.binding.recyclerView)
        }

    }


    interface GamesClick{

        fun clickCommentBtn(game: Game)

        fun clickLikeBtn(game: Game, likeManager: LikeManager)

        fun clickShareBtn(game: Game)

        fun clickDetailsBtn(game: Game)

    }


    override fun getItemCount(): Int {
        return gameList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}