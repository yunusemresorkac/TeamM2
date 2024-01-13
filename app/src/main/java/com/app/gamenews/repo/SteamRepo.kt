package com.app.gamenews.repo

import com.app.gamenews.model.Game
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await

class SteamRepo {

    private val pageSize = 5
    private var lastVisible: DocumentSnapshot? = null
    private var isFetching = false

    private val firestore = FirebaseFirestore.getInstance()
    private val steamCollection = firestore.collection("Steam")

    private val gameList = ArrayList<Game>()

    private val _gameListState = MutableStateFlow<List<Game>>(emptyList())
    val gameListState: StateFlow<List<Game>> get() = _gameListState

    fun fetchInitialData() {
        if (!isFetching) {
            isFetching = true
            val query = steamCollection.orderBy("time", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    gameList.clear()
                    for (document in snapshot.documents) {
                        val game = document.toObject(Game::class.java)
                        game?.let { gameList.add(it) }
                    }
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                    _gameListState.value = gameList.toList()

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
            val query = steamCollection.orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible!!)
                .limit(pageSize.toLong())

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val game = document.toObject(Game::class.java)
                        game?.let { gameList.add(it) }

                    }
                    lastVisible = snapshot.documents[snapshot.size() - 1]
                    _gameListState.value = gameList.toList()
                }
                isFetching = false
            }.addOnFailureListener {
                // Handle failure
                isFetching = false
            }
        }
    }

}

/**
 * class SteamRepo {
 *
 *
 *     private fun getSteamPosts(): Flow<List<Game>> = callbackFlow {
 *         val querySnapshot = FirebaseFirestore.getInstance().collection("Steam")
 *             .orderBy("time", Query.Direction.DESCENDING)
 *             .get()
 *             .await()
 *
 *         val gameList = ArrayList<Game>()
 *
 *         val documents = querySnapshot.documents
 *         for (document in documents) {
 *             val game = document.toObject(Game::class.java)
 *             if (game != null) {
 *                 gameList.add(game)
 *             }
 *         }
 *
 *         trySend(gameList)
 *         close()
 *
 *     }
 *
 *     fun getAllSteamPosts(): Flow<List<Game>> = getSteamPosts().flowOn(Dispatchers.IO).onStart {
 *         emit(emptyList())
 *     }.catch {
 *         emit(emptyList())
 *     }
 *
 *
 * }
 *
 * */
