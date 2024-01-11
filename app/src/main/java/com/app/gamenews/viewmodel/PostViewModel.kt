package com.app.gamenews.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.gamenews.model.Post
import com.app.gamenews.repo.PostRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application)  {

    private val repo : PostRepo = PostRepo()

    private val _list = MutableStateFlow<List<Post>>(emptyList())
    val list: StateFlow<List<Post>> get() = _list

    init {
        observeGameListState()
    }

    private fun observeGameListState() {
        viewModelScope.launch {
            repo.listState.collect { posts ->
                _list.value = posts
            }
        }
    }

    fun getFollowings(userId: String, onComplete: (List<String>) -> Unit, onError: (Exception) -> Unit) {
        repo.getFollowings(userId, onComplete, onError)
    }


    fun fetchInitialData(followerIds: List<String>) {
        repo.fetchInitialData(followerIds)
    }

    fun loadMoreData(followerIds: List<String>) {
        repo.loadMoreData(followerIds)
    }



    fun sharePost(post: Post, onSuccess : () -> Unit, onFailure : () -> Unit ){
        repo.sharePost(post, onSuccess, onFailure)
    }



}