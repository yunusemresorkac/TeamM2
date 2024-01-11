package com.app.gamenews.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.adapter.PostAdapter
import com.app.gamenews.adapter.PreviewAdapter
import com.app.gamenews.controller.AdsController
import com.app.gamenews.controller.FollowManager
import com.app.gamenews.databinding.ActivityProfileBinding
import com.app.gamenews.model.Chat
import com.app.gamenews.model.Post
import com.app.gamenews.viewmodel.MyPostViewModel
import com.app.gamenews.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private val userViewModel by viewModel<UserViewModel>()
    private var firebaseUser : FirebaseUser? = null
    private lateinit var userId : String
    private val myPostViewModel by viewModel<MyPostViewModel>()
    private lateinit var previewAdapter: PreviewAdapter
    private lateinit var postList : ArrayList<Post>

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        userId = intent.getStringExtra("userId").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        whichUser()
        getUserData()

        loadAds()
        initRecycler()

        myPostViewModel.fetchInitialData(userId)
        observeViewModel()

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isLoading && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isLoading = true
                    myPostViewModel.loadMoreData(userId)

                }
            }
        })





        binding.followers.setOnClickListener {
            startActivity(Intent(this,FollowersActivity::class.java)
                .putExtra("followersId",userId))
        }

        binding.following.setOnClickListener {
            startActivity(Intent(this,FollowingActivity::class.java)
                .putExtra("followingId",userId))
        }


    }

    private fun initRecycler(){
        postList = ArrayList()
        binding.recyclerView.layoutManager = GridLayoutManager(this,3)
        binding.recyclerView.setHasFixedSize(true)
        previewAdapter = PreviewAdapter(postList, this)
        binding.recyclerView.adapter = previewAdapter


    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            myPostViewModel.list.collect { posts ->
                println("postlar $posts")
                postList.clear()
                postList.addAll(posts)
                previewAdapter.notifyDataSetChanged()
                isLoading = false
            }
        }
    }


    private fun loadAds(){
        val adsController = AdsController(this)
        adsController.loadBanner(binding.adView)
    }


    private fun whichUser(){
        if (firebaseUser!=null){
            if (firebaseUser!!.uid.equals(userId)){
                binding.dmBtn.setImageResource(R.drawable.add_circle_svgrepo_com__1_)
                binding.followBtn.text = "Profili Düzenle"
                binding.dmBtn.setOnClickListener {
                    startActivity(Intent(this,AddPostActivity::class.java))
                }
                binding.followBtn.setOnClickListener {
                    startActivity(Intent(this,EditProfileActivity::class.java))
                }
            }else{
                println("else çalıştı")
                binding.dmBtn.setImageResource(R.drawable.message_square_lines_alt_svgrepo_com)
                binding.followBtn.text ="Takip Et"
                binding.dmBtn.setOnClickListener {
                    startActivity(Intent(this,ChatActivity::class.java)
                        .putExtra("receiverId",userId))
                }

                binding.followBtn.setOnClickListener {
                    FollowManager().followUser(firebaseUser!!.uid,userId,this)
                }

                FollowManager().updateFollowButton(firebaseUser!!.uid, userId,binding.followBtn)

            }

        }


    }

    private fun getUserData(){
        userViewModel.getUserRealTime(this,userId){
            it?.let { user ->
                binding.username.text = user.username
                if (user.profileImage.isNotEmpty()){
                    Glide.with(applicationContext).load(user.profileImage).into(binding.profileImage)
                }
                binding.posts.text = "${user.posts}\n Gönderi"
                binding.followers.text = "${user.followers}\n Takipçiler"
                binding.following.text = "${user.following}\n Takip Ediliyor"

            }
        }


    }

}