package com.app.gamenews.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.gamenews.model.Comment
import com.app.gamenews.model.Room
import com.app.gamenews.repo.RoomRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : RoomRepo = RoomRepo()
    private val _list = MutableStateFlow<List<Room>>(emptyList())
    val list: StateFlow<List<Room>> get() = _list

    init {
        observeGameListState()
    }

    private fun observeGameListState() {
        viewModelScope.launch {
            repo.listState.collect { rooms ->
                _list.value = rooms
            }
        }
    }

    fun fetchInitialData() {
        repo.fetchInitialData()
    }

    fun loadMoreData() {
        repo.loadMoreData()
    }

    fun getRoomById(context: Context, roomId: String, callback: (Room?) -> Unit) {
        repo.getRoomById(context, roomId, callback)
    }

    fun joinToRoom(roomId : String, userId : String, onJoined : () -> Unit){
        repo.joinToRoom(roomId, userId, onJoined)
    }

    fun deleteUserFromRoom(roomId : String, userId : String, onDeleted : () -> Unit){
        repo.deleteUserFromRoom(roomId, userId, onDeleted)
    }

    fun deleteRoom(roomId : String){
        repo.deleteRoom(roomId)
    }


    fun createRoom(room : Room, onCreated : () -> Unit, onFailure : () -> Unit){
        repo.createRoom(room, onCreated, onFailure)
    }


}