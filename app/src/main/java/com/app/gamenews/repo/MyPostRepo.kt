package com.app.gamenews.repo

import android.content.Context
import com.app.gamenews.model.Post
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

class MyPostRepo {


    private val db = FirebaseFirestore.getInstance()

    private val pageSize = 5
    private var lastVisible: DocumentSnapshot? = null
    private var isFetching = false


    private val list = ArrayList<Post>()

    private val _listState = MutableStateFlow<List<Post>>(emptyList())
    val listState: StateFlow<List<Post>> get() = _listState

    fun fetchInitialData(userId: String) {
        if (!isFetching) {
            isFetching = true
            val query = db.collection("Posts")
                .whereEqualTo("userId",userId)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    list.clear()
                    for (document in snapshot.documents) {
                        val post = document.toObject(Post::class.java)
                        post?.let { list.add(it) }
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

    fun loadMoreData(userId : String) {
        if (!isFetching && lastVisible != null) {
            isFetching = true
            val query = db.collection("Posts")
                .whereEqualTo("userId",userId)
                .orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible!!)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val post = document.toObject(Post::class.java)
                        post?.let { list.add(it) }
                        println("listemm $list")


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

    fun getPostById(context : Context, postId: String, callback: (Post?) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("Posts").document(postId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val post = documentSnapshot.toObject(Post::class.java)
                    if (post != null) {
                        callback(post)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun updatePost(post: Post, onSuccess : () -> Unit, onFailure : () -> Unit ){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Posts").document(post.postId)
                .set(post)
                .addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener {
                    onFailure()
                }
        }

    }



    fun deletePost(post: Post, onSuccess : () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Posts").document(post.postId)
                .delete().addOnSuccessListener {
                    decreasePostNumber(post.userId)
                    onSuccess()
                }
        }
    }

    private fun decreasePostNumber(userId : String){
        CoroutineScope(Dispatchers.IO).launch {
            val map: HashMap<String, Any> = HashMap()
            map["posts"] = FieldValue.increment(-1)

            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update(map).addOnSuccessListener {

                }
        }

    }

}