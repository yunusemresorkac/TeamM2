package com.app.gamenews.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.adapter.UserAdapter
import com.app.gamenews.databinding.ActivityFollowersBinding
import com.app.gamenews.model.User
import com.app.gamenews.viewmodel.FollowersViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowersActivity : AppCompatActivity(),UserAdapter.UserClick {

    private lateinit var binding : ActivityFollowersBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList : ArrayList<User>
    private var userId : String? = null
    private val followersViewModel by viewModel<FollowersViewModel>()
    private var isLoading = false
    private var followers: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        userId = intent.getStringExtra("followersId")

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
                        followersViewModel.loadMoreData(followers!!)

                    }
                }
            }
        })


    }

    private fun getIds(){
        followersViewModel.getFollowers(
            userId!!,
            onComplete = { ids ->
                if (ids.isNotEmpty()) {
                    followers = ids
                    // Do something with the followerIds list
                    followersViewModel.fetchInitialData(ids)
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
            followersViewModel.list.collect { users ->
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