package com.app.gamenews.repo

import android.content.Context
import com.app.gamenews.model.Comment
import com.app.gamenews.model.Game
import com.app.gamenews.model.Room
import com.app.gamenews.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomRepo {

    private val db = FirebaseFirestore.getInstance()

    private val pageSize = 8
    private var lastVisible: DocumentSnapshot? = null
    private var isFetching = false

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("Rooms")

    private val list = ArrayList<Room>()

    private val _listState = MutableStateFlow<List<Room>>(emptyList())
    val listState: StateFlow<List<Room>> get() = _listState


    fun deleteRoom(roomId : String){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Rooms").document(roomId)
                .delete().addOnSuccessListener {
                }.addOnFailureListener {
                }
        }
    }

    fun createRoom(room : Room, onCreated : () -> Unit, onFailure : () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Rooms").document(room.roomId)
                .set(room).addOnSuccessListener {
                    onCreated()
                }.addOnFailureListener {
                    onFailure()
                }
        }
    }


    fun joinToRoom(roomId : String, userId : String, onJoined : () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Rooms").document(roomId)
                .collection("Users").document(userId)
                .set(mapOf("joined" to true))
                .addOnSuccessListener {
                    increaseUsers(roomId)
                    onJoined()
                }
        }
    }

    fun deleteUserFromRoom(roomId : String, userId : String, onDeleted : () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Rooms").document(roomId)
                .collection("Users").document(userId)
                .delete()
                .addOnSuccessListener {
                    decreaseUsers(roomId)
                    onDeleted()
                }
        }
    }

    private fun increaseUsers(roomId: String){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["currentUsers"] = FieldValue.increment(1)

            FirebaseFirestore.getInstance().collection("Rooms").document(roomId)
                .update(map).addOnSuccessListener {

                }
        }
    }

    private fun decreaseUsers(roomId: String){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["currentUsers"] = FieldValue.increment(-1)

            FirebaseFirestore.getInstance().collection("Rooms").document(roomId)
                .update(map).addOnSuccessListener {

                }
        }
    }

    //get

    fun getRoomById(context: Context, roomId: String, callback: (Room?) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("Rooms").document(roomId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                callback(null)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val room = snapshot.toObject(Room::class.java)
                if (room != null) {
                    callback(room)
                }
            } else {
                callback(null)
            }
        }

    }


    fun fetchInitialData() {
        if (!isFetching) {
            isFetching = true
            val query = collection.orderBy("time", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    list.clear()
                    for (document in snapshot.documents) {
                        val room = document.toObject(Room::class.java)
                        room?.let { list.add(it) }
                    }
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                    _listState.value = list.toList()

                    // Listener'ı kaldırarak güncellemeleri durdur
                }
                isFetching = false
            }.addOnFailureListener {
                // Handle failure
                isFetching = false
            }
        }
    }

    fun loadMoreData() {
        if (!isFetching && lastVisible != null) {
            isFetching = true
            val query = collection.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible!!)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val room = document.toObject(Room::class.java)
                        room?.let { list.add(it) }

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









}