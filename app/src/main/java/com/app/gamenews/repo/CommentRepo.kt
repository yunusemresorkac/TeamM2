package com.app.gamenews.repo

import com.app.gamenews.model.Comment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentRepo {

    private val pageSize = 8
    private var lastVisible: DocumentSnapshot? = null
    private var isFetching = false

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("Comments")

    private val list = ArrayList<Comment>()

    private val _listState = MutableStateFlow<List<Comment>>(emptyList())
    val listState: StateFlow<List<Comment>> get() = _listState



    fun fetchInitialData(postId: String) {
        if (!isFetching) {
            isFetching = true
            val query = collection.document(postId).collection("Comments").orderBy("time", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle exception
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    list.clear()
                    for (document in snapshot.documents) {
                        val comment = document.toObject(Comment::class.java)
                        comment?.let { list.add(it) }
                    }
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                    _listState.value = list.toList()
                }
                isFetching = false
            }
        }
    }

    fun loadMoreData(postId : String) {
        if (!isFetching && lastVisible != null) {
            isFetching = true
            val query = collection.document(postId).collection("Comments").orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible!!)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val comment = document.toObject(Comment::class.java)
                        comment?.let { list.add(it) }

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

    fun addComment(comment : Comment, onSuccess : () -> Unit, onFailure : () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestore.getInstance().collection("Comments").document(comment.postId)
                .collection("Comments").document(comment.commentId)
                .set(comment)
                .addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener {
                    onFailure()
                }
        }

    }


//    private fun getComments(postId : String): Flow<List<Comment>> = callbackFlow {
//        val querySnapshot = FirebaseFirestore.getInstance().collection("Comments")
//            .document(postId).collection("Comments")
//            .orderBy("time", Query.Direction.DESCENDING)
//            .get()
//            .await()
//
//        val commentList = ArrayList<Comment>()
//
//        val documents = querySnapshot.documents
//        for (document in documents) {
//            val comment = document.toObject(Comment::class.java)
//            if (comment != null) {
//                println("listem $comment")
//                commentList.add(comment)
//            }
//        }
//
//        trySend(commentList)
//        close()
//
//    }
//
//    fun getAllComments(postId: String): Flow<List<Comment>> = getComments(postId).flowOn(Dispatchers.IO).onStart {
//        emit(emptyList())
//    }.catch {
//        emit(emptyList())
//    }




}