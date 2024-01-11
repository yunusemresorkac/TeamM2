package com.app.gamenews.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.gamenews.model.Chat
import com.app.gamenews.repo.ChatRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ChatViewModel  (application: Application) : AndroidViewModel(application) {

    private val repo : ChatRepo = ChatRepo()
    private val _list = MutableStateFlow<List<Chat>>(emptyList())
    val list: StateFlow<List<Chat>> get() = _list

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

    fun fetchInitialData(myId : String, userId : String) {
        repo.fetchInitialData(myId, userId)
    }

    fun loadMoreData(myId : String, userId : String) {
        repo.loadMoreData(myId, userId)
    }

    fun sendMessage(chat : Chat, context: Context){
        repo.sendMessage(chat, context)
    }



//    fun getAllMessages(myId : String, userId : String) : Flow<List<Chat>> {
//        return repo.getAllMessages(myId, userId)
//    }

    fun getAllLastMessages(myId : String) : Flow<List<Chat>> {
        return repo.getAllLastMessages(myId)
    }


}