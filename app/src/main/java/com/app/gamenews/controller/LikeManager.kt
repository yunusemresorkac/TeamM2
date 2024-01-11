package com.app.gamenews.controller


import com.app.gamenews.model.Game
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap

class LikeManager(postId: String, userId: String , private val listener: LikeStatusListener, collectionName : String) {
    private val likesCollection = FirebaseFirestore.getInstance().collection("Likes")
    private val postLikesDocument = likesCollection.document(postId)
    private val userLikeDocument = postLikesDocument.collection("Users").document(userId)

    private val myLikesCollection = FirebaseFirestore.getInstance().collection("MyLikes")
    private val myPostLikesDocument = myLikesCollection.document(userId)
    private val myLikeDocument = myPostLikesDocument.collection(collectionName).document(postId)


    // Beğeni durumunu dinlemeye başlayan fonksiyon
    fun startListening() {
        userLikeDocument.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Beğeni durumu dinlenirken bir hata oluştu: $error")

                return@addSnapshotListener
            }

            val liked = snapshot?.get("liked") as? Boolean
            listener.onLikeStatusChanged(liked == true)
        }
    }

    // Beğeni durumunu değiştiren fonksiyon
    fun toggleLike(id : String, collectionName: String) {
        userLikeDocument.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Kullanıcı beğeniyi daha önce eklemiş, bu yüzden beğeniyi kaldırın
                    userLikeDocument.delete()
                        .addOnSuccessListener {
                            myLikeDocument.delete().addOnSuccessListener {
                                decreaseLikeCount(id,collectionName)
                            }
                            println("Beğeni kaldırıldı.")
                        }
                        .addOnFailureListener { e ->
                            println("Beğeni kaldırılırken bir hata oluştu: $e")
                        }
                } else {
                    // Kullanıcı beğeniyi daha önce eklememiş, bu yüzden beğeniyi ekle
                    userLikeDocument.set(mapOf("liked" to true))
                        .addOnSuccessListener {
                            myLikeDocument.set(mapOf("liked" to true)).addOnSuccessListener {
                                increaseLikeCount(id,collectionName)
                            }
                            println("Beğeni eklendi.")
                        }
                        .addOnFailureListener { e ->
                            println("Beğeni eklenirken bir hata oluştu: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Beğeni durumu kontrol edilirken bir hata oluştu: $e")
            }
    }

    private fun increaseLikeCount(id: String, collectionName: String){
        val map: HashMap<String, Any> = HashMap()
        map["countOfLikes"] = FieldValue.increment(1)

        FirebaseFirestore.getInstance().collection(collectionName).document(id)
            .update(map).addOnSuccessListener {

            }
    }

    private fun decreaseLikeCount(id: String, collectionName: String){
        val map: HashMap<String, Any> = HashMap()
        map["countOfLikes"] = FieldValue.increment(-1)

        FirebaseFirestore.getInstance().collection(collectionName).document(id)
            .update(map).addOnSuccessListener {

            }
    }



    interface LikeStatusListener {
        fun onLikeStatusChanged(liked: Boolean)
    }


}