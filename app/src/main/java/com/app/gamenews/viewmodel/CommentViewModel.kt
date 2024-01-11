package com.app.gamenews.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.gamenews.model.Comment
import com.app.gamenews.model.Game
import com.app.gamenews.repo.CommentRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : CommentRepo = CommentRepo()

    private val _list = MutableStateFlow<List<Comment>>(emptyList())
    val list: StateFlow<List<Comment>> get() = _list

    init {
        observeGameListState()
    }

    private fun observeGameListState() {
        viewModelScope.launch {
            repo.listState.collect { comments ->
                _list.value = comments
            }
        }
    }

    fun fetchInitialData(postId : String) {
        repo.fetchInitialData(postId)
    }

    fun loadMoreData(postId: String) {
        repo.loadMoreData(postId)
    }


    fun addComment(comment : Comment, onSuccess : () -> Unit, onFailure : () -> Unit){
        repo.addComment(comment, onSuccess, onFailure)
    }




}