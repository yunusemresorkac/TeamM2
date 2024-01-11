package com.app.gamenews.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.gamenews.model.Game
import com.app.gamenews.repo.EpicRepo
import com.app.gamenews.repo.SteamRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EpicViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: EpicRepo = EpicRepo()

    private val _gameList = MutableStateFlow<List<Game>>(emptyList())
    val gameList: StateFlow<List<Game>> get() = _gameList

    init {
        observeGameListState()
    }

    private fun observeGameListState() {
        viewModelScope.launch {
            repo.gameListState.collect { games ->
                _gameList.value = games
            }
        }
    }

    fun fetchInitialData() {
        repo.fetchInitialData()
    }

    fun loadMoreData() {
        repo.loadMoreData()
    }
}
