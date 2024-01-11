package com.app.gamenews.repo

import com.app.gamenews.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FollowingRepo {

    private val db = FirebaseFirestore.getInstance()

    private val pageSize = 10
    private var lastVisible: DocumentSnapshot? = null
    private var isFetching = false


    private val list = ArrayList<User>()

    private val _listState = MutableStateFlow<List<User>>(emptyList())
    val listState: StateFlow<List<User>> get() = _listState


    fun getFollowing(userId: String, onComplete: (List<String>) -> Unit, onError: (Exception) -> Unit) {
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

    fun fetchInitialData(followingIds: List<String>) {
        if (!isFetching) {
            isFetching = true
            val query = db.collection("Users")
                .whereIn("userId",followingIds)
                .limit(pageSize.toLong())

            query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle exception
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    list.clear()
                    for (document in snapshot.documents) {
                        val user = document.toObject(User::class.java)
                        user?.let { list.add(it) }
                        println("listemm $list")
                    }
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                    _listState.value = list.toList()
                }
                isFetching = false
            }
        }
    }

    fun loadMoreData(followingIds: List<String>) {
        if (!isFetching && lastVisible != null) {
            isFetching = true
            val query = db.collection("Users")
                .whereIn("userId",followingIds)
                .startAfter(lastVisible!!)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val user = document.toObject(User::class.java)
                        user?.let { list.add(it) }
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


}