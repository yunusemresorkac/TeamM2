package com.app.gamenews.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.gamenews.model.User
import com.app.gamenews.repo.JoinersRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JoinersViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : JoinersRepo = JoinersRepo()

    private val _list = MutableStateFlow<List<User>>(emptyList())
    val list: StateFlow<List<User>> get() = _list

    init {
        observeListState()
    }

    private fun observeListState() {
        viewModelScope.launch {
            repo.listState.collect { users ->
                _list.value = users
            }
        }
    }

    fun fetchInitialData(followerIds: List<String>) {
        repo.fetchInitialData(followerIds)
    }


    fun getJoinerIds(roomId: String, onComplete: (List<String>) -> Unit, onError: (Exception) -> Unit) {
        repo.getJoinerIds(roomId, onComplete, onError)
    }


}