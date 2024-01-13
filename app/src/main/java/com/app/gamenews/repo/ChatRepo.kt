package com.app.gamenews.repo

import android.content.Context
import com.app.gamenews.model.Chat
import com.app.gamenews.model.Comment
import com.app.gamenews.model.User
import com.app.gamenews.util.Constants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepo {

    private val db = FirebaseFirestore.getInstance()

    private val pageSize = 8
    private var lastVisible: DocumentSnapshot? = null
    private var isFetching = false

    private val collection = db.collection("Chats")

    private val list = ArrayList<Chat>()

    private val _listState = MutableStateFlow<List<Chat>>(emptyList())
    val listState: StateFlow<List<Chat>> get() = _listState




    fun fetchInitialData(myId : String, userId : String) {
        if (!isFetching) {
            isFetching = true
            val query = collection.document(myId)
                .collection("Users").document(userId)
                .collection("Messages")
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
                        val chat = document.toObject(Chat::class.java)
                        chat?.let { list.add(it) }
                    }
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                    _listState.value = list.toList()
                }
                isFetching = false
            }
        }
    }

    fun loadMoreData(myId : String, userId : String) {
        if (!isFetching && lastVisible != null) {
            isFetching = true
            val query = collection.document(myId).collection("Users")
                .document(userId).collection("Messages")
                .orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible!!)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val chat = document.toObject(Chat::class.java)
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



    fun sendMessage(chat : Chat, context: Context){
        CoroutineScope(Dispatchers.IO).launch {


            db.collection("Chats").document(chat.receiverId)
                .collection("Users").document(chat.senderId).collection("Messages")
                .document(chat.chatId).set(chat)
                .addOnSuccessListener {
                    db.collection("Chats").document(chat.senderId)
                        .collection("Users").document(chat.receiverId)
                        .collection("Messages").document(chat.chatId)
                        .set(chat)
                        .addOnSuccessListener {
                            saveLastMessage(chat)
                        }
                }.addOnFailureListener {
                }


        }

    }



    private fun saveLastMessage(lastMessage: Chat){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Chats").document(lastMessage.senderId).collection("Users")
                .document(lastMessage.receiverId).set(lastMessage)
                .addOnSuccessListener {
                    db.collection("Chats").document(lastMessage.receiverId).collection("Users")
                        .document(lastMessage.senderId)
                        .set(lastMessage)
                }
        }

    }

    fun markMessageAsRead(chat: Chat) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Chats").document(chat.senderId)
                .collection("Users").document(chat.receiverId)
                .collection("Messages").document(chat.chatId)
                .update("read", true)
                .addOnSuccessListener {
                    db.collection("Chats").document(chat.receiverId)
                        .collection("Users").document(chat.senderId)
                        .collection("Messages").document(chat.chatId)
                        .update("read", true).addOnSuccessListener {
                        }
                }
        }

    }

     fun markLastMessageAsRead(chat: Chat){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Chats").document(chat.senderId)
                .collection("Users").document(chat.receiverId)
                .update("read",true).addOnSuccessListener {
                    db.collection("Chats").document(chat.receiverId)
                        .collection("Users").document(chat.senderId)
                        .update("read",true).addOnSuccessListener {

                        }
                }
        }
    }


//    private fun getMessages(myId : String, userId : String): Flow<List<Chat>> = callbackFlow {
//        val chatReference = FirebaseFirestore.getInstance().collection("Chats")
//            .document(myId).collection("Users").document(userId)
//            .collection("Messages")
//            .orderBy("time", Query.Direction.ASCENDING)
//
//        val listener = chatReference.addSnapshotListener { querySnapshot, exception ->
//            if (exception != null) {
//                close(exception)
//                return@addSnapshotListener
//            }
//
//            val chatList = ArrayList<Chat>()
//
//            querySnapshot?.forEach { document ->
//                val chat = document.toObject(Chat::class.java)
//                chat.let {
//                    chatList.add(chat)
//                }
//
//            }
//
//            trySend(chatList)
//        }
//
//        awaitClose { listener.remove() }
//    }
//
//    fun getAllMessages(myId : String, userId : String): Flow<List<Chat>> = getMessages(myId, userId).flowOn(Dispatchers.IO).onStart {
//        emit(emptyList()) // Optional: Emit an empty list while fetching the data
//    }.catch { exception ->
//        // Handle any errors here
//        // For instance, emit an empty list or log the error
//        emit(emptyList())
//    }
//
//



    fun getUnreadMessages(userId: String,callback: (Int?) -> Unit){
        val chatsCollection = db.collection("Chats").document(userId)
            .collection("Users").whereEqualTo("read",false)

        chatsCollection.get()
            .addOnSuccessListener { documents ->
                // Okuma başarılı oldu
                for (document in documents) {
                    val chat = document.toObject(Chat::class.java)
                    if (userId.equals(chat.receiverId)){
                        val unreadChatsCount = documents.size()
                        callback(unreadChatsCount)
                        println("UserId $userId için okunmamış mesaj sayısı: $unreadChatsCount")
                    }

                }


            }
            .addOnFailureListener { exception ->
                // Hata durumunda
                callback(0)

                println("Okunmamış mesaj sayısı alınamadı: $exception")
            }
    }



    private fun getLastMessage(myId: String): Flow<List<Chat>> = callbackFlow {
        val chatReference = FirebaseFirestore.getInstance().collection("Chats")
            .document(myId).collection("Users")
            .orderBy("time", Query.Direction.DESCENDING)

        val listener = chatReference.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            val chatList = ArrayList<Chat>()

            querySnapshot?.forEach { document ->
                val chat = document.toObject(Chat::class.java)
                chat.let {
                    chatList.add(chat)
                }

            }

            trySend(chatList)
        }

        awaitClose { listener.remove() }
    }

    fun getAllLastMessages(myId: String): Flow<List<Chat>> = getLastMessage(myId).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error
        emit(emptyList())
    }




}