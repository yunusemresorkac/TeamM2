package com.app.gamenews.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.gamenews.model.User
import com.app.gamenews.repo.FollowingRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FollowingViewModel (application: Application) : AndroidViewModel(application)  {

    private val repo : FollowingRepo = FollowingRepo()

    private val _list = MutableStateFlow<List<User>>(emptyList())
    val list: StateFlow<List<User>> get() = _list

    init {
        observeGameListState()
    }

    private fun observeGameListState() {
        viewModelScope.launch {
            repo.listState.collect { users ->
                _list.value = users
            }
        }
    }

    fun getFollowing(userId: String, onComplete: (List<String>) -> Unit, onError: (Exception) -> Unit) {
        repo.getFollowing(userId, onComplete, onError)
    }



    fun fetchInitialData(followerIds: List<String>) {
        repo.fetchInitialData(followerIds)
    }

    fun loadMoreData(followerIds: List<String>) {
        repo.loadMoreData(followerIds)
    }


}