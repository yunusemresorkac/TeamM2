package com.app.gamenews.repo

import com.app.gamenews.model.Chat
import com.app.gamenews.model.RoomChat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomChatRepo {

    private val db = FirebaseFirestore.getInstance()
    private val pageSize = 8
    private var lastVisible: DocumentSnapshot? = null
    private var isFetching = false

    private val collection = db.collection("Rooms")

    private val list = ArrayList<RoomChat>()

    private val _listState = MutableStateFlow<List<RoomChat>>(emptyList())
    val listState: StateFlow<List<RoomChat>> get() = _listState


    fun fetchInitialData(roomId : String) {
        if (!isFetching) {
            isFetching = true
            val query = collection.document(roomId)
                .collection("Chats")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle exception
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    list.clear()
                    for (document in snapshot.documents) {
                        val chat = document.toObject(RoomChat::class.java)
                        chat?.let { list.add(it) }
                    }
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                    _listState.value = list.toList()
                }
                isFetching = false
            }
        }
    }

    fun loadMoreData(roomId: String) {
        if (!isFetching && lastVisible != null) {
            isFetching = true
            val query = collection.document(roomId).collection("Chats")
                .orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible!!)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val chat = document.toObject(RoomChat::class.java)
                        chat?.let { list.add(it) }

                    }
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                    _listState.value = list.toList()
                }
                isFetching = false
            }.addOnFailureListener {
                // Handle failure
                isFetching = false
            }
        }
    }

    fun sendMessage(roomChat: RoomChat, onSent : () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Rooms").document(roomChat.roomId).collection("Chats")
                .document(roomChat.chatId)
                .set(roomChat).addOnSuccessListener {
                    onSent()
                }
        }
    }


    fun reportMessage(roomChat: RoomChat, onSuccess : () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("RoomReports").document(roomChat.roomId).collection("Reports")
                .document(System.currentTimeMillis().toString())
                .set(roomChat).addOnSuccessListener {
                    onSuccess()
                }
        }
    }



}