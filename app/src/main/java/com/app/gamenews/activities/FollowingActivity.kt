package com.app.gamenews.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.adapter.UserAdapter
import com.app.gamenews.databinding.ActivityFollowingBinding
import com.app.gamenews.model.User
import com.app.gamenews.viewmodel.FollowingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowingActivity : AppCompatActivity(),UserAdapter.UserClick {

    private lateinit var binding : ActivityFollowingBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList : ArrayList<User>
     private var userId : String? = null
    private val followingViewModel by viewModel<FollowingViewModel>()
    private var isLoading = false
    private var followers: List<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        userId = intent.getStringExtra("followingId")

        initRecycler()


        getIds()

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isLoading && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isLoading = true
                    if (followers!!.isNotEmpty()) {
                        followingViewModel.loadMoreData(followers!!)

                    }
                }
            }
        })


    }

    private fun getIds(){
        followingViewModel.getFollowing(
            userId!!,
            onComplete = { ids ->
                if (ids.isNotEmpty()) {
                    followers = ids
                    // Do something with the followerIds list
                    followingViewModel.fetchInitialData(ids)
                    observeViewModel()
                }

            },
            onError = { exception ->
                // Handle the exception
                println("Error fetching followers: $exception")
            }
        )
    }


    private fun initRecycler() {
        userList = ArrayList()
        binding.recyclerView.layoutManager = GridLayoutManager(this,2)
        binding.recyclerView.setHasFixedSize(true)
        userAdapter = UserAdapter(userList, this,this)
        binding.recyclerView.adapter = userAdapter
    }


    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            followingViewModel.list.collect { users ->
                userList.clear()
                userList.addAll(users)
                userList.sort()
                userAdapter.notifyDataSetChanged()
                isLoading = false
            }
        }
    }

    private fun goToProfile(user: User){
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userId", user.userId)
        startActivity(intent)
    }

    override fun clickUser(user: User) {
        goToProfile(user)
    }



}