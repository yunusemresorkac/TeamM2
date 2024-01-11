package com.app.gamenews.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.gamenews.model.Post
import com.app.gamenews.repo.MyPostRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyPostViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : MyPostRepo = MyPostRepo()

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

    fun fetchInitialData(userId : String) {
        repo.fetchInitialData(userId)
    }

    fun loadMoreData(userId : String) {
        repo.loadMoreData(userId)
    }

    fun deletePost(post: Post, onSuccess : () -> Unit){
        repo.deletePost(post,onSuccess)
    }

    fun updatePost(post: Post, onSuccess : () -> Unit, onFailure : () -> Unit ){
        repo.updatePost(post, onSuccess, onFailure)
    }


    fun getPostById(context : Context, postId: String, callback: (Post?) -> Unit) {
        repo.getPostById(context, postId, callback)
    }








}