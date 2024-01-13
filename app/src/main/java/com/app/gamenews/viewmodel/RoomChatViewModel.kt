package com.app.gamenews.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.gamenews.model.RoomChat
import com.app.gamenews.repo.RoomChatRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomChatViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : RoomChatRepo = RoomChatRepo()
    private val _list = MutableStateFlow<List<RoomChat>>(emptyList())
    val list: StateFlow<List<RoomChat>> get() = _list

    init {
        observeGameListState()
    }


    private fun observeGameListState() {
        viewModelScope.launch {
            repo.listState.collect { messages ->
                _list.value = messages
            }
        }
    }

    fun fetchInitialData(roomId : String) {
        repo.fetchInitialData(roomId)
    }

    fun loadMoreData(roomId : String) {
        repo.loadMoreData(roomId)
    }


    fun sendMessage(roomChat: RoomChat, onSent : () -> Unit){
        repo.sendMessage(roomChat, onSent)
    }

    fun reportMessage(roomChat: RoomChat, onSuccess : () -> Unit){
        repo.reportMessage(roomChat, onSuccess)
    }


}