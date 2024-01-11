package com.app.gamenews.repo

import com.app.gamenews.model.Post
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostRepo {

    private val db = FirebaseFirestore.getInstance()

    private val pageSize = 10
    private var lastVisible: DocumentSnapshot? = null
    private var isFetching = false



    private val list = ArrayList<Post>()

    private val _listState = MutableStateFlow<List<Post>>(emptyList())
    val listState: StateFlow<List<Post>> get() = _listState



    fun getFollowings(userId: String, onComplete: (List<String>) -> Unit, onError: (Exception) -> Unit) {
        val followersCollection = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId)
            .collection("Followings")

        followersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val followerIds = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    val followerId = document.id
                    followerIds.add(followerId)
                }
                onComplete(followerIds) // Invoke onComplete with the list of follower IDs
            }
            .addOnFailureListener { e ->
                onError(e) // Invoke onError in case of an exception
            }
    }


    fun fetchInitialData(followerIds: List<String>) {
        if (!isFetching) {
            isFetching = true
            val query = db.collection("Posts")
                .orderBy("time", Query.Direction.DESCENDING)
                .whereIn("userId", followerIds)
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


    fun loadMoreData(followerIds: List<String>) {
        if (!isFetching && lastVisible != null) {
            isFetching = true
            val query = db.collection("Posts")
                .orderBy("time", Query.Direction.DESCENDING)
                .whereIn("userId",followerIds)
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


    private fun increasePostNumber(userId : String){
        CoroutineScope(Dispatchers.IO).launch {
            val map: HashMap<String, Any> = HashMap()
            map["posts"] = FieldValue.increment(1)

            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update(map).addOnSuccessListener {

                }
        }

    }



    fun sharePost(post: Post, onSuccess : () -> Unit, onFailure : () -> Unit ){
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("Posts").document(post.postId)
                .set(post)
                .addOnSuccessListener {
                    increasePostNumber(post.userId)
                    onSuccess()
                }.addOnFailureListener {
                    onFailure()
                }
        }

    }


}